package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "order_items",
        foreignKeys = {
                @ForeignKey(
                        entity = Order.class,
                        parentColumns = "id",
                        childColumns = "orderId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("orderId"), @Index("productId")}
)
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long orderId;
    public long productId;
    public int quantity;
    public String selectedOptions;
    public String customText;
    public String designFilePath;
    public double unitPrice;

    public OrderItem(long orderId, long productId, int quantity, String selectedOptions, String customText, String designFilePath, double unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.selectedOptions = selectedOptions;
        this.customText = customText;
        this.designFilePath = designFilePath;
        this.unitPrice = unitPrice;
    }
}