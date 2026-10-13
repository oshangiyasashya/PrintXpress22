package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "addresses",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class Address {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String label;
    public String line1;
    public String city;
    public String district;
    public String postalCode;
    public boolean isDefault;

    public Address(long userId, String label, String line1, String city, String district, String postalCode, boolean isDefault) {
        this.userId = userId;
        this.label = label;
        this.line1 = line1;
        this.city = city;
        this.district = district;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }
}