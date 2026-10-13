package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "cart_items",
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
        indices = {@Index("userId"), @Index("productId")}
)
public class CartItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public long productId;
    public String productName;
    public double basePrice;
    public int quantity;
    public String selectedOptions;
    public String designType; // "upload", "text", "sample"
    public String artworkUrl;
    public String customText;

    public CartItem(long userId, long productId, String productName, double basePrice, int quantity, String selectedOptions, String designType, String artworkUrl, String customText) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.selectedOptions = selectedOptions;
        this.designType = designType;
        this.artworkUrl = artworkUrl;
        this.customText = customText;
    }
}
