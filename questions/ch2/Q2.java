package ch2;

public class Q2 {
    public static void main(String[] args) {
        int[] input = {1, 1, 2, 3, 4, 5, 2, 2, 3, 6, 7, 7};
        var head = new LinkedListNode<Integer>(input[0]);
        for (int i = 1; i < input.length; i++) {
            head.addLast(input[i]);
        }

        System.out.println(head);
        System.out.println(removeKthToLast(head, 12));
        System.out.println(head);
    }

    private static <T> T removeKthToLast(LinkedListNode<T> node, int k) {
        if (k < 1) {
            throw new RuntimeException("K must be greater than 0");
        }

        if (node == null) {
            throw new RuntimeException("Cannot remove element from an empty list");
        }

        // Shortcut since we already know the answer for this simple case.
        if (node.next == null) {
            return null;
        }

        // Naive. Let's run it through to see how long it is.
        // Then go back through and remove that Kth to last element.
        
        // Save the head value before we start messing with stuff.
        LinkedListNode<T> head = node;

        int length = 0;
        while (node != null) {
            length++;
            node = node.next;
        }

        if (k > length) {
            throw new RuntimeException("K is greater than the length of the list");
        }

        // Run it through again but let's remove the Kth to last element
        node = head;
        while (length - k - 1 > 0) {
            length--;
            node = node.next;
        }

        T data = node.next.data;
        node.next = node.next.next;
        return data;


        // TODO
        // I can do it in one pass.
        // Two pointers, leader and follower. 
        // Advance the leader point up by K, then start the follower at head.
        // The pointers are K elements apart from each other.
        // When the leader reaches the end of the list, remove the follower.
        // O(n) instead of O(2n)
    }
}