package com.printxpress.app.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String fullName;
    public String email;
    public String phone;
    public String passwordHash;
    public long createdAt;

    public UserEntity() {
        createdAt = System.currentTimeMillis();
    }

    @Ignore
    public UserEntity(String fullName, String email, String phone, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = System.currentTimeMillis();
    }
}
