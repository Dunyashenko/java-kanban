package com.yandex.task_tracker.service;

import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.utils.CustomLinkedList;
import com.yandex.task_tracker.utils.CustomLinkedList.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<Task> customLinkedList = new CustomLinkedList<>();
    private final Map<Integer, Node<Task>> history = new HashMap<>();

    @Override
    public void add(Task task) {
        if (history.containsKey(task.getId())) {
            customLinkedList.removeNode(history.get(task.getId()));
        }

        Node<Task> newNode = customLinkedList.linkLast(new Task(task));
        history.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node<Task> node = history.remove(id);
        if (node != null) {
            customLinkedList.removeNode(node);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(customLinkedList.getTasks());
    }
}
