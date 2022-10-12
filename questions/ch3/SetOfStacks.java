package ch3;

import java.util.EmptyStackException;

public class SetOfStacks<T> {

    private final int threshold;
    private int sizeOfHotStack;
    private SimpleStack<SimpleStack<T>> stacks;

    public SetOfStacks(int threshold) {
        this.threshold = threshold;
        this.stacks = new SimpleStack<>();
    }

    public void push(T item) {
        if (stacks.isEmpty() || sizeOfHotStack >= threshold) {
            var newStack = new SimpleStack<T>();
            stacks.push(newStack);
            sizeOfHotStack = 0;
        }

        var hotStack = stacks.peek();
        hotStack.push(item);
        sizeOfHotStack += 1;
    }

    public T pop() {
        if (stacks.isEmpty()) {
            throw new EmptyStackException();
        }

        var hotStack = stacks.peek();
        sizeOfHotStack -= 1;
        T item = hotStack.pop();

        if (sizeOfHotStack <= 0) {
            stacks.pop();
            sizeOfHotStack = threshold;
        }

        return item;
    }

    public void debugPrint() {
        System.out.println("threshold is " + threshold);
        System.out.println("sizeOfHotStack is " + sizeOfHotStack);
    }
}
