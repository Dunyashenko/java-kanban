package service;

import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.HistoryManager;
import com.yandex.task_tracker.service.InMemoryHistoryManager;
import com.yandex.task_tracker.service.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    public void createItems() {
        manager = new InMemoryTaskManager();
        Task task1 = new Task("Task 1", "task description 1", null);
        Task task2 = new Task("Task 2", "task description 2", 1000);

        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        Epic epic2 = new Epic("Epic 2", "epic description 2", null, new ArrayList<>());

        manager.createEpic(epic1);
        manager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null, epic1.getId());
        Subtask subtask3 = new Subtask("Subtask 3", "subtask description 3", null, epic1.getId());
        Subtask subtask4 = new Subtask("Subtask 4", "subtask description 4", null, epic2.getId());
        Subtask subtask5 = new Subtask("Subtask 5", "subtask description 5", null, epic2.getId());

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);
        manager.createSubtask(subtask4);
        manager.createSubtask(subtask5);
    }

    @Test
    public void managerShouldReturnTaskSubtaskAndEpicByIdAndWriteToHistory() {
        Task task = manager.getTaskById(1000);
        assertNotNull(task);

        Subtask subtask = manager.getSubtaskById(1004);
        assertNotNull(subtask);

        Epic epic = manager.getEpicById(1002);
        assertNotNull(epic);

        ArrayList<Task> history = manager.historyManager.getHistory();
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(subtask.getId(), history.get(1).getId());
        assertEquals(epic.getId(), history.get(2).getId());
    }

    @Test
    public void tasksWithProvidedIdAndGeneratedIdShouldNotConflict() {
        List<Task> tasks = manager.getAllTasks();

        assertNotEquals(tasks.get(0).getId(), tasks.get(1).getId());

    }

    @Test
    public void allFieldsOfTaskExceptIdShouldNotChangeAfterCreatingInManager() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task3 = new Task("Task 1", "task description 1", null);
        manager.createTask(task3);
        Task task = manager.getTaskById(1000);

        assertEquals(task3.getName(), task.getName());
        assertEquals(task3.getDescription(), task.getDescription());
        assertEquals(task3.getStatus(), task.getStatus());
        assertEquals(task3.getName(), task.getName());
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
        Epic epic = manager.getEpicById(1003);
        List<Subtask> subtasks = epic.getSubtasks();

        manager.deleteSubtaskById(1005);

        boolean isSubtaskDeletedFromEpic = true;
        for (Subtask subtask : subtasks) {
            if (subtask.getId().equals(1005)) {
                isSubtaskDeletedFromEpic = false;
                break;
            }
        }
        assertTrue(isSubtaskDeletedFromEpic);
    }

    @Test
    void shouldNotTaskUpdatingAffectStateOfTaskInManager() {
        Task task = new Task("Original Name", "Original Desc", null);
        manager.createTask(task);
        int taskId = task.getId();

        Task fromManager = manager.getTaskById(taskId);
        fromManager.setName("Changed Externally");

        Task sameFromManager = manager.getTaskById(taskId);
        assertEquals("Original Name", sameFromManager.getName());
    }

}