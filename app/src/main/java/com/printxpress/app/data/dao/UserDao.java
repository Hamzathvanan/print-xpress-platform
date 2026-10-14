package com.printxpress.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.printxpress.app.data.model.UserEntity;

@Dao
public interface UserDao {
    @Insert
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getById(long id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    UserEntity getByPhone(String phone);

    @Query("SELECT * FROM users WHERE (email IS NOT NULL AND lower(email) = lower(:identifier)) "
            + "OR (phone IS NOT NULL AND phone = :identifier) LIMIT 1")
    UserEntity getByEmailOrPhone(String identifier);
}
