package com.yandex.task_tracker.utils;

import com.yandex.task_tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class CustomLinkedList<T extends Task> {
    public static class Node<T> {
        private final T data;
        private Node<T> prev;
        private Node<T> next;

        public Node(T data, Node<T> previous, Node<T> next) {
            this.data = data;
            this.prev = previous;
            this.next = next;
        }
    }

    private Node<T> tail;
    private Node<T> head;

    public Node<T> linkLast(T element) {
        final Node<T> oldLast = tail;
        final Node<T> newLast = new Node<>(element, oldLast, null);
        tail = newLast;
        if (oldLast == null) {
            head = newLast;
        } else {
            oldLast.next = newLast;
        }
        return newLast;
    }

    public List<T> getTasks() {
        List<T> tasks = new ArrayList<>();
        Node<T> element = head;
        while (element != null) {
            tasks.add(element.data);
            element = element.next;
        }
        return tasks;
    }

    public void removeNode(Node<T> node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

}
