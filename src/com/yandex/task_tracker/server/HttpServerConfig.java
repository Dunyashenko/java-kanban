package com.yandex.task_tracker.server;

import com.sun.net.httpserver.HttpServer;
import com.yandex.task_tracker.handlers.*;
import com.yandex.task_tracker.service.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerConfig {

    private InMemoryTaskManager manager;
    private HttpServer server;
    private final static int PORT = 8080;

    public HttpServerConfig(InMemoryTaskManager manager) {
        this.manager = manager;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/tasks", new TasksHandler(manager));
            server.createContext("/epics", new EpicsHandler(manager));
            server.createContext("/subtasks", new SubtasksHandler(manager));
            server.createContext("/history", new HistoryHandler(manager.historyManager));
            server.createContext("/prioritized", new PrioritizedHandler(manager));
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        server.stop(0);
    }
}
