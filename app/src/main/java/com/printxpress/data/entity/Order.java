package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "orders",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Address.class,
                        parentColumns = "id",
                        childColumns = "addressId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {@Index("userId"), @Index("addressId")}
)
public class Order {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String status; // "PENDING", "PROCESSING", "READY", "DELIVERED", "CANCELLED"
    public String deliveryType; // "DELIVERY", "PICKUP"
    public Long addressId;
    public String shippingAddressText;
    public String paymentMethod; // "CARD", "COD", "PICKUP"
    public String paymentStatus; // "PAID", "PENDING"
    public String trackingNumber;
    public long scheduledDate;
    public double totalPrice;
    public String notes;
    public long createdAt;
    public String cancelReason;
    public String promoCode;
    public double discountAmount;
    public String adminNotes = "";
    public boolean rescheduleRequested = false;
    public long rescheduleDate = 0L;
    public boolean cancelApproved = false;

    public Order(long userId, String status, String deliveryType, Long addressId, long scheduledDate, double totalPrice, String notes, long createdAt) {
        this.userId = userId;
        this.status = status;
        this.deliveryType = deliveryType;
        this.addressId = addressId;
        this.scheduledDate = scheduledDate;
        this.totalPrice = totalPrice;
        this.notes = notes;
        this.createdAt = createdAt;
        this.shippingAddressText = "";
        this.paymentMethod = "COD";
        this.paymentStatus = "PENDING";
        this.trackingNumber = "PX-" + (System.currentTimeMillis() % 1000000);
        this.cancelReason = "";
        this.promoCode = "";
        this.discountAmount = 0.0;
    }
}