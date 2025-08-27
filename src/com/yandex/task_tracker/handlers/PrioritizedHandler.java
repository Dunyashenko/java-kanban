package com.yandex.task_tracker.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.task_tracker.endpoints.Endpoint;
import com.yandex.task_tracker.service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Optional<Endpoint> endpoint = Endpoint.getEndpointByPathAndMethod(path, method);

        if (endpoint.isPresent()) {
            switch (endpoint.get()) {
                case Endpoint.GET_PRIORITIZED_TASKS -> handleGetPrioritizedTasks(exchange);
            }
        } else {
            sendBaseResponse(exchange, "There is no such an endpoint. Check the address and try again", HttpURLConnection.HTTP_NOT_FOUND);
        }

    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        Gson gson = getGsonConfig();
        String response = gson.toJson(manager.getPrioritizedTasks());

        sendBaseResponse(exchange, response, HttpURLConnection.HTTP_OK);
    }
}
