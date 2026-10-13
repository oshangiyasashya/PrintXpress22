package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.printxpress.data.entity.SupportQuery;
import java.util.List;

@Dao
public interface SupportQueryDao {
    @Insert
    long insert(SupportQuery query);

    @Query("SELECT * FROM support_queries WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<SupportQuery>> getQueriesByUser(long userId);

    @Query("SELECT * FROM support_queries WHERE userId = :userId ORDER BY createdAt ASC")
    LiveData<List<SupportQuery>> getQueriesByUserAsc(long userId);

    @Query("SELECT COUNT(*) FROM support_queries")
    int getQueryCountSync();

    @Query("SELECT * FROM support_queries ORDER BY createdAt DESC")
    LiveData<List<SupportQuery>> getAllQueries();

    @Query("UPDATE support_queries SET reply = :reply, repliedAt = :repliedAt, status = 'RESOLVED' WHERE id = :queryId")
    void replyToQuery(long queryId, String reply, long repliedAt);

    @Query("UPDATE support_queries SET status = :status WHERE id = :queryId")
    void updateQueryStatus(long queryId, String status);

    @Query("SELECT COUNT(*) FROM support_queries WHERE reply IS NULL OR reply = '' OR status = 'OPEN'")
    LiveData<Integer> getUnansweredTicketsCount();

    @Query("SELECT * FROM support_queries WHERE reply IS NULL OR reply = '' OR status = 'OPEN' ORDER BY createdAt DESC")
    LiveData<List<SupportQuery>> getUnansweredTickets();
}
