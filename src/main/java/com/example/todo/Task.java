package com.example.todo;

import javafx.scene.paint.Color;

public class Task {

    private boolean taskCompleted = false;

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    private String profileKey;

    private String text;
    private int priority;
    private static Color priorityColor;
    public Task(String name, int priority, Color priorityColor, boolean taskCompleted) {
        this.text = name;
        this.priority = priority;
        Task.priorityColor = priorityColor;
        this.taskCompleted = taskCompleted;
        this.profileKey = profileKey;
    }

    public Task() {

    }


    public boolean isTaskCompleted() {
        return taskCompleted;
    }

    public void setTaskCompleted(boolean taskCompleted) {
        this.taskCompleted = taskCompleted;
    }

    public int getPriority() {
        return priority;
    }

    public static Color getPriorityColor() {
        return priorityColor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
