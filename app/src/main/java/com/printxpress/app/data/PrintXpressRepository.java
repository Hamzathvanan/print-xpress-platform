package com.printxpress.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.printxpress.app.data.dao.AddressDao;
import com.printxpress.app.data.dao.NotificationDao;
import com.printxpress.app.data.dao.OrderDao;
import com.printxpress.app.data.dao.ProductDao;
import com.printxpress.app.data.dao.SavedDesignDao;
import com.printxpress.app.data.dao.SessionDao;
import com.printxpress.app.data.dao.UserDao;
import com.printxpress.app.data.model.AddressEntity;
import com.printxpress.app.data.model.DeliveryMode;
import com.printxpress.app.data.model.NotificationEntity;
import com.printxpress.app.data.model.OrderEntity;
import com.printxpress.app.data.model.OrderStatus;
import com.printxpress.app.data.model.ProductEntity;
import com.printxpress.app.data.model.SavedDesignEntity;
import com.printxpress.app.data.model.SessionEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;
import com.printxpress.app.util.Validation;

import java.util.List;
import java.util.Locale;

public class PrintXpressRepository {
    private final UserDao users;
    private final AddressDao addresses;
    private final ProductDao products;
    private final OrderDao orders;
    private final SavedDesignDao designs;
    private final NotificationDao notifications;
    private final SessionDao session;
    private final MediatorLiveData<UserEntity> sessionUser = new MediatorLiveData<>();
    private boolean sessionUserWired;

    public PrintXpressRepository(AppDatabase db) {
        users = db.userDao();
        addresses = db.addressDao();
        products = db.productDao();
        orders = db.orderDao();
        designs = db.savedDesignDao();
        notifications = db.notificationDao();
        session = db.sessionDao();
    }

    public void ensureSeeded() {
        if (products.count() == 0) {
            products.upsertAll(ProductCatalog.SEED_PRODUCTS);
        }
    }

    public LiveData<UserEntity> observeSessionUser() {
        if (!sessionUserWired) {
            sessionUserWired = true;
            sessionUser.addSource(session.observe(), active -> {
                if (active == null) {
                    sessionUser.setValue(null);
                    return;
                }
                AppExecutors.get().diskIo().execute(() -> {
                    UserEntity user = users.getById(active.userId);
                    sessionUser.postValue(user);
                });
            });
        }
        return sessionUser;
    }

    public void refreshSessionUser() {
        AppExecutors.get().diskIo().execute(() -> {
            sessionUser.postValue(currentUser());
        });
    }

    public UserEntity currentUser() {
        SessionEntity active = session.get();
        if (active == null) {
            return null;
        }
        return users.getById(active.userId);
    }

    public Result<UserEntity> register(String fullName, String email, String phone, String password) {
        if (!Validation.isValidName(fullName)) {
            return Result.failure(new IllegalArgumentException(
                    "Enter your full name (at least 2 characters)."));
        }
        String cleanedEmail = blankToNull(email);
        String cleanedPhone = phone == null || phone.trim().isEmpty()
                ? null
                : Validation.normalizePhone(phone);
        if (cleanedPhone != null && cleanedPhone.isEmpty()) {
            cleanedPhone = null;
        }
        if (cleanedEmail == null && cleanedPhone == null) {
            return Result.failure(new IllegalArgumentException(
                    "Provide an email or a Sri Lankan mobile number."));
        }
        if (cleanedEmail != null && !Validation.isValidEmail(cleanedEmail)) {
            return Result.failure(new IllegalArgumentException("Enter a valid email address."));
        }
        if (cleanedPhone != null && !Validation.isValidPhone(cleanedPhone)) {
            return Result.failure(new IllegalArgumentException(
                    "Enter a valid Sri Lankan mobile number (07XXXXXXXX)."));
        }
        if (!Validation.isValidPassword(password)) {
            return Result.failure(new IllegalArgumentException(
                    "Password must be at least 6 characters."));
        }
        if (cleanedEmail != null && users.getByEmail(cleanedEmail) != null) {
            return Result.failure(new IllegalArgumentException(
                    "An account with this email already exists."));
        }
        if (cleanedPhone != null && users.getByPhone(cleanedPhone) != null) {
            return Result.failure(new IllegalArgumentException(
                    "An account with this phone number already exists."));
        }

        long id = users.insert(new UserEntity(
                fullName.trim(),
                cleanedEmail,
                cleanedPhone,
                Validation.passwordHash(password)
        ));
        session.set(new SessionEntity(id));
        UserEntity user = users.getById(id);
        notifications.insert(new NotificationEntity(
                id,
                "Welcome to PrintXpress",
                "Browse products, upload artwork, and schedule pickup or delivery anywhere in Sri Lanka.",
                "welcome"
        ));
        for (String offer : ProductCatalog.SEASONAL_OFFERS) {
            notifications.insert(new NotificationEntity(id, "Seasonal offer", offer, "promo"));
        }
        return Result.success(user);
    }

