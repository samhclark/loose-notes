public class Ch1Q3 {
    public static void main(String[] args) {
        char[] input = "A B C          ".toCharArray();
        int len = 7;
        escapeUrl(input, len);
        System.out.println(input);
    }

    private static void escapeUrl(char[] s, int len) {
        for (int i = 0; i < len; i++) {
            if (s[i] == ' ') {
                len += 2;
                for (int j = len-1; j > (i+2); j--) {
                    s[j] = s[j-2];
                }
                if (i + 4 < len) {
                    s[i+4] = s[i+2];
                }
                if (i + 3 < len) {
                    s[i+3] = s[i+1];
                }
                s[i+2] = '0';
                s[i+1] = '2';
                s[i] = '%';
                // not needed, but you could skip a few
                // i += 2;
            }
        }
    }
}


