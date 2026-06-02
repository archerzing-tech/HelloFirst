package com.example.hellofirst.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellofirst.R;
import com.example.hellofirst.adapter.TaskAdapter;
import com.example.hellofirst.data.AppDatabase;
import com.example.hellofirst.data.Task;
import com.example.hellofirst.data.TaskDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FutureFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private TaskDao taskDao;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_future, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskDao = AppDatabase.getInstance(requireContext()).taskDao();
        recyclerView = view.findViewById(R.id.futureRecyclerView);
        emptyText = view.findViewById(R.id.futureEmptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ImageButton addBtn = view.findViewById(R.id.futureAddBtn);
        addBtn.setOnClickListener(v -> showAddDialog());

        taskAdapter = new TaskAdapter(new ArrayList<>(), true, new TaskAdapter.OnTaskListener() {
            @Override
            public void onToggleComplete(Task task) {
                task.completed = !task.completed;
                new Thread(() -> { taskDao.update(task); loadFuture(); }).start();
            }

            @Override
            public void onDelete(Task task) {
                new Thread(() -> { taskDao.delete(task); loadFuture(); }).start();
            }
        });
        recyclerView.setAdapter(taskAdapter);
        loadFuture();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFuture();
    }

    private void loadFuture() {
        new Thread(() -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            long nextWeekStart = getStartOfDay(cal.getTimeInMillis());
            final List<Task> tasks = taskDao.getFutureTasks(nextWeekStart - 1);
            requireActivity().runOnUiThread(() -> {
                taskAdapter.updateTasks(tasks);
                emptyText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void showAddDialog() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            long dateMillis = getStartOfDay(selected.getTimeInMillis());

            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
            EditText input = dialogView.findViewById(R.id.addTaskInput);
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("添加任务")
                    .setView(dialogView)
                    .setPositiveButton("确定", (dialog, which) -> {
                        String content = input.getText().toString().trim();
                        if (!content.isEmpty()) {
                            Task task = new Task(content, dateMillis);
                            new Thread(() -> {
                                taskDao.insert(task);
                                loadFuture();
                            }).start();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
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
}
