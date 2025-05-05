package test.model;

import com.yandex.task_tracker.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    public void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task A", "Description A", 1);
        Task task2 = new Task("Task B", "Description B", 1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

}