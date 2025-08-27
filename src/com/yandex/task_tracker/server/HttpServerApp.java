package com.yandex.task_tracker.server;

import com.yandex.task_tracker.service.InMemoryTaskManager;

public class HttpServerApp {
    public static void main(String[] args) {
        HttpServerConfig serverConfig = new HttpServerConfig(new InMemoryTaskManager());
        serverConfig.start();
        System.out.println("Server is running on port 8080");
    }
}
