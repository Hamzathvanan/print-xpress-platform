package com.printxpress.app.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notifications",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("userId")
)
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;
    public String title;
    public String body;
    public String type;
    public boolean isRead;
    public long createdAt = System.currentTimeMillis();

    public NotificationEntity() {
    }

    @Ignore
    public NotificationEntity(long userId, String title, String body, String type) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.type = type;
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }
}
