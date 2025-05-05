package test.model;

import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Subtask;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void subtasksWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Task A", "Description A", 1, new ArrayList<>());
        Epic epic2 = new Epic("Task B", "Description B", 1, new ArrayList<>());

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
    }

    @Test
    public void epicShouldNotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Main epic", 1, new ArrayList<>());
        Subtask fakeSelfSubtask = new Subtask("Fake", "Fake subtask", 1, 1);
        epic.addSubtask(fakeSelfSubtask);

        assertTrue(epic.getSubtasks().isEmpty());

    }

}