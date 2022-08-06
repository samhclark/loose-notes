package ch2;

import java.util.HashSet;

public class Q1 {

    public static void main(String[] args) {
        int[] input = {1, 1, 2, 3, 4, 5, 2, 2, 3, 6, 7, 7};
        var head = new LinkedListNode<Integer>(input[0]);
        for (int i = 0; i < input.length; i++) {
            head.addLast(input[i]);
        }

        removeDuplicatesNoBuffer(head);

        System.out.println(head);
    }

    private static <T> void removeDuplicates(LinkedListNode<T> node) {
        var seen = new HashSet<T>();
        LinkedListNode<T> prev = null;
        while (node != null) {
            if (seen.contains(node.data)) {
                if (prev != null) {
                    prev.next = node.next;
                }
            } else {
                seen.add(node.data);
                prev = node;
            }
            node = node.next;
        }
    }

    private static <T> void removeDuplicatesNoBuffer(LinkedListNode<T> node) {
        LinkedListNode<T> checker = null;
        LinkedListNode<T> prev = null;
        while (node != null) {
            checker = node.next;
            prev = node;
            while (checker != null) {
                if (checker.data == node.data) {
                    if (prev != null) {
                        prev.next = checker.next;
                    }
                } else {
                    prev = checker;
                }
                checker = checker.next;
            }
            node = node.next;
        }
    }
}