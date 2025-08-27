package com.yandex.task_tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {

    private ArrayList<Subtask> subtasks;
    private LocalDateTime endTime;

    public Epic(String name, String description, Integer id, Status status, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(name, description, id, status, startTime, duration);
        this.subtasks = new ArrayList<>();
        this.endTime = endTime;
    }

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
        super(epic.getName(), epic.getDescription(), epic.getId(), epic.getStatus(), epic.getStartTime(), epic.getDuration());
        this.subtasks = epic.getSubtasks();
        this.endTime = epic.getEndTime();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public void updateDynamicFields() {
        epicStatusMonitoring();
        epicStartAndEndTimeMonitoring();
        epicDurationMonitoring();
    }

    private void epicStatusMonitoring() {
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

    private void epicStartAndEndTimeMonitoring() {
        Optional<Subtask> prioritisedSubtask = subtasks.stream()
                .min(Comparator.comparing(Task::getStartTime));

        prioritisedSubtask.ifPresentOrElse(
                subtask -> this.setAutoStartTime(subtask.getStartTime()),
                () -> this.setAutoStartTime(null)
        );

        Optional<Subtask> lessPrioritisedSubtask = subtasks.stream()
                .max(Comparator.comparing(Task::getEndTime));

        lessPrioritisedSubtask.ifPresentOrElse(
                subtask -> this.setEndTime(subtask.getEndTime()),
                () -> this.setEndTime(null)
        );
    }

    private void epicDurationMonitoring() {
        Duration epicDuration = subtasks.stream()
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        this.setAutoDuration(epicDuration);
    }

    @Override
    public void setStatus(Status status) {
        System.out.println("Статус эпика установить невозможно. Он рассчитывается автоматически исходя из статусов его подзадач");
    }

    private void setAutoStatus(Status status) {
        super.setStatus(status);
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        System.out.println("Начало выполнения эпика установить невозможно. Он рассчитывается автоматически исходя из его подзадач");
    }

    private void setAutoStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
    }

    @Override
    public void setDuration(Duration duration) {
        System.out.println("Продолжение эпика установить невозможно. Он рассчитывается автоматически исходя из его подзадач");
    }

    private void setAutoDuration(Duration duration) {
        super.setDuration(duration);
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                getId(),
                Type.EPIC.name(),
                getName(),
                getStatus(),
                getDescription(),
                getStartTime() != null ? getStartTime().format(DATE_TIME_FORMATTER) : "null",
                getDuration() != null ? getDuration().toMinutes() : 0,
                getEndTime() != null ? getEndTime().format(DATE_TIME_FORMATTER) : "null"
        );
    }


}