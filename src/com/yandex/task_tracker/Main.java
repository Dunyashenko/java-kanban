package com.yandex.task_tracker;

import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.Managers;
import com.yandex.task_tracker.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager inMemoryTaskManager = Managers.getDefault();

        Task task1 = new Task("Task 1", "task description 1", null);
        Task task2 = new Task("Task 2", "task description 2", null);
        inMemoryTaskManager.createTask(task1);
        inMemoryTaskManager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        Epic epic2 = new Epic("Epic 2", "epic description 2", null, new ArrayList<>());
        inMemoryTaskManager.createEpic(epic1);
        inMemoryTaskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null,
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(90), epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null,
                LocalDateTime.now().plusDays(2), Duration.ofMinutes(60), epic1.getId());
        Subtask subtask3 = new Subtask("Subtask 3", "subtask description 3", null,
                LocalDateTime.now().plusDays(3), Duration.ofMinutes(70), epic1.getId());
        Subtask subtask4 = new Subtask("Subtask 4", "subtask description 4", null,
                LocalDateTime.now().plusDays(4), Duration.ofMinutes(80), epic2.getId());
        Subtask subtask5 = new Subtask("Subtask 5", "subtask description 5", null,
                LocalDateTime.now().plusDays(5), Duration.ofMinutes(50), epic2.getId());


        inMemoryTaskManager.createSubtask(subtask1);
        inMemoryTaskManager.createSubtask(subtask2);
        inMemoryTaskManager.createSubtask(subtask3);
        inMemoryTaskManager.createSubtask(subtask4);
        inMemoryTaskManager.createSubtask(subtask5);


        System.out.println("== Все задачи ==");
        System.out.println(inMemoryTaskManager.getAllTasks());
        System.out.println("== Все эпики ==");
        System.out.println(inMemoryTaskManager.getAllEpics());
        System.out.println("== Все подзадачи ==");
        System.out.println(inMemoryTaskManager.getAllSubtasks());

        task1.setStatus(Status.IN_PROGRESS);
        inMemoryTaskManager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask1);

        subtask4.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask4);
        subtask5.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask5);

        epic2.setDescription("new Description");
        inMemoryTaskManager.updateEpic(epic2);

        System.out.println("== Все эпики ==");
        System.out.println(inMemoryTaskManager.getAllEpics());

        Subtask subtask6 = new Subtask("Subtask 6", "subtask description 6", null,
                LocalDateTime.now(), Duration.ofMinutes(90), epic2.getId());
        inMemoryTaskManager.createSubtask(subtask6);

        epic2.setSubtasks(new ArrayList<>(List.of(subtask6)));
        inMemoryTaskManager.updateEpic(epic2);

        System.out.println("== Все эпики ==");
        System.out.println(inMemoryTaskManager.getAllEpics());

        System.out.println("== Все подзадачи ==");
        System.out.println(inMemoryTaskManager.getAllSubtasks());


        System.out.println("== Эпики после обновления подзадач ==");
        System.out.println(inMemoryTaskManager.getTaskById(task1.getId()));
        System.out.println(inMemoryTaskManager.getEpicById(epic1.getId()));
        System.out.println(inMemoryTaskManager.getEpicById(epic2.getId()));

        inMemoryTaskManager.deleteTaskById(task2.getId());
        inMemoryTaskManager.deleteSubtaskById(subtask1.getId());
        inMemoryTaskManager.deleteEpicById(epic2.getId());

        System.out.println("== После удаления задачи, подзадачи и эпика ==");
        System.out.println(inMemoryTaskManager.getAllTasks());
        System.out.println(inMemoryTaskManager.getAllEpics());
        System.out.println(inMemoryTaskManager.getAllSubtasks());

        inMemoryTaskManager.deleteAllEpics();

        System.out.println("== После удаления всех сабтасков ==");
        System.out.println(inMemoryTaskManager.getAllTasks());
        System.out.println(inMemoryTaskManager.getAllEpics());
        System.out.println(inMemoryTaskManager.getAllSubtasks());
    }
}
