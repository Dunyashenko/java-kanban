package com.yandex.task_tracker.service;

import com.yandex.task_tracker.model.Task;

import java.util.ArrayList;

public interface HistoryManager {

    void add(Task task);

    ArrayList<Task> getHistory();
}
