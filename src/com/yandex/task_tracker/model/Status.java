package com.yandex.task_tracker.model;

public enum Status {
    NEW, IN_PROGRESS, DONE;

    public static Status fromString(String value) {
        return Status.valueOf(value.toUpperCase());
    }
}