package com.yandex.task_tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, Integer id, Status status, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, id, status, startTime, duration);
        if (!((Integer) epicId).equals(id)) {
            this.epicId = epicId;
        }
    }

    public Subtask(String name, String description, Integer id, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, id, startTime, duration);
        if (!((Integer) epicId).equals(id)) {
            this.epicId = epicId;
        }
    }

    public Subtask(Subtask subtask) {
        super(subtask.getName(), subtask.getDescription(), subtask.getId(), subtask.getStatus(), subtask.getStartTime(), subtask.getDuration());
        this.epicId = subtask.getEpicId();
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (this.epicId == this.getId()) {
            return;
        }
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%d,%s,%d",
                getId(),
                Type.SUBTASK.name(),
                getName(),
                getStatus(),
                getDescription(),
                getStartTime() != null ? getStartTime().format(DATE_TIME_FORMATTER) : "null",
                getDuration() != null ? getDuration().toMinutes() : 0,
                getEndTime() != null ? getEndTime().format(DATE_TIME_FORMATTER) : "null",
                epicId
        );
    }


}