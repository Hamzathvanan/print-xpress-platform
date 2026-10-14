package com.printxpress.app.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "saved_designs",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("userId")
)
public class SavedDesignEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;
    public String title;
    public String notes;
    public String artworkUri;
    public long createdAt = System.currentTimeMillis();

    public SavedDesignEntity() {
    }

    @Ignore
    public SavedDesignEntity(long userId, String title, String notes, String artworkUri) {
        this.userId = userId;
        this.title = title;
        this.notes = notes;
        this.artworkUri = artworkUri;
        this.createdAt = System.currentTimeMillis();
    }
}
