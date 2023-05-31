package com.example.todo;

import javafx.scene.paint.Color;

public class Task {

    private String text;
    private String priority;
    private static Color priorityColor;

    public Task(String name, String priority, Color priorityColor) {
        this.text = name;
        this.priority = priority;
        Task.priorityColor = priorityColor;
    }

    public Task() {

    }

    public String getPriority() {
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

    public void setPriority(String priority) {
        this.priority = priority;
    }

}
