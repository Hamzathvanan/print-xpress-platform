package com.printxpress.app.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "orders",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ProductEntity.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {@Index("userId"), @Index("productId")}
)
public class OrderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;
    public String productId;
    public int quantity;
    public String paperType;
    public String size;
    public String customText;
    public String artworkUri;
    public String deliveryMode;
    public Long addressId;
    public String scheduledDate;
    public String status = OrderStatus.RECEIVED.name();
    public double unitPriceLkr;
    public double totalPriceLkr;
    public String notes;
    public long createdAt = System.currentTimeMillis();
    public long updatedAt = System.currentTimeMillis();

    public OrderEntity() {
    }
}
