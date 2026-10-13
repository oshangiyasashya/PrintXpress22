package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "support_queries",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class SupportQuery {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String subject;
    public String message;
    public String reply;
    public String status; // "OPEN", "ANSWERED", "RESOLVED"
    public String ticketNumber;
    public long createdAt;
    public long repliedAt;

    public SupportQuery(long userId, String subject, String message, long createdAt) {
        this.userId = userId;
        this.subject = subject;
        this.message = message;
        this.createdAt = createdAt;
        this.reply = "Thank you for reaching out to PrintXpress! An agent has received your request and will follow up shortly.";
        this.status = "ANSWERED";
        this.ticketNumber = "TKT-" + (100000 + (Math.abs(createdAt) % 900000));
        this.repliedAt = createdAt + 60000L;
    }
}
