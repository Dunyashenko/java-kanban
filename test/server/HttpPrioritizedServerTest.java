package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.server.HttpServerConfig;
import com.yandex.task_tracker.service.InMemoryTaskManager;
import com.yandex.task_tracker.service.TaskManager;
import com.yandex.task_tracker.utils.DurationAdapter;
import com.yandex.task_tracker.utils.LocalDateTimeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpPrioritizedServerTest {

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
    public void prioritizedTasksShouldBeGottenViaEndpoint() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", null, Status.NEW, LocalDateTime.now(), Duration.ofMinutes(90));
        Task task2 = new Task("Task 2", "Description 2", null, Status.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(90));
        Task task3 = new Task("Task 3", "Description 3", null);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(LOCAL_DOMAIN + "/prioritized");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(2, prioritizedTasks.size());
    }

}
