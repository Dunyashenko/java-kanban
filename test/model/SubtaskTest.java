package model;

import com.yandex.task_tracker.model.Subtask;
import org.junit.jupiter.api.Test;


import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    public void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Task A", "Description A", 1, LocalDateTime.now(), Duration.ofMinutes(90), 123);
        Subtask subtask2 = new Subtask("Task B", "Description B", 1, LocalDateTime.now().plusDays(1), Duration.ofMinutes(60), 123);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    public void subtaskShouldNotBeItselfAsEpic() {
        Subtask fakeEpic = new Subtask("Fake Epic", "Description A", 1, LocalDateTime.now(), Duration.ofMinutes(90),1);

        assertEquals(0, fakeEpic.getEpicId());
    }

}