package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "promotions")
public class Promotion {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String description;
    public double discountPercent;
    public int minQuantity;
    public long validFrom;
    public long validTo;

    public Promotion(String title, String description, double discountPercent, int minQuantity, long validFrom, long validTo) {
        this.title = title;
        this.description = description;
        this.discountPercent = discountPercent;
        this.minQuantity = minQuantity;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }
}