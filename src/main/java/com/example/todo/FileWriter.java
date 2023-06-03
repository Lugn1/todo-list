package com.example.todo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class FileWriter {
    private final String fileName;

    public FileWriter(String fileName) {
        this.fileName = fileName;
    }

    public void writeTasksToFile(List<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(fileName))) {
            for (Task task : tasks) {
                writer.write(task.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
