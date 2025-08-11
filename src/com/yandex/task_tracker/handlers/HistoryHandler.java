package com.yandex.task_tracker.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.task_tracker.endpoints.Endpoint;
import com.yandex.task_tracker.service.HistoryManager;

import java.io.IOException;
import java.util.Optional;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final HistoryManager manager;

    public HistoryHandler(HistoryManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Optional<Endpoint> endpoint = Endpoint.getEndpointByPathAndMethod(path, method);

        if (endpoint.isPresent()) {
            switch (endpoint.get()) {
                case Endpoint.GET_HISTORY -> handleGetHistory(exchange);
            }
        } else {
            sendBaseResponse(exchange, "There is no such an endpoint. Check the address and try again", 404);
        }

    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        Gson gson = getGsonConfig();
        String response = gson.toJson(manager.getHistory());

        sendBaseResponse(exchange, response, 200);
    }
}
