package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.printxpress.data.entity.CrashLog;
import java.util.List;

@Dao
public interface CrashLogDao {

    @Insert
    long insert(CrashLog log);

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC LIMIT 100")
    LiveData<List<CrashLog>> getAllCrashLogs();

    @Query("DELETE FROM crash_logs")
    void clearAllLogs();
}
