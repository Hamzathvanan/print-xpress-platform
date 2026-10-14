package com.printxpress.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.printxpress.app.data.model.AddressEntity;

import java.util.List;

@Dao
public interface AddressDao {
    @Insert
    long insert(AddressEntity address);

    @Update
    void update(AddressEntity address);

    @Query("DELETE FROM addresses WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    LiveData<List<AddressEntity>> observeForUser(long userId);

    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    List<AddressEntity> listForUser(long userId);

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    void clearDefaults(long userId);
}
