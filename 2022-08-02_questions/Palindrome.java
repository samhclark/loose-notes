import java.util.HashSet;

public class Palindrome {
    public static void main(String[] args) {
        String input = "Star Trek";
        System.out.println(isPermutationPalindrome(input));
    }

    // Do you have any length restrictions on S?
    // ASCII or Unicode? 
    //
    // Assuming no length restriction
    // Assuming ASCII
    private static boolean isPermutationPalindrome(String s) {
        var counts = new HashSet<Character>();
        for (var c : s.toUpperCase().toCharArray()) {
            if (c == ' ') {
                continue;
            }
            if (counts.contains(c)) {
                counts.remove(c);
            } else {
                counts.add(c);
            }
        }
        return (counts.size() == 0 || counts.size() == 1);
    }
}