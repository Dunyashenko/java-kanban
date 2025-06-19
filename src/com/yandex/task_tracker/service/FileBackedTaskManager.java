package com.yandex.task_tracker.service;

import com.yandex.task_tracker.exceptions.ManagerSaveException;
import com.yandex.task_tracker.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File tasksFile;
    public FileBackedTaskManager(File file) {
        this.tasksFile = file;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(tasksFile, StandardCharsets.UTF_8)) {

            writer.write("id,type,name,status,description,epic" + "\n");

            for (Task task : getAllTasks()) {
                writer.write(task.toString() + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(epic.toString() + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(subtask.toString() + "\n");
            }

        } catch (FileNotFoundException exception) {
            throw new ManagerSaveException("Файл не найден" + tasksFile.getAbsolutePath(), exception);
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка сохранения файла", exception);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileManager = new FileBackedTaskManager(file);

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8);
        BufferedReader buffer = new BufferedReader(reader)) {
            while (buffer.ready()) {
                String line = buffer.readLine();
                if ((!line.isBlank() || !line.isEmpty())
                        && !line.equals("id,type,name,status,description,epic")) {
                    Task task = fileManager.fromString(line);

                    switch (checkTaskType(line)) {
                        case TASK -> fileManager.addTask(task);
                        case EPIC -> fileManager.addEpic((Epic) task);
                        case SUBTASK -> fileManager.addSubtask((Subtask) task);
                    }
                }
            }
        } catch (FileNotFoundException exception) {
            throw new ManagerSaveException("Файл не найден" + fileManager.tasksFile.getAbsolutePath(), exception);
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка чтения файла", exception);
        }
        return fileManager;
    }

    public Task fromString(String value) {
        Type taskType = checkTaskType(value);
        String[] taskDetails = value.split(",");

        String name = taskDetails[2];
        String description = taskDetails[4];
        int id = Integer.parseInt(taskDetails[0]);
        Status status = Status.fromString(taskDetails[3]);

        return switch (taskType) {
            case TASK -> new Task(name, description, id, status);
            case SUBTASK -> new Subtask(name, description, id, status, Integer.parseInt(taskDetails[5]));
            case EPIC -> new Epic(name, description, id, status);
        };
    }

    private static Type checkTaskType(String value) {
        String[] taskDetails = value.split(",");
        if (taskDetails[1].equals(Type.TASK.name())) {
            return Type.TASK;
        } else if (taskDetails[1].equals(Type.SUBTASK.name())) {
            return Type.SUBTASK;
        } else {
            return Type.EPIC;
        }
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    public void addTask(Task task) {
        super.createTask(task);
    }

    public void addEpic(Epic epic) {
        super.createEpic(epic);
    }

    public void addSubtask(Subtask subtask) {
        super.createSubtask(subtask);
    }


}
