package com.example.hellofirst.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellofirst.R;

import java.util.List;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.ViewHolder> {
    private final List<WeekFragment.WeekDay> days;
    private final OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(WeekFragment.WeekDay day);
    }

    public WeekDayAdapter(List<WeekFragment.WeekDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeekFragment.WeekDay day = days.get(position);
        holder.dayLabel.setText(day.label);
        holder.dayDate.setText(day.dateStr);

        int taskCount = day.tasks.size();
        long todayStart = WeekFragment.getStartOfDay(System.currentTimeMillis());
        boolean isPast = day.dateMillis < todayStart;

        if (taskCount > 0) {
            holder.taskPreview.setVisibility(View.VISIBLE);
            String preview = day.tasks.get(0).content;
            if (taskCount > 1) {
                preview += " +" + (taskCount - 1);
            }
            holder.taskPreview.setText(preview);
        } else {
            holder.taskPreview.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onDayClick(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayLabel, dayDate, taskPreview;

        ViewHolder(View itemView) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.dayLabel);
            dayDate = itemView.findViewById(R.id.dayDate);
            taskPreview = itemView.findViewById(R.id.taskPreview);
        }
    }
}
