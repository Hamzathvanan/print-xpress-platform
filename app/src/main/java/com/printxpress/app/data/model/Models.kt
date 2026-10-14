package com.printxpress.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProductCategory(val label: String) {
    BUSINESS_CARDS("Business Cards"),
    FLYERS("Flyers"),
    POSTERS("Posters"),
    BANNERS("Banners"),
    STICKERS("Stickers"),
    MUGS("Mugs"),
    T_SHIRTS("T-Shirts")
}

enum class OrderStatus(val label: String) {
    RECEIVED("Received"),
    PROCESSING("Processing"),
    PRINTING("Printing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class DeliveryMode(val label: String) {
    PICKUP("Store Pickup"),
    DELIVERY("Home Delivery")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "addresses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val label: String,
    val line1: String,
    val city: String,
    val district: String,
    val postalCode: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String,
    val basePriceLkr: Double,
    val materials: String,
    val sizes: String,
    val samplePreview: String
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("userId"), Index("productId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: String,
    val quantity: Int,
    val paperType: String,
    val size: String,
    val customText: String?,
    val artworkUri: String?,
    val deliveryMode: String,
    val addressId: Long?,
    val scheduledDate: String,
    val status: String = OrderStatus.RECEIVED.name,
    val unitPriceLkr: Double,
    val totalPriceLkr: Double,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "saved_designs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class SavedDesignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val notes: String,
    val artworkUri: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: Long
)
