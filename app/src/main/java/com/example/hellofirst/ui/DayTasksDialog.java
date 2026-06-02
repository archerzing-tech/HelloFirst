package com.example.hellofirst.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        taskDao = AppDatabase.getInstance(requireContext()).taskDao();
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_day_detail, null);
        recyclerView = view.findViewById(R.id.dayTaskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        EditText input = view.findViewById(R.id.dayTaskInput);
        ImageButton addBtn = view.findViewById(R.id.dayTaskAddBtn);

        taskAdapter = new TaskAdapter(new ArrayList<>(), false, new TaskAdapter.OnTaskListener() {
            @Override
            public void onToggleComplete(Task task) {
                task.completed = !task.completed;
                Task t = task;
                new Thread(() -> { taskDao.update(t); loadTasks(); }).start();
            }

            @Override
            public void onDelete(Task task) {
                Task t = task;
                new Thread(() -> { taskDao.delete(t); loadTasks(); }).start();
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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> input.setText(""));
                    }
                    loadTasks();
                }).start();
            }
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(view)
                .setPositiveButton("关闭", null)
                .create();
    }

    private void loadTasks() {
        new Thread(() -> {
            if (!isAdded()) return;
            long dayStart = WeekFragment.getStartOfDay(dateMillis);
            long dayEnd = WeekFragment.getEndOfDay(dateMillis);
            final List<Task> tasks = taskDao.getTasksForDate(dayStart, dayEnd);
            if (!isAdded() || getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                taskAdapter.updateTasks(tasks);
            });
        }).start();
    }
}