    public Result<UserEntity> login(String identifier, String password) {
        if (!Validation.isNonBlank(identifier) || !Validation.isNonBlank(password)) {
            return Result.failure(new IllegalArgumentException(
                    "Email/phone and password are required."));
        }
        String normalized = identifier.contains("@")
                ? identifier.trim()
                : Validation.normalizePhone(identifier);
        UserEntity user = users.getByEmailOrPhone(normalized);
        if (user == null) {
            return Result.failure(new IllegalArgumentException(
                    "No account found for that email or phone."));
        }
        if (!user.passwordHash.equals(Validation.passwordHash(password))) {
            return Result.failure(new IllegalArgumentException("Incorrect password."));
        }
        session.set(new SessionEntity(user.id));
        return Result.success(user);
    }

    public void logout() {
        session.clear();
    }

    public Result<Void> updateProfile(UserEntity user) {
        if (!Validation.isValidName(user.fullName)) {
            return Result.failure(new IllegalArgumentException("Enter a valid name."));
        }
        if (user.email != null && !user.email.trim().isEmpty()
                && !Validation.isValidEmail(user.email)) {
            return Result.failure(new IllegalArgumentException("Enter a valid email address."));
        }
        if (user.phone != null && !user.phone.trim().isEmpty()
                && !Validation.isValidPhone(user.phone)) {
            return Result.failure(new IllegalArgumentException("Enter a valid phone number."));
        }
        users.update(user);
        return Result.success(null);
    }

    public LiveData<List<ProductEntity>> observeProducts() {
        return products.observeAll();
    }

    public LiveData<List<ProductEntity>> observeProductsByCategory(String category) {
        return products.observeByCategory(category);
    }

    public ProductEntity getProduct(String id) {
        return products.getById(id);
    }

    public LiveData<List<OrderEntity>> observeOrders(long userId) {
        return orders.observeForUser(userId);
    }

    public LiveData<OrderEntity> observeOrder(long orderId) {
        return orders.observeById(orderId);
    }

    public Result<Long> placeOrder(long userId, ProductEntity product, int quantity,
                                   String paperType, String size, String customText,
                                   String artworkUri, DeliveryMode deliveryMode,
                                   Long addressId, String scheduledDate, String notes) {
        if (quantity < 1 || quantity > 10000) {
            return Result.failure(new IllegalArgumentException(
                    "Quantity must be between 1 and 10,000."));
        }
        if (paperType == null || paperType.trim().isEmpty()
                || size == null || size.trim().isEmpty()) {
            return Result.failure(new IllegalArgumentException(
                    "Select material/paper type and size."));
        }
        boolean hasText = customText != null && !customText.trim().isEmpty();
        boolean hasArt = artworkUri != null && !artworkUri.trim().isEmpty();
        if (!hasText && !hasArt) {
            return Result.failure(new IllegalArgumentException(
                    "Upload artwork or enter custom text/instructions."));
        }
        if (scheduledDate == null || scheduledDate.trim().isEmpty()) {
            return Result.failure(new IllegalArgumentException(
                    "Choose a pickup/delivery date."));
        }
        if (deliveryMode == DeliveryMode.DELIVERY && addressId == null) {
            return Result.failure(new IllegalArgumentException(
                    "Select a delivery address or add one in Profile."));
        }

        double total = product.basePriceLkr * quantity;
        OrderEntity order = new OrderEntity();
        order.userId = userId;
        order.productId = product.id;
        order.quantity = quantity;
        order.paperType = paperType;
        order.size = size;
        order.customText = blankToNull(customText);
        order.artworkUri = artworkUri;
        order.deliveryMode = deliveryMode.name();
        order.addressId = addressId;
        order.scheduledDate = scheduledDate;
        order.unitPriceLkr = product.basePriceLkr;
        order.totalPriceLkr = total;
        order.notes = blankToNull(notes);
        long orderId = orders.insert(order);
        notifications.insert(new NotificationEntity(
                userId,
                "Order #" + orderId + " confirmed",
                product.name + " × " + quantity + " is received. Track progress in My Orders.",
                "order"
        ));
        return Result.success(orderId);
    }

