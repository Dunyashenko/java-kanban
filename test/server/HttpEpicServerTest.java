package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.server.HttpServerConfig;
import com.yandex.task_tracker.service.InMemoryTaskManager;
import com.yandex.task_tracker.service.TaskManager;
import com.yandex.task_tracker.utils.DurationAdapter;
import com.yandex.task_tracker.utils.LocalDateTimeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpEpicServerTest {

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

    @Test
    public void epicShouldBeCreatedViaEndpointAndSavedInManager() throws IOException, InterruptedException {
        Epic epic = new Epic("Task 1", "Description 1", null, new ArrayList<>());
        String epicRequest = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/epics");

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicRequest))
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());

        Epic epicFromManager = manager.getEpicById(1000);
        assertEquals(epic.getName(), epicFromManager.getName());

    }

    @ParameterizedTest
    @MethodSource("getEpicByIdEndpoint")
    public void epicShouldBeGottenFromEndpoint(
            int statusCode,
            int id,
            String assertString
    ) throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1", null, new ArrayList<>());
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/epics/" + id);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            JsonElement jsonElement = JsonParser.parseString(response.body());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String name = jsonObject.get("name").getAsString();
                assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
                assertEquals(assertString, name);
            }
        } else {
            assertEquals(statusCode, response.statusCode());
            assertEquals(assertString, response.body());
        }
    }

    @ParameterizedTest
    @MethodSource("deleteEpicByIdEndpoint")
    public void epicShouldBeDeletedViaEndpoint(
            int statusCode,
            int id,
            String assertString
    ) throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1", null, new ArrayList<>());
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/epics/" + id);

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (assertString.equals("Deleted")) {
            assertEquals(statusCode, response.statusCode());
            assertTrue(manager.getAllEpics().isEmpty());
        } else {
            assertEquals(statusCode, response.statusCode());
            assertEquals(assertString, response.body());
        }
    }

    @Test
    public void epicSubtasksShouldBeReturnedViaEndpoint() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1", null, new ArrayList<>());
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", null, LocalDateTime.now(), Duration.ofMinutes(90), 1000);
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", null, LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), 1000);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/epics/" + 1000 + "/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>(){}.getType());
        assertEquals(2, subtasks.size());
    }

    private static Stream<Arguments> getEpicByIdEndpoint() {
        return Stream.of(
                Arguments.of(HttpURLConnection.HTTP_OK, 1000, "Epic 1"),
                Arguments.of(HttpURLConnection.HTTP_NOT_FOUND, 1001, "Epic with id " + 1001 + " is not found")
        );
    }

    private static Stream<Arguments> deleteEpicByIdEndpoint() {
        return Stream.of(
                Arguments.of(HttpURLConnection.HTTP_CREATED, 1000, "Deleted"),
                Arguments.of(HttpURLConnection.HTTP_NOT_FOUND, 1001, "Epic with id " + 1001 + " is not found")
        );
    }
}
