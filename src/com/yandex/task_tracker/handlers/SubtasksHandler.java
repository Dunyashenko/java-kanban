package com.yandex.task_tracker.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.task_tracker.endpoints.Endpoint;
import com.yandex.task_tracker.exceptions.ManagerSaveException;
import com.yandex.task_tracker.exceptions.NotFoundException;
import com.yandex.task_tracker.exceptions.TimeOverlapException;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Optional<Endpoint> endpoint = Endpoint.getEndpointByPathAndMethod(path, method);

        if (endpoint.isPresent()) {
            switch (endpoint.get()) {
                case Endpoint.GET_SUBTASKS -> handleGetSubtasks(exchange);
                case Endpoint.GET_SUBTASK_BY_ID -> handleGetSubtaskById(exchange);
                case Endpoint.POST_SUBTASK -> handlePostSubtask(exchange);
                case Endpoint.DELETE_SUBTASK_BY_ID -> handleDeleteSubtaskById(exchange);
            }
        } else {
            sendBaseResponse(exchange, "There is no such an endpoint. Check the address and try again", HttpURLConnection.HTTP_NOT_FOUND);
        }

    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        Gson gson = getGsonConfig();
        String response = gson.toJson(manager.getAllSubtasks());

        sendBaseResponse(exchange, response, HttpURLConnection.HTTP_OK);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> optSubtaskId = getSubtaskIdFromPath(exchange);
        if (optSubtaskId.isPresent()) {
            try {
                Gson gson = getGsonConfig();
                Subtask subtask = manager.getSubtaskById(optSubtaskId.get());
                sendBaseResponse(exchange, gson.toJson(subtask), HttpURLConnection.HTTP_OK);
            } catch (NotFoundException ex) {
                sendNotFound(exchange, ex.getMessage());
            }
        } else {
            sendIncorrectIdFormat(exchange);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        if (!headers.getFirst("Content-Type").equals("application/json")) {
            sendBaseResponse(exchange, "Request body should be in JSON format", HttpURLConnection.HTTP_NOT_ACCEPTABLE);
            return;
        }
        try {
            Subtask subtask = parseSubtask(exchange.getRequestBody());
            if (subtask.getId() == null) {
                manager.createSubtask(subtask);
                sendBaseResponse(exchange, "", HttpURLConnection.HTTP_CREATED);
            } else {
                manager.updateSubtask(subtask);
                sendBaseResponse(exchange, "", HttpURLConnection.HTTP_CREATED);
            }
        } catch (TimeOverlapException ex) {
            sendHasTimeOverlap(exchange, ex.getMessage());
        } catch (ManagerSaveException ex) {
            sendInternalServerError(exchange, ex.getMessage());
        }
    }

    private void handleDeleteSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> optSubtaskId = getSubtaskIdFromPath(exchange);
        if (optSubtaskId.isPresent()) {
            try {
                manager.deleteSubtaskById(optSubtaskId.get());
                sendBaseResponse(exchange, "", HttpURLConnection.HTTP_CREATED);
            } catch (NotFoundException ex) {
                sendNotFound(exchange, ex.getMessage());
            } catch (ManagerSaveException ex) {
                sendInternalServerError(exchange, ex.getMessage());
            }
        } else {
            sendIncorrectIdFormat(exchange);
        }
    }

    private Optional<Integer> getSubtaskIdFromPath(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    private Subtask parseSubtask(InputStream bodyInputStream) throws IOException {
        Gson gson = getGsonConfig();
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);

        return gson.fromJson(body, Subtask.class);
    }
}
