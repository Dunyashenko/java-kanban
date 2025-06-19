package com.yandex.task_tracker.model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, Integer id, Status status, int epicId) {
        super(name, description, id, status);
        if (!((Integer) epicId).equals(id)) {
            this.epicId = epicId;
        }
    }

    public Subtask(String name, String description, Integer id, int epicId) {
        super(name, description, id);
        if (!((Integer) epicId).equals(id)) {
            this.epicId = epicId;
        }
    }

    public Subtask(Subtask subtask) {
        super(subtask.getName(), subtask.getDescription(), subtask.getId());
        this.epicId = subtask.getEpicId();
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (this.epicId == this.getId()) {
            return;
        }
        this.epicId = epicId;
    }

//    @Override
//    public String toString() {
//        return "Subtask{" +
//                "id=" + getId() +
//                ", name='" + getName() + '\'' +
//                ", status=" + getStatus() + '\'' +
//                ", description=" + getDescription() + '\'' +
//                ", epicId=" + epicId +
//                '}';
//    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", getId(), Type.SUBTASK.name(), getName(), getStatus(), getDescription(), epicId);
    }


}