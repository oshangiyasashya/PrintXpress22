package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.printxpress.data.entity.UserSettings;

@Dao
public interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserSettings settings);

    @Update
    void update(UserSettings settings);

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    LiveData<UserSettings> getSettings(long userId);

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    UserSettings getSettingsSync(long userId);
}
