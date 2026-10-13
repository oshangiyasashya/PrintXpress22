package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sample_designs",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("categoryId")}
)
public class SampleDesign {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long categoryId;
    public String title;
    public int imageRes;

    public SampleDesign(long categoryId, String title, int imageRes) {
        this.categoryId = categoryId;
        this.title = title;
        this.imageRes = imageRes;
    }
}