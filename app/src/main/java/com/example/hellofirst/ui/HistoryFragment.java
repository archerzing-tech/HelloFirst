package com.example.hellofirst.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private TaskDao taskDao;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskDao = AppDatabase.getInstance(requireContext()).taskDao();
        recyclerView = view.findViewById(R.id.historyRecyclerView);
        emptyText = view.findViewById(R.id.historyEmptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        new Thread(() -> {
            if (!isAdded()) return;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            long weekStart = WeekFragment.getStartOfDay(cal.getTimeInMillis());
            List<Task> allTasks = taskDao.getHistoryTasks(weekStart);

            List<HistoryGroup> groups = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.getDefault());
            long currentDate = -1;
            List<Task> currentList = new ArrayList<>();

            for (Task t : allTasks) {
                if (t.targetDate != currentDate) {
                    if (currentDate != -1) {
                        groups.add(new HistoryGroup(sdf.format(new Date(currentDate)), currentDate, currentList));
                    }
                    currentDate = t.targetDate;
                    currentList = new ArrayList<>();
                }
                currentList.add(t);
            }
            if (currentDate != -1 && !currentList.isEmpty()) {
                groups.add(new HistoryGroup(sdf.format(new Date(currentDate)), currentDate, currentList));
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                historyAdapter = new HistoryAdapter(groups, new TaskAdapter.OnTaskListener() {
                    @Override
                    public void onToggleComplete(Task task) {
                        task.completed = !task.completed;
                        new Thread(() -> { taskDao.update(task); loadHistory(); }).start();
                    }

                    @Override
                    public void onDelete(Task task) {
                        new Thread(() -> { taskDao.delete(task); loadHistory(); }).start();
                    }
                });
                recyclerView.setAdapter(historyAdapter);
                emptyText.setVisibility(groups.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    static class HistoryGroup {
        String dateLabel;
        long dateMillis;
        List<Task> tasks;

        HistoryGroup(String dateLabel, long dateMillis, List<Task> tasks) {
            this.dateLabel = dateLabel;
            this.dateMillis = dateMillis;
            this.tasks = tasks;
        }
    }
}
