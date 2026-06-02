package com.example.hellofirst.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks WHERE target_date >= :startOfDay AND target_date < :endOfDay ORDER BY created_at ASC")
    List<Task> getTasksForDate(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE target_date >= :start AND target_date <= :end ORDER BY target_date ASC, created_at ASC")
    List<Task> getTasksInRange(long start, long end);

    @Query("SELECT * FROM tasks WHERE target_date > :endOfWeek ORDER BY target_date ASC, created_at ASC")
    List<Task> getFutureTasks(long endOfWeek);

    @Query("SELECT * FROM tasks WHERE target_date < :startOfWeek ORDER BY target_date DESC, created_at ASC")
    List<Task> getHistoryTasks(long startOfWeek);

    @Query("SELECT * FROM tasks WHERE target_date < :todayStart AND completed = 0 ORDER BY target_date ASC, created_at ASC")
    List<Task> getOverdueTasks(long todayStart);

    @Query("SELECT * FROM tasks ORDER BY target_date DESC, created_at ASC")
    List<Task> getAllTasks();

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);
}
