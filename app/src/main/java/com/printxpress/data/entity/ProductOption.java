package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "product_options",
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("productId")}
)
public class ProductOption {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long productId;
    public String type; // e.g. "Paper Type", "Size", "Finish"
    public String value;
    public double priceModifier;

    public ProductOption(long productId, String type, String value, double priceModifier) {
        this.productId = productId;
        this.type = type;
        this.value = value;
        this.priceModifier = priceModifier;
    }
}