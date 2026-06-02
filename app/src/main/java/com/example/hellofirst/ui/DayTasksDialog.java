package com.example.hellofirst.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellofirst.R;
import com.example.hellofirst.adapter.TaskAdapter;
import com.example.hellofirst.data.AppDatabase;
import com.example.hellofirst.data.Task;
import com.example.hellofirst.data.TaskDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DayTasksDialog extends DialogFragment {
    private static final String ARG_DATE = "date_millis";
    private static final String ARG_TITLE = "title";

    private long dateMillis;
    private String title;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TaskDao taskDao;

    public static DayTasksDialog newInstance(long dateMillis, String title) {
        DayTasksDialog dialog = new DayTasksDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, dateMillis);
        args.putString(ARG_TITLE, title);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dateMillis = getArguments().getLong(ARG_DATE);
            title = getArguments().getString(ARG_TITLE);
        }
        taskDao = AppDatabase.getInstance(requireContext()).taskDao();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_day_detail, null);
        recyclerView = view.findViewById(R.id.dayTaskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        EditText input = view.findViewById(R.id.dayTaskInput);
        View addBtn = view.findViewById(R.id.dayTaskAddBtn);

        taskAdapter = new TaskAdapter(new ArrayList<>(), false, new TaskAdapter.OnTaskListener() {
            @Override
            public void onToggleComplete(Task task) {
                task.completed = !task.completed;
                new Thread(() -> { taskDao.update(task); loadTasks(); }).start();
            }

            @Override
            public void onDelete(Task task) {
                new Thread(() -> { taskDao.delete(task); loadTasks(); }).start();
            }
        });
        recyclerView.setAdapter(taskAdapter);
        loadTasks();

        addBtn.setOnClickListener(v -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                Task task = new Task(content, dateMillis);
                new Thread(() -> {
                    taskDao.insert(task);
                    requireActivity().runOnUiThread(() -> input.setText(""));
                    loadTasks();
                }).start();
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(view)
                .setPositiveButton("关闭", null)
                .create();
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        return dialog;
    }

    private void loadTasks() {
        new Thread(() -> {
            long dayStart = getStartOfDay(dateMillis);
            long dayEnd = getEndOfDay(dateMillis);
            final List<Task> tasks = taskDao.getTasksForDate(dayStart, dayEnd);
            requireActivity().runOnUiThread(() -> taskAdapter.updateTasks(tasks));
        }).start();
    }

    private long getStartOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}
