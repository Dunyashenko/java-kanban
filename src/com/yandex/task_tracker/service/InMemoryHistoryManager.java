package com.yandex.task_tracker.service;

import com.yandex.task_tracker.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() < 10) {
            history.add(new Task(task));
        } else {
            history.removeFirst();
            history.add(new Task(task));
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
