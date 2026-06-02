package com.example.hellofirst.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellofirst.R;
import com.example.hellofirst.adapter.TaskAdapter;
import com.example.hellofirst.data.Task;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<HistoryFragment.HistoryGroup> groups;
    private final TaskAdapter.OnTaskListener listener;

    public HistoryAdapter(List<HistoryFragment.HistoryGroup> groups, TaskAdapter.OnTaskListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryFragment.HistoryGroup group = groups.get(position);
        holder.dateHeader.setText(group.dateLabel);

        int completed = 0;
        for (Task t : group.tasks) {
            if (t.completed) completed++;
        }
        holder.summary.setText(completed + "/" + group.tasks.size() + " 完成");

        TaskAdapter taskAdapter = new TaskAdapter(group.tasks, false, listener);
        holder.taskList.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.taskList.setAdapter(taskAdapter);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateHeader, summary;
        RecyclerView taskList;

        ViewHolder(View itemView) {
            super(itemView);
            dateHeader = itemView.findViewById(R.id.historyDateHeader);
            summary = itemView.findViewById(R.id.historySummary);
            taskList = itemView.findViewById(R.id.historyTaskList);
        }
    }
}
