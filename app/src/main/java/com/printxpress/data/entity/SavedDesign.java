package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(
        tableName = "saved_designs",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class SavedDesign {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String name;
    public String filePath;
    public String designJson; // Stores full layout state (positions, text, fonts, colors, sizes, background)
    public String thumbnailPath;
    public long createdAt;

    @Ignore
    public SavedDesign(long userId, String name, String filePath, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.filePath = filePath;
        this.designJson = "{}";
        this.thumbnailPath = "";
        this.createdAt = createdAt;
    }

    public SavedDesign(long userId, String name, String filePath, String designJson, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.filePath = filePath;
        this.designJson = designJson;
        this.thumbnailPath = "";
        this.createdAt = createdAt;
    }
}
