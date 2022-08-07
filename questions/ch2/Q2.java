package ch2;

public class Q2 {
    public static void main(String[] args) {
        // int[] input = {1, 1, 2, 3, 4, 5, 2, 2, 3, 6, 7, 7};
        int[] input = {1};
        var head = new LinkedListNode<Integer>(input[0]);
        // for (int i = 1; i < input.length; i++) {
        //     head.addLast(input[i]);
        // }

        System.out.println(head);
        System.out.println(removeKthToLastOnePass(head, 1));
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
    }

    private static <T> T removeKthToLastOnePass(LinkedListNode<T> node, int k) {
        if (k < 1) {
            throw new RuntimeException("K must be greater than 0");
        }

        if (node == null) {
            throw new RuntimeException("Cannot remove element from an empty list");
        }

        // Shortcut since we already know the answer for this simple case.
        if (node.next == null) {
            T data = node.data;
            node.data = null;
            node = null;
            return data;
        }

        // Two pointers: leader, follower. 
        // Leader starts at head + K, follower starts at head.
        LinkedListNode<T> leader = node.next;
        LinkedListNode<T> follower = node;

        // Advance leader to head + K
        for (int i = 2; i < k ; i++) {
            if (leader.next == null) {
                throw new RuntimeException("K is greater than the length of the list");
            }
            leader = leader.next;
        }

        // Now just run through the rest of the list until the leader reaches the end,
        // but advance both pointers together
        while (leader.next != null) {
            leader = leader.next;
            follower = follower.next;
        }

        // Leader is at the end of the list
        // Follower is at the Kth to last element of the list.
        // Remove follower
        T data = follower.next.data;
        follower.next = follower.next.next;
        return data;
    }
}