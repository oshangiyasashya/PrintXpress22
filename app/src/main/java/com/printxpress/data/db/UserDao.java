package com.printxpress.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.printxpress.data.entity.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE (email = :email OR phone = :email) AND isDeleted = 0 LIMIT 1")
    User getUserByEmailSync(String email);

    @Query("SELECT * FROM users WHERE id = :id AND isDeleted = 0 LIMIT 1")
    LiveData<User> getUserById(long id);

    @Query("SELECT * FROM users WHERE id = :id AND isDeleted = 0 LIMIT 1")
    User getUserByIdSync(long id);

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt, hashAlgorithm = :algorithm WHERE id = :userId")
    int updatePasswordById(long userId, String newHash, String newSalt, String algorithm);

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt, hashAlgorithm = 'PBKDF2' WHERE email = :email")
    int updatePasswordByEmail(String email, String newHash, String newSalt);

    @Query("UPDATE users SET failedLoginAttempts = :attempts, lockoutUntil = :lockout WHERE email = :email")
    void updateLoginAttempts(String email, int attempts, long lockout);

    @Query("UPDATE users SET isBlocked = :blocked WHERE id = :userId")
    void updateUserBlockedStatus(long userId, boolean blocked);

    @Query("UPDATE users SET isDeleted = 1 WHERE id = :userId")
    void softDeleteUser(long userId);

    @Query("SELECT * FROM users WHERE isDeleted = 0 ORDER BY id DESC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users WHERE role = :role AND isDeleted = 0 ORDER BY id DESC")
    LiveData<List<User>> getUsersByRole(String role);

    @Query("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER' AND isDeleted = 0")
    LiveData<Integer> getTotalCustomersCount();

    @Query("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER' AND isDeleted = 0 AND createdAt >= :startTime")
    LiveData<Integer> getNewCustomersCount(long startTime);

    @Query("SELECT * FROM users WHERE isDeleted = 0 AND (fullName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY id DESC")
    LiveData<List<User>> searchUsers(String query);
}
