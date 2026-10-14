package com.printxpress.app.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "session")
public class SessionEntity {
    @PrimaryKey
    public int id = 1;

    public long userId;

    public SessionEntity() {
    }

    @Ignore
    public SessionEntity(long userId) {
        this.id = 1;
        this.userId = userId;
    }
}
