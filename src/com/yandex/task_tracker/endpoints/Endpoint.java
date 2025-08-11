package com.yandex.task_tracker.endpoints;

import java.util.Arrays;
import java.util.Optional;

public enum Endpoint {

    GET_TASKS("/tasks", "GET"),
    GET_TASK_BY_ID("/tasks/{id}", "GET"),
    POST_TASK("/tasks", "POST"),
    DELETE_TASK_BY_ID("/tasks/{id}", "DELETE"),
    GET_SUBTASKS("/subtasks", "GET"),
    GET_SUBTASK_BY_ID("/subtasks/{id}", "GET"),
    POST_SUBTASK("/subtasks", "POST"),
    DELETE_SUBTASK_BY_ID("/subtasks/{id}", "DELETE"),
    GET_EPICS("/epics", "GET"),
    GET_EPIC_BY_ID("/epics/{id}", "GET"),
    GET_EPIC_SUBTASKS("/epics/{id}/subtasks", "GET"),
    POST_EPIC("/epics", "POST"),
    DELETE_EPIC_BY_ID("/epics/{id}", "DELETE"),
    GET_HISTORY("/history", "GET"),
    GET_PRIORITIZED_TASKS("/prioritized", "GET");

    private final String path;
    private final String method;

    Endpoint(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public static Optional<Endpoint> getEndpointByPathAndMethod(String path, String method) {
        String[] pathsArray = path.split("/");
        StringBuilder pathBuilder = new StringBuilder("/");
        if (pathsArray.length > 1) {
            pathBuilder.append(pathsArray[1]);
        } if (pathsArray.length > 2) {
            pathBuilder.append("/").append("{id}");
        } if (pathsArray.length > 3) {
            pathBuilder.append("/").append(pathsArray[3]);
        }

        return Arrays.stream(Endpoint.values())
                .filter(endpoint -> endpoint.path.contentEquals(pathBuilder) && endpoint.method.equals(method))
                .findFirst();
    }
}
