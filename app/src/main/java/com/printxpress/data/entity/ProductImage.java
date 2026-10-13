package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "product_images",
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("productId")}
)
public class ProductImage {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long productId;
    public String imageUri;
    public int sortOrder;

    public ProductImage(long productId, String imageUri, int sortOrder) {
        this.productId = productId;
        this.imageUri = imageUri;
        this.sortOrder = sortOrder;
    }
}
