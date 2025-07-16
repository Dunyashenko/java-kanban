package com.yandex.task_tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Comparable<Task> {

    private String name;
    private String description;
    private Integer id;
    private Status status;
    private LocalDateTime startTime;
    private Duration duration;
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Task(String name, String description, Integer id, Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, Integer id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = Status.NEW;
    }

    public Task(String name, String description, Integer id, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = Status.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Task task) {
        this.name = task.name;
        this.description = task.description;
        this.id = task.id;
        this.status = task.status;
        this.startTime = task.startTime;
        this.duration = task.duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        return startTime != null ? startTime.plus(duration) : null;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                id,
                Type.TASK.name(),
                name,
                status,
                description,
                startTime != null ? startTime.format(DATE_TIME_FORMATTER) : "null",
                duration != null ? duration.toMinutes() : 0,
                getEndTime() != null ? getEndTime().format(DATE_TIME_FORMATTER) : "null"
                );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Task task) {
        if (this.startTime == null && task.startTime == null) {
            return 0;
        }
        if (this.startTime == null) {
            return 1;
        }
        if (task.startTime == null) {
            return -1;
        }
        return this.startTime.compareTo(task.startTime);
    }

    public boolean hasTimeOverlap(Task task) {
        if ((task.startTime != null && task.duration != null) && (this.startTime != null && this.duration != null)) {
            return task.getEndTime().isAfter(this.startTime) && task.startTime.isBefore(this.getEndTime());
        } else {
            return false;
        }
    }
}