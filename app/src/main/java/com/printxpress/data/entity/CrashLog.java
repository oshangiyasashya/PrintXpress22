package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "crash_logs")
public class CrashLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String message;
    public String stackTrace;
    public long userId;
    public long timestamp;

    public CrashLog(String message, String stackTrace, long userId, long timestamp) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.userId = userId;
        this.timestamp = timestamp;
    }
}
