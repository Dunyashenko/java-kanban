package server;

import com.google.gson.*;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Task;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {

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
    @MethodSource("postTaskEndpoint")
    public void taskShouldBeCreatedViaEndpointAndSavedInManager(
            int statusCode,
            String statusDescription,
            LocalDateTime time
    ) throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", null, Status.NEW, time, Duration.ofMinutes(90));
        String taskRequest1 = gson.toJson(task1);
        Task task2 = new Task("Task 1", "Description 1", null, Status.NEW, LocalDateTime.now(), Duration.ofMinutes(90));
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/tasks");

        switch (statusDescription) {
            case "Created" -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(taskRequest1))
                        .uri(url)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(statusCode, response.statusCode());

                Task taskFromManager = manager.getTaskById(1000);
                assertEquals(task1.getName(), taskFromManager.getName());
            }
            case "TimeOverlap" -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(taskRequest1))
                        .uri(url)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(statusCode, response.statusCode());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getTaskByIdEndpoint")
    public void taskShouldBeGottenFromEndpoint(
            int statusCode,
            int id,
            String assertString
    ) throws IOException, InterruptedException {
        Task task = new Task(assertString, "Description 1", null, Status.NEW, LocalDateTime.now(), Duration.ofMinutes(90));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/tasks/" + id);

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
    @MethodSource("deleteTaskByIdEndpoint")
    public void taskShouldBeDeletedViaEndpoint(
            int statusCode,
            int id,
            String assertString
    ) throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description 1", null, Status.NEW, LocalDateTime.now(), Duration.ofMinutes(90));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/tasks/" + id);

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (assertString.equals("Deleted")) {
            assertEquals(statusCode, response.statusCode());
            assertTrue(manager.getAllTasks().isEmpty());
        } else {
            assertEquals(statusCode, response.statusCode());
            assertEquals(assertString, response.body());
        }
    }

    private static Stream<Arguments> postTaskEndpoint() {
        return Stream.of(
                Arguments.of(201, "Created", LocalDateTime.now().plusDays(1)),
                Arguments.of(406, "TimeOverlap", LocalDateTime.now())
        );
    }

    private static Stream<Arguments> getTaskByIdEndpoint() {
        return Stream.of(
                Arguments.of(200, 1000, "Task 1"),
                Arguments.of(404, 1001, "Task with id " + 1001 + " is not found")
        );
    }

    private static Stream<Arguments> deleteTaskByIdEndpoint() {
        return Stream.of(
                Arguments.of(201, 1000, "Deleted"),
                Arguments.of(404, 1001, "Task with id " + 1001 + " is not found")
        );
    }

}
