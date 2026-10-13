package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "order_status_history",
        foreignKeys = @ForeignKey(
                entity = Order.class,
                parentColumns = "id",
                childColumns = "orderId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("orderId")}
)
public class OrderStatusHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long orderId;
    public String status;
    public long timestamp;

    public OrderStatusHistory(long orderId, String status, long timestamp) {
        this.orderId = orderId;
        this.status = status;
        this.timestamp = timestamp;
    }
}