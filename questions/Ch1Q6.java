public class Ch1Q6 {
    public static void main(String[] args) {
        System.out.println(compress("aabcccccaaa"));
    }

    // Runs in O(n). 
    // StringBuilder makes this pretty straightforward.
    private static String compress(String s) {
        int currentCount = 0;
        char currentChar = s.charAt(0);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == currentChar) {
                currentCount++;
            } else {
                sb.append(currentChar).append(currentCount);
                currentChar = s.charAt(i);
                currentCount = 1;
            }
        }
        String result = sb.append(currentChar)
            .append(currentCount)
            .toString();

        if (result.length() < s.length()) {
            return result;
        } else {
            return s;
        }
    }
}
