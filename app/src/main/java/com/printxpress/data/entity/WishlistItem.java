package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "wishlist",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("userId"),
                @Index("productId"),
                @Index(value = {"userId", "productId"}, unique = true)
        }
)
public class WishlistItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public long productId;
    public long addedAt;

    public WishlistItem(long userId, long productId, long addedAt) {
        this.userId = userId;
        this.productId = productId;
        this.addedAt = addedAt;
    }
}
