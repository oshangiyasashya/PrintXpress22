package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "products",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("categoryId")}
)
public class Product {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long categoryId;
    public String name;
    public String description;
    public double basePrice;
    public int imageRes;
    public String material;
    public String sizes;
    public boolean isActive = true;

    public Product(long categoryId, String name, String description, double basePrice, int imageRes, String material, String sizes) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.imageRes = imageRes;
        this.material = material;
        this.sizes = sizes;
        this.isActive = true;
    }
}