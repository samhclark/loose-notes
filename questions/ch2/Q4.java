package ch2;

/**
 * Q4
 */
public class Q4 {

    // Partition a linked list. This is a weird question
    // But off the top of my head, I think the plan would be to just build two
    // new lists and then join them up.
    // Let's worry about in-place optimization later
    public static void main(String[] args) {
        int[] input = {3, 5, 8, 5, 10, 2, 1};
        var head = new LinkedListNode<Integer>(input[0]);
        for (int i = 1; i < input.length; i++) {
            head.addLast(input[i]);
        }

        System.out.println(head);
        
        System.out.println(partition(head, 8));
    }

    private static <T extends Comparable<T>> LinkedListNode<T> partition(LinkedListNode<T> list, T value) {
        LinkedListNode<T> low = null;
        LinkedListNode<T> lowTail = null;
        LinkedListNode<T> high = null;

        while (list != null) {
            // System.out.println("Low list is: " + low);
            // System.out.println("High list is: " + high);
            // System.out.println("Checking " + list.data);
            if (list.data.compareTo(value) < 0) { // `<` doesn't work for generic type T
                // System.out.println("Less than " + value);
                if (low == null) {
                    // System.out.println("Adding first node to low list");
                    low = new LinkedListNode<T>(list.data);
                    lowTail = low;
                } else {
                    // System.out.println("Adding to end of low list");
                    low.addLast(list.data);
                    lowTail = lowTail.next;
                }
            } else {
                // System.out.println("Equal to or greater than " + value);
                if (high == null) {
                    // System.out.println("Adding first node to high list");
                    high = new LinkedListNode<T>(list.data);
                } else {
                    // System.out.println("Adding to end of high list");
                    high.addLast(list.data);
                }
            }
            list = list.next;
        }

        // System.out.println("Low list is: " + low);
        // System.out.println("High list is: " + high);
        if (low != null) {
            lowTail.next = high;
            return low;
        } else {
            return high;
        }
    }
}