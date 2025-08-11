package com.yandex.task_tracker.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.task_tracker.endpoints.Endpoint;
import com.yandex.task_tracker.exceptions.ManagerSaveException;
import com.yandex.task_tracker.exceptions.NotFoundException;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Optional<Endpoint> endpoint = Endpoint.getEndpointByPathAndMethod(path, method);

        if (endpoint.isPresent()) {
            switch (endpoint.get()) {
                case Endpoint.GET_EPICS -> handleGetEpics(exchange);
                case Endpoint.GET_EPIC_BY_ID -> handleGetEpicById(exchange);
                case Endpoint.GET_EPIC_SUBTASKS -> handleGetEpicSubtasks(exchange);
                case Endpoint.POST_EPIC -> handlePostEpic(exchange);
                case Endpoint.DELETE_EPIC_BY_ID -> handleDeleteEpicById(exchange);
            }
        } else {
            sendBaseResponse(exchange, "There is no such an endpoint. Check the address and try again", 404);
        }

    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        Gson gson = getGsonConfig();
        String response = gson.toJson(manager.getAllEpics());

        sendBaseResponse(exchange, response, 200);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> optEpicId = getEpicIdFromPath(exchange);
        if (optEpicId.isPresent()) {
            try {
                Gson gson = getGsonConfig();
                Epic epic = manager.getEpicById(optEpicId.get());
                sendBaseResponse(exchange, gson.toJson(epic), 200);
            } catch (NotFoundException ex) {
                sendNotFound(exchange, ex.getMessage());
            }
        } else {
            sendIncorrectIdFormat(exchange);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> optEpicId = getEpicIdFromPath(exchange);
        if (optEpicId.isPresent()) {
            try {
                Gson gson = getGsonConfig();
                Epic epic = manager.getEpicById(optEpicId.get());
                String response = gson.toJson(epic.getSubtasks());
                sendBaseResponse(exchange, response, 200);
            } catch (NotFoundException ex) {
                sendNotFound(exchange, ex.getMessage());
            }
        } else {
            sendIncorrectIdFormat(exchange);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        if (!headers.getFirst("Content-Type").equals("application/json")) {
            sendBaseResponse(exchange, "Request body should be in JSON format", 406);
            return;
        }
        try {
            Epic epic = parseEpic(exchange.getRequestBody());
            if (epic.getId() == null) {
                manager.createEpic(epic);
                sendBaseResponse(exchange, "", 201);
            } else {
                manager.updateEpic(epic);
                sendBaseResponse(exchange, "", 201);
            }
        } catch (ManagerSaveException ex) {
            sendInternalServerError(exchange, ex.getMessage());
        }
    }

    private void handleDeleteEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> optEpicId = getEpicIdFromPath(exchange);
        if (optEpicId.isPresent()) {
            try {
                manager.deleteEpicById(optEpicId.get());
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

    private Optional<Integer> getEpicIdFromPath(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    private Epic parseEpic(InputStream bodyInputStream) throws IOException {
        Gson gson = getGsonConfig();
        String body = new String(bodyInputStream.readAllBytes(), DEFAULT_CHARSET);

        return gson.fromJson(body, Epic.class);
    }
}
