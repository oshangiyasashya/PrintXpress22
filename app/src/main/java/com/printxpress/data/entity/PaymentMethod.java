package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "payment_methods",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class PaymentMethod {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String cardHolderName;
    public String cardNumberMasked; // e.g. "**** **** **** 4242"
    public String cardType;         // e.g. "Visa", "Mastercard", "Amex"
    public String expiryDate;       // e.g. "12/28"
    public boolean isDefault;

    public PaymentMethod(long userId, String cardHolderName, String cardNumberMasked, String cardType, String expiryDate, boolean isDefault) {
        this.userId = userId;
        this.cardHolderName = cardHolderName;
        this.cardNumberMasked = cardNumberMasked;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault;
    }
}
