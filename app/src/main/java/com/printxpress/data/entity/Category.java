package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public int iconRes;

    public Category(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }
}