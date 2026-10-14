package com.printxpress.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.printxpress.app.data.model.OrderEntity;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insert(OrderEntity order);

    @Update
    void update(OrderEntity order);

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<OrderEntity>> observeForUser(long userId);

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    OrderEntity getById(long id);

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    LiveData<OrderEntity> observeById(long id);
}
