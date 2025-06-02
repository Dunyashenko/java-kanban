package com.yandex.task_tracker.service;

import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Subtask;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    public final HistoryManager historyManager;
    private int id = 1000;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    public void generateId(Task task) {
        if (task.getId() != null) {
            if (tasks.containsKey(task.getId())) {
                task.setId(id++);
            } else if (subtasks.containsKey(task.getId())) {
                task.setId(id++);
            } else if (epics.containsKey(task.getId())) {
                task.setId(id++);
            }
        } else {
            task.setId(id++);
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtasks();
        }
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        deleteAllSubtasks();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return new Task(task);
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return new Subtask(subtask);
        }
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return new Epic(epic);
        }
        return null;
    }

    @Override
    public void createTask(Task task) {
        generateId(task);
        Task copy = new Task(task);
        tasks.put(copy.getId(), task);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        generateId(subtask);
        Subtask copy = new Subtask(subtask);
        epic.addSubtask(copy);
        epic.epicStatusMonitoring();
        subtasks.put(copy.getId(), subtask);
    }

    @Override
    public void createEpic(Epic epic) {
        generateId(epic);
        Epic copy = new Epic(epic);
        epics.put(copy.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        epic.updateSubtask(subtask);
        epic.epicStatusMonitoring();
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic == null) {
            return;
        }

        Epic copiedEpic = new Epic(epic);

        for (Subtask subtask : oldEpic.getSubtasks()) {
            subtasks.remove(subtask.getId());
        }
        epics.put(epic.getId(), copiedEpic);
        for (Subtask subtask : copiedEpic.getSubtasks()) {
            subtasks.put(subtask.getId(), subtask);
        }
        epics.get(epic.getId()).epicStatusMonitoring();
    }

    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        epic.deleteSubtaskById(subtask.getId());
        epic.epicStatusMonitoring();
        historyManager.remove(id);
        subtasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        for (Subtask subtask : epic.getSubtasks()) {
            historyManager.remove(subtask.getId());
            subtasks.remove(subtask.getId());
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public ArrayList<Subtask> getAllSubtasksOfEpicById(int id) {
        return epics.get(id).getSubtasks();
    }


}