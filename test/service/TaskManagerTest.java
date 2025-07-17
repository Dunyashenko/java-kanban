package service;

import com.yandex.task_tracker.exceptions.TimeOverlapException;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setup() {
        taskManager = createTaskManager();
    }

    @Test
    public void shouldCreateALlTypeOfEntities() {
        Task task1 = new Task("Task 1", "task description 1", null);
        taskManager.createTask(task1);
        Task task = taskManager.getTaskById(1000);
        assertNotNull(task);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        taskManager.createEpic(epic1);
        Epic epic = taskManager.getEpicById(1001);
        assertNotNull(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(2), Duration.ofMinutes(60), epic1.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        Subtask subtask = taskManager.getSubtaskById(1003);
        assertNotNull(subtask);
    }

    @Test
    public void shouldDeleteAllTypeOfEntities() {
        Task task1 = new Task("Task 1", "task description 1", null);
        taskManager.createTask(task1);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(2), Duration.ofMinutes(60), epic1.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertNotNull(taskManager.getAllTasks());
        assertNotNull(taskManager.getAllEpics());
        assertNotNull(taskManager.getAllSubtasks());

        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks();
        taskManager.deleteAllEpics();

        assertTrue(taskManager.getAllTasks().isEmpty());
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    public void shouldNotCreateSubtaskWithoutCreatedEpic() {
        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", 1000,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), 1);

        taskManager.createSubtask(subtask1);

        assertNull(taskManager.getSubtaskById(1000));

    }

    @Test
    public void shouldNotCreateTaskIfTimeExecutionOverlaps() {
        Task task1 = new Task("Task 1", "task description 1", 1000, LocalDateTime.now(), Duration.ofMinutes(90));
        Task task2 = new Task("Task 2", "task description 2", 1001, LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(90));
        taskManager.createTask(task1);

        assertThrows(TimeOverlapException.class, () -> {
            taskManager.createTask(task2);
        });
    }


    @ParameterizedTest
    @MethodSource("epicStatusMonitoringProvider")
    public void shouldUpdateStatusOfEpicBasedOnStatusOfItSubtasks(
            Status subtaskStatus1,
            Status subtaskStatus2,
            Status epicStatus
    ) {
        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(2), Duration.ofMinutes(60), epic1.getId());

        subtask1.setStatus(subtaskStatus1);
        subtask2.setStatus(subtaskStatus2);

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(epicStatus, epic1.getStatus());
    }

    private static Stream<Arguments> epicStatusMonitoringProvider() {
        return Stream.of(
                Arguments.of(Status.NEW, Status.NEW, Status.NEW),
                Arguments.of(Status.DONE, Status.DONE, Status.DONE),
                Arguments.of(Status.NEW, Status.DONE, Status.IN_PROGRESS),
                Arguments.of(Status.IN_PROGRESS, Status.IN_PROGRESS, Status.IN_PROGRESS)
        );
    }


}
