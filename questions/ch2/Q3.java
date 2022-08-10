package ch2;

public class Q3 {

    // Removing a middle node from a singly linked list 
    // given only access to the node to be removed.
    // Off the top of my head idk how to do that.
    // Singly linked means I need to re-link the previous node.
    // So like if it was a->b->c, and I'm given access to b
    // I need to modify a.next = c (AKA b.next)
    // How do I access anything in a?
    // 
    // I guess I don't have to. a.next is a pointer to a node
    // Can I somehow just rename b = c? b = b.next? 
    // 
    // To consider:
    // * Does this mess with the identity of the nodes? 
    //      Like if anyone else holds a reference to b, are they also updated?
    // * If I run into problems, another approach could be to go through the
    //      rest of the list and copy each next into the current. Like the data and all. 
    //      ...might end up needing to do that anyway?
    public static void main(String[] args) {
        int[] input = {1, 2, 3};
        var head = new LinkedListNode<Integer>(input[0]);
        for (int i = 1; i < input.length; i++) {
            head.addLast(input[i]);
        }

        var copy = head.next;
        System.out.println(head);
        removeMiddle(head.next);
        System.out.println(head);

        System.out.println(copy);
    }

    private static <T> void removeMiddle(LinkedListNode<T> node) {
        // Does this really just work?
        // node = node.next;
        // No it doesn't.

        // Okay fine. Let's try moving through the rest of the list
        // and copying all the nexts into current

        // while (node.next != null) {
        //     node.data = node.next.data;
        //     node.next = node.next.next;
        // }

        // And actually, you don't even need to go through the rest of the list.
        // Just update the current node's data to match
        // And update its `next` pointer to skip the next one.
        node.data = node.next.data;
        node.next = node.next.next;

        // Okay
        // Let's discuss
        // This does get the result to look right, if we're looking at the data. 
        // Let head = 1 -> 2 -> 3 (where each is Integer)
        // removeMiddle(head.next)
        // then head = 1 -> 3
        // 
        // BUT
        // The references to them didn't change
        // Altering the toString() method to print the hashCode shows this output instead
        // head = 2060468723, 622488023, 1933863327
        // removeMiddle(622488023)
        // 2060468723, 622488023
        //
        // It didn't really remove that.
        // If someone else (another thread?) hold a reference to that middle element that will be removed,
        // then it will be updated.
        // I know that's how it works with Java, but it would be nice to have actually removed the element
        // But preserverd the other thread's reference to it.

    }
}