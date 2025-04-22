import java.util.*;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private int id = 1000;

    public int generateId() {
        return id++;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    public void createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            subtask.setId(generateId());
            epic.addSubtask(subtask);
            epic.epicStatusMonitoring();
            subtasks.put(subtask.getId(), subtask);
        } else {
            System.out.println("Эпика, к которому вы хотите создать подзадачу, не существует");
        }
    }

    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.epicStatusMonitoring();
        } else {
            System.out.println("Эпика, к которому вы хотите обновить подзадачу, не существует");
        }
    }

    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            Epic copiedEpic = new Epic(epic.getName(), epic.getDescription(), epic.getId(), epic.getSubtasks());

            for (Subtask subtask : oldEpic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
            epics.put(epic.getId(), copiedEpic);
            for (Subtask subtask : copiedEpic.getSubtasks()) {
                subtasks.put(subtask.getId(), subtask);
            }
            epics.get(epic.getId()).epicStatusMonitoring();
        } else {
            System.out.println("Такого эпика не существует");
        }
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.deleteSubtaskById(subtask.getId());
                epic.epicStatusMonitoring();
                subtasks.remove(id);
            } else {
                System.out.println("Такого эпика не существует");
            }
        } else {
            System.out.println("Такой сабтаски не существует");
        }
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(id);
        } else {
            System.out.println("Такого эпика не существует");
        }
    }

    public ArrayList<Subtask> getAllSubtasksOfEpicById(int id) {
        return epics.get(id).getSubtasks();
    }


}