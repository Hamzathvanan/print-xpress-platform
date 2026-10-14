package com.printxpress.app.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "addresses",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("userId")
)
public class AddressEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;
    public String label;
    public String line1;
    public String city;
    public String district;
    public String postalCode;
    public boolean isDefault;

    public AddressEntity() {
    }

    @Ignore
    public AddressEntity(long userId, String label, String line1, String city,
                         String district, String postalCode, boolean isDefault) {
        this.userId = userId;
        this.label = label;
        this.line1 = line1;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }
}
