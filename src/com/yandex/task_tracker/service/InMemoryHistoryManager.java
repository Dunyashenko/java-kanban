package com.yandex.task_tracker.service;

import com.yandex.task_tracker.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_SIZE = 10;
    private final ArrayList<Task> history = new ArrayList<>(MAX_SIZE);

    @Override
    public void add(Task task) {
        history.add(new Task(task));
        if (history.size() > MAX_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
