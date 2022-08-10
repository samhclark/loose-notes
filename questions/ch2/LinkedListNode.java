package ch2;

public class LinkedListNode<T> {
    LinkedListNode<T> next = null;
    T data = null;

    public LinkedListNode(T data) {
        this.data = data;
    }

    public void addLast(T data) {
        var node = new LinkedListNode<T>(data);
        var it = this;
        while (it.next != null) {
            it = it.next;
        }
        it.next = node;
    };

    public String toString() {
        var sb = new StringBuilder();
        sb.append(String.valueOf(this.data));
        // sb.append(String.valueOf(this));
        if (this.next != null) {
            sb.append(", ");
            sb.append(String.valueOf(next));
        }
        return sb.toString();
    }
    // public String toString() {
    //     var sb = new StringBuilder();
    //     sb.append(this.hashCode());
    //     // sb.append(String.valueOf(this));
    //     if (this.next != null) {
    //         sb.append(", ");
    //         // sb.append(String.valueOf(next));
    //         sb.append(next.toString());
    //     }
    //     return sb.toString();
    // }
}
