package com.yandex.task_tracker.service;

import com.yandex.task_tracker.exceptions.TimeOverlapException;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Subtask;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final TreeSet<Task> tasksByPriority = new TreeSet<>();
    public final HistoryManager historyManager;
    private int id = 1000;
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        tasks.keySet().forEach(id -> {
            historyManager.remove(id);
            tasksByPriority.remove(tasks.get(id));
        });
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        epics.values().forEach(Epic::deleteAllSubtasks);
        subtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            tasksByPriority.remove(subtasks.get(id));
        });
        subtasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
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

        try {
            checkTimeOverlap(task);
        } catch (TimeOverlapException e) {
            System.out.println(e.getMessage());
            return;
        }

        tasks.put(copy.getId(), task);
        if (task.getStartTime() != null) {
            tasksByPriority.add(task);
        }
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        generateId(subtask);
        Subtask copy = new Subtask(subtask);

        try {
            checkTimeOverlap(subtask);
        } catch (TimeOverlapException e) {
            System.out.println(e.getMessage());
            return;
        }

        epic.addSubtask(copy);
        epic.updateDynamicFields();
        subtasks.put(copy.getId(), subtask);
        if (subtask.getStartTime() != null) {
            tasksByPriority.add(subtask);
        }
    }

    @Override
    public void createEpic(Epic epic) {
        generateId(epic);
        Epic copy = new Epic(epic);
        epics.put(copy.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        tasksByPriority.remove(tasks.get(task.getId()));

        try {
            checkTimeOverlap(task);
        } catch (TimeOverlapException e) {
            System.out.println(e.getMessage());
            return;
        }

        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            tasksByPriority.add(task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        tasksByPriority.remove(subtasks.get(subtask.getId()));

        try {
            checkTimeOverlap(subtask);
        } catch (TimeOverlapException e) {
            System.out.println(e.getMessage());
            return;
        }

        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            tasksByPriority.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        epic.updateSubtask(subtask);
        epic.updateDynamicFields();
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic == null) {
            return;
        }
        Epic copiedEpic = new Epic(epic);

        oldEpic.getSubtasks().stream()
                .map(Task::getId)
                .forEach(subtasks::remove);

        epics.put(epic.getId(), copiedEpic);
        copiedEpic.getSubtasks().forEach(subtask -> subtasks.put(subtask.getId(), subtask));
        epics.get(epic.getId()).updateDynamicFields();
    }

    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id);
        tasksByPriority.remove(tasks.get(id));
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
        epic.updateDynamicFields();
        historyManager.remove(id);
        tasksByPriority.remove(subtasks.get(id));
        subtasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        epic.getSubtasks().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            tasksByPriority.remove(subtask);
            subtasks.remove(subtask.getId());
        });
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(tasksByPriority);
    }

    private void checkTimeOverlap(Task task) {
        getPrioritizedTasks().forEach(prioritizedTask -> {
            if (task.hasTimeOverlap(prioritizedTask)) {
                throw new TimeOverlapException(String.format("Запланированное время выполения новой задачи %s " +
                        "пересекается с запланированным временем уже существующей задачи %s", task, prioritizedTask));
            }
        });
    }
}