    public Result<Void> cancelOrder(OrderEntity order) {
        OrderStatus status = OrderStatus.valueOf(order.status);
        if (status != OrderStatus.RECEIVED && status != OrderStatus.PROCESSING) {
            return Result.failure(new IllegalStateException(
                    "Orders can only be cancelled before printing begins."));
        }
        order.status = OrderStatus.CANCELLED.name();
        order.updatedAt = System.currentTimeMillis();
        orders.update(order);
        notifications.insert(new NotificationEntity(
                order.userId,
                "Order #" + order.id + " cancelled",
                "Your order was cancelled before printing started.",
                "order"
        ));
        return Result.success(null);
    }

    public Result<Void> rescheduleOrder(OrderEntity order, String newDate) {
        OrderStatus status = OrderStatus.valueOf(order.status);
        if (status != OrderStatus.RECEIVED && status != OrderStatus.PROCESSING) {
            return Result.failure(new IllegalStateException(
                    "Orders can only be rescheduled before printing begins."));
        }
        if (newDate == null || newDate.trim().isEmpty()) {
            return Result.failure(new IllegalArgumentException("Choose a new date."));
        }
        order.scheduledDate = newDate;
        order.updatedAt = System.currentTimeMillis();
        orders.update(order);
        notifications.insert(new NotificationEntity(
                order.userId,
                "Order #" + order.id + " rescheduled",
                "New pickup/delivery date: " + newDate,
                "order"
        ));
        return Result.success(null);
    }

    public Result<Void> advanceOrderDemo(OrderEntity order) {
        OrderStatus current = OrderStatus.valueOf(order.status);
        OrderStatus next;
        switch (current) {
            case RECEIVED:
                next = OrderStatus.PROCESSING;
                break;
            case PROCESSING:
                next = OrderStatus.PRINTING;
                break;
            case PRINTING:
                next = DeliveryMode.PICKUP.name().equals(order.deliveryMode)
                        ? OrderStatus.READY_FOR_PICKUP
                        : OrderStatus.OUT_FOR_DELIVERY;
                break;
            case READY_FOR_PICKUP:
            case OUT_FOR_DELIVERY:
                next = OrderStatus.COMPLETED;
                break;
            case COMPLETED:
            case CANCELLED:
            default:
                return Result.failure(new IllegalStateException("Order is already finished."));
        }
        order.status = next.name();
        order.updatedAt = System.currentTimeMillis();
        orders.update(order);
        notifications.insert(new NotificationEntity(
                order.userId,
                "Order #" + order.id + " update",
                "Status is now: " + next.getLabel(),
                "order"
        ));
        return Result.success(null);
    }

    public LiveData<List<AddressEntity>> observeAddresses(long userId) {
        return addresses.observeForUser(userId);
    }

    public List<AddressEntity> listAddresses(long userId) {
        return addresses.listForUser(userId);
    }

    public Result<Long> saveAddress(AddressEntity address) {
        if (!Validation.isNonBlank(address.label)
                || !Validation.isNonBlank(address.line1)
                || !Validation.isNonBlank(address.city)
                || !Validation.isNonBlank(address.district)) {
            return Result.failure(new IllegalArgumentException(
                    "Label, street, city, and district are required."));
        }
        if (address.isDefault) {
            addresses.clearDefaults(address.userId);
        }
        long id;
        if (address.id == 0L) {
            id = addresses.insert(address);
        } else {
            addresses.update(address);
            id = address.id;
        }
        return Result.success(id);
    }

    public void deleteAddress(long id) {
        addresses.delete(id);
    }

    public LiveData<List<SavedDesignEntity>> observeDesigns(long userId) {
        return designs.observeForUser(userId);
    }

    public Result<Long> saveDesign(SavedDesignEntity design) {
        if (!Validation.isNonBlank(design.title)) {
            return Result.failure(new IllegalArgumentException("Design title is required."));
        }
        return Result.success(designs.insert(design));
    }

    public void deleteDesign(long id) {
        designs.delete(id);
    }

    public LiveData<List<NotificationEntity>> observeNotifications(long userId) {
        return notifications.observeForUser(userId);
    }

    public LiveData<Integer> observeUnreadCount(long userId) {
        return notifications.observeUnreadCount(userId);
    }

    public void markNotificationsRead(long userId) {
        notifications.markAllRead(userId);
    }

    public String priceLabel(double amount) {
        return String.format(Locale.US, "LKR %,d", (int) amount);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
