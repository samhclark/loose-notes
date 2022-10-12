package ch3;

public class Q3 {
    public static void main(String[] args) {
        var testStacks = new SetOfStacks<Integer>(2);

        int[] input = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < input.length; i++) {
            testStacks.push(input[i]);
            testStacks.debugPrint();
        }

        for (int i = input.length - 1; i >= 0; i--) {
            System.out.println("Popped: " + testStacks.pop() + ". Should be: " + input[i]);
        }
    }
}
