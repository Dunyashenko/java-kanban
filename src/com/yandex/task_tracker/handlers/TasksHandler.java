package com.yandex.task_tracker.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.task_tracker.endpoints.Endpoint;
import com.yandex.task_tracker.exceptions.ManagerSaveException;
import com.yandex.task_tracker.exceptions.NotFoundException;
import com.yandex.task_tracker.exceptions.TimeOverlapException;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Optional<Endpoint> endpoint = Endpoint.getEndpointByPathAndMethod(path, method);

        if (endpoint.isPresent()) {
            switch (endpoint.get()) {
                case Endpoint.GET_TASKS -> handleGetTasks(exchange);
                case Endpoint.GET_TASK_BY_ID -> handleGetTaskById(exchange);
                case Endpoint.POST_TASK -> handlePostTask(exchange);
                case Endpoint.DELETE_TASK_BY_ID -> handleDeleteTaskById(exchange);
            }
        } else {
            sendBaseResponse(exchange, "There is no such an endpoint. Check the address and try again", 404);
        }

    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        Gson gson = getGsonConfig();
        String response = gson.toJson(manager.getAllTasks());

        sendBaseResponse(exchange, response, 200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getTaskIdFromPath(exchange);
        if (optTaskId.isPresent()) {
            try {
                Gson gson = getGsonConfig();
                Task task = manager.getTaskById(optTaskId.get());
                sendBaseResponse(exchange, gson.toJson(task), 200);
            } catch (NotFoundException ex) {
                sendNotFound(exchange, ex.getMessage());
            }
        } else {
            sendIncorrectIdFormat(exchange);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        if (!headers.getFirst("Content-Type").equals("application/json")) {
            sendBaseResponse(exchange, "Request body should be in JSON format", 406);
            return;
        }
        try {
            Task task = parseTask(exchange.getRequestBody());
            if (task.getId() == null) {
                manager.createTask(task);
                sendBaseResponse(exchange, "", 201);
            } else {
                manager.updateTask(task);
                sendBaseResponse(exchange, "", 201);
            }
        } catch (TimeOverlapException ex) {
            sendHasTimeOverlap(exchange, ex.getMessage());
        } catch (ManagerSaveException ex) {
            sendInternalServerError(exchange, ex.getMessage());
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getTaskIdFromPath(exchange);
        if (optTaskId.isPresent()) {
            try {
                manager.deleteTaskById(optTaskId.get());
                sendBaseResponse(exchange, "", 201);
            } catch (NotFoundException ex) {
                sendNotFound(exchange, ex.getMessage());
            } catch (ManagerSaveException ex) {
                sendInternalServerError(exchange, ex.getMessage());
            }
        } else {
            sendIncorrectIdFormat(exchange);
        }
    }

    private Optional<Integer> getTaskIdFromPath(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    private Task parseTask(InputStream bodyInputStream) throws IOException {
        Gson gson = getGsonConfig();
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);

        return gson.fromJson(body, Task.class);
    }
}
