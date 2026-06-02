package com.example.hellofirst;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hellofirst.ui.FutureFragment;
import com.example.hellofirst.ui.HistoryFragment;
import com.example.hellofirst.ui.WeekFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
