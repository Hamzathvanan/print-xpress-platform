package com.printxpress.app.data

import com.printxpress.app.data.model.AddressEntity
import com.printxpress.app.data.model.DeliveryMode
import com.printxpress.app.data.model.NotificationEntity
import com.printxpress.app.data.model.OrderEntity
import com.printxpress.app.data.model.OrderStatus
import com.printxpress.app.data.model.ProductEntity
import com.printxpress.app.data.model.SavedDesignEntity
import com.printxpress.app.data.model.SessionEntity
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.util.Validation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class PrintXpressRepository(private val db: AppDatabase) {
    private val users = db.userDao()
    private val addresses = db.addressDao()
    private val products = db.productDao()
    private val orders = db.orderDao()
    private val designs = db.savedDesignDao()
    private val notifications = db.notificationDao()
    private val session = db.sessionDao()

    suspend fun ensureSeeded() {
        if (products.count() == 0) {
            products.upsertAll(ProductCatalog.seedProducts)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSessionUser(): Flow<UserEntity?> =
        session.observe().flatMapLatest { active ->
            if (active == null) {
                flowOf(null)
            } else {
                flow { emit(users.getById(active.userId)) }
            }
        }

    suspend fun currentUser(): UserEntity? {
        val active = session.get() ?: return null
        return users.getById(active.userId)
    }

    suspend fun register(
        fullName: String,
        email: String?,
        phone: String?,
        password: String
    ): Result<UserEntity> {
        if (!Validation.isValidName(fullName)) {
            return Result.failure(IllegalArgumentException("Enter your full name (at least 2 characters)."))
        }
        val cleanedEmail = email?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedPhone = phone?.let { Validation.normalizePhone(it) }?.takeIf { it.isNotEmpty() }
        if (cleanedEmail == null && cleanedPhone == null) {
            return Result.failure(IllegalArgumentException("Provide an email or a Sri Lankan mobile number."))
        }
        if (cleanedEmail != null && !Validation.isValidEmail(cleanedEmail)) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        if (cleanedPhone != null && !Validation.isValidPhone(cleanedPhone)) {
            return Result.failure(IllegalArgumentException("Enter a valid Sri Lankan mobile number (07XXXXXXXX)."))
        }
        if (!Validation.isValidPassword(password)) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (cleanedEmail != null && users.getByEmail(cleanedEmail) != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        if (cleanedPhone != null && users.getByPhone(cleanedPhone) != null) {
            return Result.failure(IllegalArgumentException("An account with this phone number already exists."))
        }

        val id = users.insert(
            UserEntity(
                fullName = fullName.trim(),
                email = cleanedEmail,
                phone = cleanedPhone,
                passwordHash = Validation.passwordHash(password)
            )
        )
        session.set(SessionEntity(userId = id))
        val user = users.getById(id)!!
        notifications.insert(
            NotificationEntity(
                userId = id,
                title = "Welcome to PrintXpress",
                body = "Browse products, upload artwork, and schedule pickup or delivery anywhere in Sri Lanka.",
                type = "welcome"
            )
        )
        ProductCatalog.seasonalOffers.forEach { offer ->
            notifications.insert(
                NotificationEntity(
                    userId = id,
                    title = "Seasonal offer",
                    body = offer,
                    type = "promo"
                )
            )
        }
        return Result.success(user)
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        if (!Validation.isNonBlank(identifier) || !Validation.isNonBlank(password)) {
            return Result.failure(IllegalArgumentException("Email/phone and password are required."))
        }
        val normalized = if (identifier.contains("@")) {
            identifier.trim()
        } else {
            Validation.normalizePhone(identifier)
        }
        val user = users.getByEmailOrPhone(normalized)
            ?: return Result.failure(IllegalArgumentException("No account found for that email or phone."))
        if (user.passwordHash != Validation.passwordHash(password)) {
            return Result.failure(IllegalArgumentException("Incorrect password."))
        }
        session.set(SessionEntity(userId = user.id))
        return Result.success(user)
    }

    suspend fun logout() {
        session.clear()
    }

    suspend fun updateProfile(user: UserEntity): Result<Unit> {
        if (!Validation.isValidName(user.fullName)) {
            return Result.failure(IllegalArgumentException("Enter a valid name."))
        }
        if (user.email != null && user.email.isNotBlank() && !Validation.isValidEmail(user.email)) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        if (user.phone != null && user.phone.isNotBlank() && !Validation.isValidPhone(user.phone)) {
            return Result.failure(IllegalArgumentException("Enter a valid phone number."))
        }
        users.update(user)
        return Result.success(Unit)
    }

    fun observeProducts(): Flow<List<ProductEntity>> = products.observeAll()

    fun observeProductsByCategory(category: String): Flow<List<ProductEntity>> =
        products.observeByCategory(category)

    suspend fun getProduct(id: String): ProductEntity? = products.getById(id)

    fun observeOrders(userId: Long): Flow<List<OrderEntity>> = orders.observeForUser(userId)

    fun observeOrder(orderId: Long): Flow<OrderEntity?> = orders.observeById(orderId)

    suspend fun placeOrder(
        userId: Long,
        product: ProductEntity,
        quantity: Int,
        paperType: String,
        size: String,
        customText: String?,
        artworkUri: String?,
        deliveryMode: DeliveryMode,
        addressId: Long?,
        scheduledDate: String,
        notes: String?
    ): Result<Long> {
        if (quantity !in 1..10000) {
            return Result.failure(IllegalArgumentException("Quantity must be between 1 and 10,000."))
        }
        if (paperType.isBlank() || size.isBlank()) {
            return Result.failure(IllegalArgumentException("Select material/paper type and size."))
        }
        if (customText.isNullOrBlank() && artworkUri.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Upload artwork or enter custom text/instructions."))
        }
        if (scheduledDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Choose a pickup/delivery date."))
        }
        if (deliveryMode == DeliveryMode.DELIVERY && addressId == null) {
            return Result.failure(IllegalArgumentException("Select a delivery address or add one in Profile."))
        }

        val total = product.basePriceLkr * quantity
        val orderId = orders.insert(
            OrderEntity(
                userId = userId,
                productId = product.id,
                quantity = quantity,
                paperType = paperType,
                size = size,
                customText = customText?.trim()?.takeIf { it.isNotEmpty() },
                artworkUri = artworkUri,
                deliveryMode = deliveryMode.name,
                addressId = addressId,
                scheduledDate = scheduledDate,
                unitPriceLkr = product.basePriceLkr,
                totalPriceLkr = total,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        notifications.insert(
            NotificationEntity(
                userId = userId,
                title = "Order #$orderId confirmed",
                body = "${product.name} × $quantity is received. Track progress in My Orders.",
                type = "order"
            )
        )
        return Result.success(orderId)
    }

    suspend fun cancelOrder(order: OrderEntity): Result<Unit> {
        val status = OrderStatus.valueOf(order.status)
        if (status != OrderStatus.RECEIVED && status != OrderStatus.PROCESSING) {
            return Result.failure(
                IllegalStateException("Orders can only be cancelled before printing begins.")
            )
        }
        orders.update(
            order.copy(
                status = OrderStatus.CANCELLED.name,
                updatedAt = System.currentTimeMillis()
            )
        )
        notifications.insert(
            NotificationEntity(
                userId = order.userId,
                title = "Order #${order.id} cancelled",
                body = "Your order was cancelled before printing started.",
                type = "order"
            )
        )
        return Result.success(Unit)
    }

    suspend fun rescheduleOrder(order: OrderEntity, newDate: String): Result<Unit> {
        val status = OrderStatus.valueOf(order.status)
        if (status != OrderStatus.RECEIVED && status != OrderStatus.PROCESSING) {
            return Result.failure(
                IllegalStateException("Orders can only be rescheduled before printing begins.")
            )
        }
        if (newDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Choose a new date."))
        }
        orders.update(
            order.copy(
                scheduledDate = newDate,
                updatedAt = System.currentTimeMillis()
            )
        )
        notifications.insert(
            NotificationEntity(
                userId = order.userId,
                title = "Order #${order.id} rescheduled",
                body = "New pickup/delivery date: $newDate",
                type = "order"
            )
        )
        return Result.success(Unit)
    }

    suspend fun advanceOrderDemo(order: OrderEntity): Result<Unit> {
        val next = when (OrderStatus.valueOf(order.status)) {
            OrderStatus.RECEIVED -> OrderStatus.PROCESSING
            OrderStatus.PROCESSING -> OrderStatus.PRINTING
            OrderStatus.PRINTING -> if (order.deliveryMode == DeliveryMode.PICKUP.name) {
                OrderStatus.READY_FOR_PICKUP
            } else {
                OrderStatus.OUT_FOR_DELIVERY
            }
            OrderStatus.READY_FOR_PICKUP, OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED, OrderStatus.CANCELLED -> {
                return Result.failure(IllegalStateException("Order is already finished."))
            }
        }
        orders.update(order.copy(status = next.name, updatedAt = System.currentTimeMillis()))
        notifications.insert(
            NotificationEntity(
                userId = order.userId,
                title = "Order #${order.id} update",
                body = "Status is now: ${next.label}",
                type = "order"
            )
        )
        return Result.success(Unit)
    }

    fun observeAddresses(userId: Long): Flow<List<AddressEntity>> = addresses.observeForUser(userId)

    suspend fun listAddresses(userId: Long): List<AddressEntity> = addresses.listForUser(userId)

    suspend fun saveAddress(address: AddressEntity): Result<Long> {
        if (!Validation.isNonBlank(address.label) ||
            !Validation.isNonBlank(address.line1) ||
            !Validation.isNonBlank(address.city) ||
            !Validation.isNonBlank(address.district)
        ) {
            return Result.failure(IllegalArgumentException("Label, street, city, and district are required."))
        }
        if (address.isDefault) {
            addresses.clearDefaults(address.userId)
        }
        val id = if (address.id == 0L) {
            addresses.insert(address)
        } else {
            addresses.update(address)
            address.id
        }
        return Result.success(id)
    }

    suspend fun deleteAddress(id: Long) = addresses.delete(id)

    fun observeDesigns(userId: Long): Flow<List<SavedDesignEntity>> = designs.observeForUser(userId)

    suspend fun saveDesign(design: SavedDesignEntity): Result<Long> {
        if (!Validation.isNonBlank(design.title)) {
            return Result.failure(IllegalArgumentException("Design title is required."))
        }
        return Result.success(designs.insert(design))
    }

    suspend fun deleteDesign(id: Long) = designs.delete(id)

    fun observeNotifications(userId: Long): Flow<List<NotificationEntity>> =
        notifications.observeForUser(userId)

    fun observeUnreadCount(userId: Long): Flow<Int> = notifications.observeUnreadCount(userId)

    suspend fun markNotificationsRead(userId: Long) = notifications.markAllRead(userId)

    fun priceLabel(amount: Double): String = "LKR %,d".format(amount.toInt())
}
