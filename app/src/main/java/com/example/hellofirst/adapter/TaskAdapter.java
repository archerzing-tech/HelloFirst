package com.example.hellofirst.adapter;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellofirst.R;
import com.example.hellofirst.data.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private List<Task> tasks;
    private final boolean showDate;
    private final OnTaskListener listener;

    public interface OnTaskListener {
        void onToggleComplete(Task task);
        void onDelete(Task task);
    }

    public TaskAdapter(List<Task> tasks, boolean showDate, OnTaskListener listener) {
        this.tasks = tasks;
        this.showDate = showDate;
        this.listener = listener;
    }

    public void updateTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        long todayStart = getStartOfDay(System.currentTimeMillis());
        boolean isExpired = task.targetDate < todayStart;

        if (isExpired || task.completed) {
            SpannableString ss = new SpannableString(task.content);
            ss.setSpan(new StrikethroughSpan(), 0, task.content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contentText.setText(ss);
        } else {
            holder.contentText.setText(task.content);
        }

        holder.checkbox.setChecked(task.completed);
        holder.checkbox.setOnClickListener(v -> {
            if (listener != null) listener.onToggleComplete(task);
        });

        if (showDate) {
            holder.dateText.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
            holder.dateText.setText(sdf.format(new Date(task.targetDate)));
        } else {
            holder.dateText.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(task);
        });
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView contentText;
        TextView dateText;
        ImageButton deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.taskCheckbox);
            contentText = itemView.findViewById(R.id.taskContent);
            dateText = itemView.findViewById(R.id.taskDate);
            deleteBtn = itemView.findViewById(R.id.taskDelete);
        }
    }

    public static long getStartOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
