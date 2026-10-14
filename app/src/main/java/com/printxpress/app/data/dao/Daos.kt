package com.printxpress.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.printxpress.app.data.model.AddressEntity
import com.printxpress.app.data.model.NotificationEntity
import com.printxpress.app.data.model.OrderEntity
import com.printxpress.app.data.model.ProductEntity
import com.printxpress.app.data.model.SavedDesignEntity
import com.printxpress.app.data.model.SessionEntity
import com.printxpress.app.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query(
        """
        SELECT * FROM users
        WHERE (email IS NOT NULL AND lower(email) = lower(:identifier))
           OR (phone IS NOT NULL AND phone = :identifier)
        LIMIT 1
        """
    )
    suspend fun getByEmailOrPhone(identifier: String): UserEntity?
}

@Dao
interface AddressDao {
    @Insert
    suspend fun insert(address: AddressEntity): Long

    @Update
    suspend fun update(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    fun observeForUser(userId: Long): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    suspend fun listForUser(userId: Long): List<AddressEntity>

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaults(userId: Long)
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("SELECT * FROM products ORDER BY category, name")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name")
    fun observeByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface OrderDao {
    @Insert
    suspend fun insert(order: OrderEntity): Long

    @Update
    suspend fun update(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<OrderEntity?>
}

@Dao
interface SavedDesignDao {
    @Insert
    suspend fun insert(design: SavedDesignEntity): Long

    @Query("DELETE FROM saved_designs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM saved_designs WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: Long): Flow<List<SavedDesignEntity>>
}

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Insert
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: Long): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: Long)

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun observeUnreadCount(userId: Long): Flow<Int>
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(session: SessionEntity)

    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    fun observe(): Flow<SessionEntity?>

    @Query("SELECT * FROM session WHERE id = 1 LIMIT 1")
    suspend fun get(): SessionEntity?

    @Query("DELETE FROM session")
    suspend fun clear()
}
