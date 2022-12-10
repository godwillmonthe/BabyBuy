package com.godwill.babybuy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private final List<TaskItem> taskArray;
    private final Context context;
    private final TaskListener taskListener;

    public TaskAdapter(Context context, List<TaskItem> taskArray, TaskListener taskListener) {
        this.context = context;
        this.taskArray = taskArray;
        this.taskListener = taskListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.task_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);

        Picasso.get().load(taskArray.get(position).getTaskImage()).into(holder.taskImage);
        holder.taskTitle.setText(taskArray.get(position).getTaskName());
        holder.taskDate.setText(taskArray.get(position).getTaskDate());
        holder.taskDescription.setText(taskArray.get(position).getTaskDescription());

        holder.itemView.setAnimation(animation);

        //System.out.println("TaskAdapter: onBindViewHolder: taskArray.get(position).getTaskName(): " + taskArray.get(position).getTaskName());

    }

    @Override
    public int getItemCount() {
        return taskArray.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskDate;
        ImageView taskImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            taskImage = itemView.findViewById(R.id.taskImageRecycler);
            taskTitle = itemView.findViewById(R.id.taskTitleRecycler);
            taskDate = itemView.findViewById(R.id.taskDateRecycler);
            taskDescription = itemView.findViewById(R.id.taskDescriptionRecycler);
            itemView.setOnClickListener(v -> taskListener.onTaskClick(getAdapterPosition()));
        }
    }
}
