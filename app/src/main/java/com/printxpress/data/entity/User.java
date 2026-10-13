package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = "email", unique = true),
                @Index(value = "phone", unique = true)
        }
)
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String fullName;
    public String email;
    public String phone;
    public String passwordHash;
    public String salt;
    public long createdAt;
    public String role;
    public int failedLoginAttempts;
    public long lockoutUntil;
    public boolean isBlocked;
    public String profileImageUri;
    public String hashAlgorithm = "SHA256";
    public boolean isDeleted = false;

    public User(String fullName, String email, String phone, String passwordHash, String salt, long createdAt) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
        this.role = "CUSTOMER";
        this.failedLoginAttempts = 0;
        this.lockoutUntil = 0L;
        this.isBlocked = false;
        this.hashAlgorithm = "PBKDF2";
        this.isDeleted = false;
    }

    @Ignore
    public User(String fullName, String email, String phone, String passwordHash, String salt, long createdAt, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
        this.role = role != null ? role : "CUSTOMER";
        this.failedLoginAttempts = 0;
        this.lockoutUntil = 0L;
        this.isBlocked = false;
        this.hashAlgorithm = "PBKDF2";
        this.isDeleted = false;
    }
}
