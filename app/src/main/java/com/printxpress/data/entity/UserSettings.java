package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "user_settings",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        )
)
public class UserSettings {
    @PrimaryKey
    public long userId;
    public boolean notifyOrderUpdates;
    public boolean notifyPromotions;
    public boolean notifyNewProducts;
    public String theme; // "SYSTEM", "LIGHT", "DARK"

    public UserSettings(long userId, boolean notifyOrderUpdates, boolean notifyPromotions, boolean notifyNewProducts, String theme) {
        this.userId = userId;
        this.notifyOrderUpdates = notifyOrderUpdates;
        this.notifyPromotions = notifyPromotions;
        this.notifyNewProducts = notifyNewProducts;
        this.theme = theme != null ? theme : "SYSTEM";
    }
}
