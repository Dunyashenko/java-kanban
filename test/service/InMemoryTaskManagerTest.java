package service;

import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.HistoryManager;
import com.yandex.task_tracker.service.InMemoryHistoryManager;
import com.yandex.task_tracker.service.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    public void managerShouldReturnTaskSubtaskAndEpicByIdAndWriteToHistory() {
        InMemoryTaskManager manager = createTaskManager();

        Task task1 = new Task("Task 1", "task description 1", null);
        manager.createTask(task1);
        Task task = manager.getTaskById(1000);
        assertNotNull(task);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        manager.createEpic(epic1);
        Epic epic = manager.getEpicById(1001);
        assertNotNull(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(2), Duration.ofMinutes(60), epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        Subtask subtask = manager.getSubtaskById(1003);
        assertNotNull(subtask);

        ArrayList<Task> history = manager.historyManager.getHistory();
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subtask.getId(), history.get(2).getId());
    }

    @Test
    public void tasksWithProvidedIdAndGeneratedIdShouldNotConflict() {
        InMemoryTaskManager manager = createTaskManager();
        Task task1 = new Task("Task 1", "task description 1", null);
        Task task2 = new Task("Task 2", "task description 2", 1000);

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> tasks = manager.getAllTasks();

        assertNotEquals(tasks.get(0).getId(), tasks.get(1).getId());

    }

    @Test
    public void allFieldsOfTaskExceptIdShouldNotChangeAfterCreatingInManager() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = new Task("Task 1", "task description 1", null);
        manager.createTask(task);
        Task taskFromManager = manager.getTaskById(1000);

        assertEquals(task.getName(), taskFromManager.getName());
        assertEquals(task.getDescription(), taskFromManager.getDescription());
        assertEquals(task.getStatus(), taskFromManager.getStatus());
        assertEquals(task.getName(), taskFromManager.getName());
    }

    @Test
    public void shouldPreserveTaskStateInHistoryAfterOriginalTaskIsModified() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task original = new Task("Original", "Original Description", null);
        historyManager.add(original);

        original.setName("Modified");
        original.setDescription("Modified Description");
        original.setStatus(Status.IN_PROGRESS);

        Task fromHistory = historyManager.getHistory().get(0);

        assertEquals("Original", fromHistory.getName());
        assertEquals("Original Description", fromHistory.getDescription());
        assertEquals(Status.NEW, fromHistory.getStatus());
    }

    @Test
    public void epicShouldNotContainIrrelevantSubtasks() {
        InMemoryTaskManager manager = createTaskManager();
        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(2), Duration.ofMinutes(60), epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic = manager.getEpicById(1000);
        List<Subtask> subtasks = epic.getSubtasks();

        manager.deleteSubtaskById(1002);

        boolean isSubtaskDeletedFromEpic = true;
        for (Subtask subtask : subtasks) {
            if (subtask.getId().equals(1002)) {
                isSubtaskDeletedFromEpic = false;
                break;
            }
        }
        assertTrue(isSubtaskDeletedFromEpic);
    }

    @Test
    public void shouldNotTaskUpdatingAffectStateOfTaskInManager() {
        InMemoryTaskManager manager = createTaskManager();
        Task task = new Task("Original Name", "Original Desc", null);
        manager.createTask(task);
        int taskId = task.getId();

        Task fromManager = manager.getTaskById(taskId);
        fromManager.setName("Changed Externally");

        Task sameFromManager = manager.getTaskById(taskId);
        assertEquals("Original Name", sameFromManager.getName());
    }

    @Test
    public void shouldSaveTasksInPrioritizedOrder() {
        InMemoryTaskManager manager = createTaskManager();

        Task task1 = new Task("Task 1", "task description 1", 1000, LocalDateTime.now().plusDays(2),
                Duration.ofMinutes(60));
        manager.createTask(task1);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now(), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(60), epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(subtask1, prioritizedTasks.get(0));
        assertEquals(subtask2, prioritizedTasks.get(1));
        assertEquals(task1, prioritizedTasks.get(2));
    }

}