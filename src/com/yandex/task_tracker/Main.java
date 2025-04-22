package com.yandex.task_tracker;

import com.yandex.task_tracker.model.Epic;
import com.yandex.task_tracker.model.Status;
import com.yandex.task_tracker.model.Subtask;
import com.yandex.task_tracker.model.Task;
import com.yandex.task_tracker.service.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Task 1", "task description 1", null);
        Task task2 = new Task("Task 2", "task description 2", null);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "epic description 1", null, new ArrayList<>());
        Epic epic2 = new Epic("Epic 2", "epic description 2", null, new ArrayList<>());
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask 1", "subtask description 1", null, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "subtask description 2", null, epic1.getId());
        Subtask subtask3 = new Subtask("Subtask 3", "subtask description 3", null, epic1.getId());
        Subtask subtask4 = new Subtask("Subtask 4", "subtask description 4", null, epic2.getId());
        Subtask subtask5 = new Subtask("Subtask 5", "subtask description 5", null, epic2.getId());


        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        taskManager.createSubtask(subtask4);
        taskManager.createSubtask(subtask5);


        System.out.println("== Все задачи ==");
        System.out.println(taskManager.getAllTasks());
        System.out.println("== Все эпики ==");
        System.out.println(taskManager.getAllEpics());
        System.out.println("== Все подзадачи ==");
        System.out.println(taskManager.getAllSubtasks());

        task1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        subtask4.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask4);
        subtask5.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask5);

        epic2.setDescription("new Description");
        taskManager.updateEpic(epic2);

        System.out.println("== Все эпики ==");
        System.out.println(taskManager.getAllEpics());

        Subtask subtask6 = new Subtask("Subtask 6", "subtask description 6", null, epic2.getId());
        taskManager.createSubtask(subtask6);

        epic2.setSubtasks(new ArrayList<>(List.of(subtask6)));
        taskManager.updateEpic(epic2);

        System.out.println("== Все эпики ==");
        System.out.println(taskManager.getAllEpics());

        System.out.println("== Все подзадачи ==");
        System.out.println(taskManager.getAllSubtasks());


        System.out.println("== Эпики после обновления подзадач ==");
        System.out.println(taskManager.getTaskById(task1.getId()));
        System.out.println(taskManager.getEpicById(epic1.getId()));
        System.out.println(taskManager.getEpicById(epic2.getId()));

        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteSubtaskById(subtask1.getId());
        taskManager.deleteEpicById(epic2.getId());

        System.out.println("== После удаления задачи, подзадачи и эпика ==");
        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());

        taskManager.deleteAllEpics();

        System.out.println("== После удаления всех сабтасков ==");
        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
    }
}
