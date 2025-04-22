package com.yandex.task_tracker.model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, Integer id, int epicId) {
        super(name, description, id);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() + '\'' +
                ", description=" + getDescription() + '\'' +
                ", epicId=" + epicId +
                '}';
    }


}