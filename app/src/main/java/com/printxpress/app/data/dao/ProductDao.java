package com.printxpress.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.printxpress.app.data.model.ProductEntity;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ProductEntity> products);

    @Query("SELECT * FROM products ORDER BY category, name")
    LiveData<List<ProductEntity>> observeAll();

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name")
    LiveData<List<ProductEntity>> observeByCategory(String category);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    ProductEntity getById(String id);

    @Query("SELECT COUNT(*) FROM products")
    int count();
}
