package com.example.hellofirst.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellofirst.R;
import com.example.hellofirst.data.AppDatabase;
import com.example.hellofirst.data.Task;
import com.example.hellofirst.data.TaskDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskDao taskDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskDao = AppDatabase.getInstance(requireContext()).taskDao();
        recyclerView = view.findViewById(R.id.weekRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadWeek();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWeek();
    }

    public void loadWeek() {
        new Thread(() -> {
            if (!isAdded()) return;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            long mondayStart = getStartOfDay(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 6);
            long sundayEnd = getEndOfDay(cal.getTimeInMillis());

            List<Task> weekTasks = taskDao.getTasksInRange(mondayStart, sundayEnd);
            List<WeekDay> days = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());

            cal.setTimeInMillis(mondayStart);
            for (int i = 0; i < 7; i++) {
                String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
                long dayStart = getStartOfDay(cal.getTimeInMillis());
                long dayEnd = getEndOfDay(cal.getTimeInMillis());
                List<Task> dayTasks = new ArrayList<>();
                for (Task t : weekTasks) {
                    if (t.targetDate >= dayStart && t.targetDate <= dayEnd) {
                        dayTasks.add(t);
                    }
                }
                days.add(new WeekDay(dayNames[i], sdf.format(new Date(dayStart)), dayStart, dayTasks));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                WeekDayAdapter adapter = new WeekDayAdapter(days, day -> {
                    if (!isAdded()) return;
                    DayTasksDialog dialog = DayTasksDialog.newInstance(day.dateMillis, day.label + " " + day.dateStr);
                    dialog.show(getChildFragmentManager(), "DayTasks");
                });
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    static long getStartOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    static long getEndOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    static class WeekDay {
        String label;
        String dateStr;
        long dateMillis;
        List<Task> tasks;

        WeekDay(String label, String dateStr, long dateMillis, List<Task> tasks) {
            this.label = label;
            this.dateStr = dateStr;
            this.dateMillis = dateMillis;
            this.tasks = tasks;
        }
    }
}
