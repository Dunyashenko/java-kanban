package com.yandex.task_tracker.exceptions;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
