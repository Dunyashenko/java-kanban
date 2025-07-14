package service;

import com.yandex.task_tracker.exceptions.ManagerSaveException;
import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.FileBackedTaskManager;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File temporaryFile = null;
    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            temporaryFile = File.createTempFile("tasksTest", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(temporaryFile);
    }

    @Test
    public void shouldSaveFileWithNoTasks() {
        FileBackedTaskManager fileManager = createTaskManager();

        fileManager.save();

        FileBackedTaskManager newFileManager = FileBackedTaskManager.loadFromFile(temporaryFile);

        assertTrue(newFileManager.getAllTasks().isEmpty());
        assertTrue(newFileManager.getAllEpics().isEmpty());
        assertTrue(newFileManager.getAllSubtasks().isEmpty());
    }

    @Test
    public void shouldSaveToFileAfterTaskCreation() throws IOException {
        FileBackedTaskManager fileManager = createTaskManager();
        Task task1 = new Task("Task 1", "task description 1", 1, LocalDateTime.now(), Duration.ofMinutes(90));

        fileManager.createTask(task1);

        try (FileReader reader = new FileReader(temporaryFile);
            BufferedReader buffer = new BufferedReader(reader)) {

            while (buffer.ready()) {
                String line = buffer.readLine();
                if (!line.equals("id,type,name,status,description,startTime,duration,endTime,epic")) {
                    Task taskFromFile = fileManager.fromString(line);
                    assertEquals(task1, taskFromFile);
                }
            }
        }
    }

    @Test
    public void shouldSaveToFileAndLoadFromFileAllTypesOfEntities() {
        FileBackedTaskManager fileManager = createTaskManager();

        Task task1 = new Task("Task 1", "task description 1", 1, LocalDateTime.now(), Duration.ofMinutes(90));
        Epic epic1 = new Epic("Epic 1", "epic description 1", 2, new ArrayList<>());
        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", 3, LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());

        fileManager.createTask(task1);
        fileManager.createEpic(epic1);
        fileManager.createSubtask(subtask1);

        FileBackedTaskManager newFileManager = FileBackedTaskManager.loadFromFile(temporaryFile);
        Task loadedTask = newFileManager.getTaskById(1);
        Epic loadedEpic = newFileManager.getEpicById(2);
        Subtask loadedSubtask = newFileManager.getSubtaskById(3);

        assertEquals(task1, loadedTask);
        assertEquals(epic1, loadedEpic);
        assertEquals(subtask1, loadedSubtask);
    }

    @Test
    public void shouldThrowExceptionIfFileNotFound() {
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(new File("someFile"));
        });

    }
}
