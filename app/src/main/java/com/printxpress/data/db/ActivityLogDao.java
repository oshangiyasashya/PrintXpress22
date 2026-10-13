package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.printxpress.data.entity.AdminActivityLog;
import java.util.List;

@Dao
public interface ActivityLogDao {
    @Insert
    long insert(AdminActivityLog log);

    @Query("SELECT * FROM admin_activity_logs ORDER BY timestamp DESC")
    LiveData<List<AdminActivityLog>> getAllLogs();

    @Query("SELECT * FROM admin_activity_logs WHERE adminUserId = :adminUserId ORDER BY timestamp DESC")
    LiveData<List<AdminActivityLog>> getLogsForAdmin(long adminUserId);
}
