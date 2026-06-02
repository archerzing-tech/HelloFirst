package com.example.hellofirst;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hellofirst.data.AppDatabase;
import com.example.hellofirst.data.TaskDao;
import com.example.hellofirst.ui.FutureFragment;
import com.example.hellofirst.ui.HistoryFragment;
import com.example.hellofirst.ui.WeekFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView todayCount, urgentCount;
    private TaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskDao = AppDatabase.getInstance(this).taskDao();

        todayCount = findViewById(R.id.todayCount);
        urgentCount = findViewById(R.id.urgentCount);

        findViewById(R.id.cardToday).setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
        });
        findViewById(R.id.cardUrgent).setOnClickListener(v -> {
            viewPager.setCurrentItem(2);
        });

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new PagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("本周"); break;
                        case 1: tab.setText("将来"); break;
                        case 2: tab.setText("历史记录"); break;
                    }
                }
        ).attach();

        loadHeaderCounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHeaderCounts();
    }

    private void loadHeaderCounts() {
        new Thread(() -> {
            long todayStart = getStartOfDay(System.currentTimeMillis());
            long todayEnd = getEndOfDay(System.currentTimeMillis());
            int today = taskDao.getTasksForDate(todayStart, todayEnd).size();
            int urgent = taskDao.getOverdueTasks(todayStart).size();
            runOnUiThread(() -> {
                todayCount.setText(String.valueOf(today));
                urgentCount.setText(String.valueOf(urgent));
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

    private static class PagerAdapter extends FragmentStateAdapter {
        PagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new WeekFragment();
                case 1: return new FutureFragment();
                case 2: return new HistoryFragment();
                default: return new WeekFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
