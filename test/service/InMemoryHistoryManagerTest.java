package service;

import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task1 = new Task("Task 1", "task description 1", 1);

        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void shouldNotAddDuplicateTasks() {
        Task task1 = new Task("Task 1", "task description 1", 1);

        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "task description 1", 1);
        Task task2 = new Task("Task 2", "task description 1", 2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    void shouldKeepLastViewOfDuplicateTasks() {
        Task task1 = new Task("Task 1", "task description 1", 1);
        Task task2 = new Task("Task 2", "task description 1", 2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void shouldHaveEmptyHistoryIfThereWasNoCallsOfGetMethod() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldRemoveTasksFromStartMiddleAndFinishHistoryPositions() {
        Task task1 = new Task("Task 1", "task description 1", 1);
        Task task2 = new Task("Task 2", "task description 2", 2);
        Task task3 = new Task("Task 3", "task description 3", 3);
        Task task4 = new Task("Task 4", "task description 4", 4);
        Task task5 = new Task("Task 5", "task description 5", 5);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);
        historyManager.add(task5);

        historyManager.remove(task1.getId());
        assertNotEquals(task1, historyManager.getHistory().get(0));

        historyManager.remove(task5.getId());
        assertNotEquals(task5, historyManager.getHistory().get(2));

        historyManager.remove(task3.getId());
        assertNotEquals(task3, historyManager.getHistory().get(1));
    }
}
