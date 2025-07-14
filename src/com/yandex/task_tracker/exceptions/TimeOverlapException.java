package com.yandex.task_tracker.exceptions;

public class TimeOverlapException extends RuntimeException {

    public TimeOverlapException(String message) {
        super(message);
    }
}
