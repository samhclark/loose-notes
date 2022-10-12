package ch3;

import java.util.NoSuchElementException;

public class SimpleQueue<T> {
    private static class Node<T> {
        private T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }

    private Node<T> first;
    private Node<T> last;

    public void add(T item) {
        var newItem = new Node<T>(item);
        if (last != null) {
            last.next = newItem;
        }
        last = newItem;
        if (first == null) {
            first = newItem;
        }
    }

    public T remove() {
        if (first == null) {
            throw new NoSuchElementException();
        }
        T item = first.data;
        first = first.next;
        if (first == null) {
            last = null;
        }
        return item;
    }

    public T peek() {
        if (first == null) {
            throw new NoSuchElementException();
        }
        return first.data;
    }

    public boolean isEmpty() {
        return first == null;
    }
}
