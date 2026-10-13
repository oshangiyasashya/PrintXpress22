package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notifications",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class Notification {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String title;
    public String message;
    public String type; // "ORDER", "PROMOTION", "SYSTEM"
    public boolean isRead;
    public boolean isDelivered;
    public long createdAt;

    public Notification(long userId, String title, String message, String type, boolean isRead, long createdAt) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.isDelivered = false;
        this.createdAt = createdAt;
    }
}