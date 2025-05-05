package com.yandex.task_tracker.model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {

    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description, Integer id, ArrayList<Subtask> subtasks) {
        super(name, description, id);
        if (!subtasks.isEmpty()) {
            for (Subtask subtask : subtasks) {
                if (!Objects.equals(subtask.getId(), this.getId())) {
                    this.subtasks.add(subtask);
                }
            }
        } else {
            this.subtasks = new ArrayList<>();
        }
    }

    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getId());
        this.subtasks = epic.getSubtasks();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void updateSubtask(Subtask newSubtask) {
        for (int i = 0; i < subtasks.size(); i++) {
            if (Objects.equals(subtasks.get(i).getId(), newSubtask.getId())) {
                subtasks.set(i, newSubtask);
                break;
            }
        }
    }

    public void addSubtask(Subtask subtask) {
        if (Objects.equals(subtask.getId(), this.getId())) {
            return;
        }
        subtasks.add(subtask);
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void deleteSubtaskById(int id) {
        subtasks.removeIf(subtask -> subtask.getId() == id);
    }

    public void epicStatusMonitoring() {
        boolean allDone = true;
        boolean allNew = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            this.setAutoStatus(Status.DONE);
        } else if (allNew) {
            this.setAutoStatus(Status.NEW);
        } else {
            this.setAutoStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void setStatus(Status status) {
        System.out.println("Статус эпика установить невозможно. Он рассчитывается автоматически исходя из статусов его подзадач");
    }

    private void setAutoStatus(Status status) {
        super.setStatus(status);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() + '\'' +
                ", description=" + getDescription() + '\'' +
                ", subtasks=" + subtasks +
                '}';
    }


}