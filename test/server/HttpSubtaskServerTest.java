package server;

import com.google.gson.*;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.server.HttpServerConfig;
import com.yandex.task_tracker.service.InMemoryTaskManager;
import com.yandex.task_tracker.service.TaskManager;
import com.yandex.task_tracker.utils.DurationAdapter;
import com.yandex.task_tracker.utils.LocalDateTimeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpSubtaskServerTest {

    private TaskManager manager;
    private HttpServerConfig serverConfig;
    private Gson gson;
    private final static String LOCAL_DOMAIN = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        serverConfig = new HttpServerConfig((InMemoryTaskManager) manager);
        serverConfig.start();
        gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @AfterEach
    void tearDown() {
        serverConfig.stop();
    }

    @ParameterizedTest
    @MethodSource("postSubtaskEndpoint")
    public void subtaskShouldBeCreatedViaEndpointAndSavedInManager(
            int statusCode,
            String statusDescription,
            LocalDateTime time
    ) throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1", null, new ArrayList<>());
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", null, LocalDateTime.now(), Duration.ofMinutes(90), 1000);
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 1", null, time, Duration.ofMinutes(90), 1000);
        String subtaskRequest = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/subtasks");

        switch (statusDescription) {
            case "Created" -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(subtaskRequest))
                        .uri(url)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(statusCode, response.statusCode());

                Subtask subtaskFromManager = manager.getSubtaskById(1002);
                assertEquals(subtask2.getName(), subtaskFromManager.getName());
            }
            case "TimeOverlap" -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(subtaskRequest))
                        .uri(url)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(statusCode, response.statusCode());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getSubtaskByIdEndpoint")
    public void subtaskShouldBeGottenFromEndpoint(
            int statusCode,
            int id,
            String assertString
    ) throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1", null, new ArrayList<>());
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", null, LocalDateTime.now(), Duration.ofMinutes(90), 1000);
        manager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/subtasks/" + id);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String name = jsonObject.get("name").getAsString();
                assertEquals(200, response.statusCode());
                assertEquals(assertString, name);
            }
        } else {
            assertEquals(statusCode, response.statusCode());
            assertEquals(assertString, response.body());
        }
    }

    @ParameterizedTest
    @MethodSource("deleteSubtaskByIdEndpoint")
    public void subtaskShouldBeDeletedViaEndpoint(
            int statusCode,
            int id,
            String assertString
    ) throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1", null, new ArrayList<>());
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", null, LocalDateTime.now(), Duration.ofMinutes(90), 1000);
        manager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/subtasks/" + id);

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (assertString.equals("Deleted")) {
            assertEquals(statusCode, response.statusCode());
            assertTrue(manager.getAllSubtasks().isEmpty());
        } else {
            assertEquals(statusCode, response.statusCode());
            assertEquals(assertString, response.body());
        }
    }

    private static Stream<Arguments> postSubtaskEndpoint() {
        return Stream.of(
                Arguments.of(201, "Created", LocalDateTime.now().plusDays(1)),
                Arguments.of(406, "TimeOverlap", LocalDateTime.now())
        );
    }

    private static Stream<Arguments> getSubtaskByIdEndpoint() {
        return Stream.of(
                Arguments.of(200, 1001, "Subtask 1"),
                Arguments.of(404, 1002, "Subtask with id " + 1002 + " is not found")
        );
    }

    private static Stream<Arguments> deleteSubtaskByIdEndpoint() {
        return Stream.of(
                Arguments.of(201, 1001, "Deleted"),
                Arguments.of(404, 1002, "Subtask with id " + 1002 + " is not found")
        );
    }
}
