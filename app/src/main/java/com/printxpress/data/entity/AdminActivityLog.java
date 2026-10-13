package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "admin_activity_logs")
public class AdminActivityLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long adminUserId;
    public String adminName;
    public String actionName;
    public String details;
    public long timestamp;

    public AdminActivityLog(long adminUserId, String adminName, String actionName, String details, long timestamp) {
        this.adminUserId = adminUserId;
        this.adminName = adminName;
        this.actionName = actionName;
        this.details = details;
        this.timestamp = timestamp;
    }
}
