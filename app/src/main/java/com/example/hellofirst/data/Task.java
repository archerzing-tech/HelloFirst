package com.example.hellofirst.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "target_date")
    public long targetDate;

    @ColumnInfo(name = "completed")
    public boolean completed;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public Task(String content, long targetDate) {
        this.content = content;
        this.targetDate = targetDate;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
    }
}
