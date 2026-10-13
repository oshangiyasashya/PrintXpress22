package com.printxpress.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "faqs")
public class Faq {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String question;
    public String answer;

    public Faq(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}