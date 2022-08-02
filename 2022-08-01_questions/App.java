import java.util.HashSet;

class App {
    public static void main(String[] args) {
        var result = isUniqueBitwiseAscii(args[0]);
        if (result) {
            System.out.println("Unique");
        } else {
            System.out.println("Not unique");
        }
    }

    // Requires that the string is ASCII only
    private static boolean isUniqueBitwiseAscii(String s) {
        // 128 ASCII values so 128 bits
        long mostSigBits = 0L;
        long leastSigBits = 0L;
        for (int i = 0; i < s.length(); i++) {
            int val = s.charAt(i);
            if (val >= Long.SIZE) {
                // You don't have to subtract 64 because << will wrap around
                // val = val - Long.SIZE;
                if ((mostSigBits & (1 << val)) > 0) {
                    return false;
                }
                mostSigBits |= (1 << val);
            } else {
                if ((leastSigBits & (1 << val)) > 0) {
                    return false;
                }
                leastSigBits |= (1 << val);
            }
        }
        return true;
    }

    private static boolean isUniqueSetUnicode(String s) {
        final var seenChars = new HashSet<Integer>(s.length(), 1.0f);

        for (int c : s.codePoints().toArray()) {
            if (seenChars.contains(c)) {
                return false;
            } else {
                seenChars.add(c);
            }
        }
        
        return true;
    }

    private static boolean isUniqueArrayUnicode(String s) {
        // Allocating this array is probably not the move if you have to support Unicode
        boolean[] seenChars = new boolean[Character.MAX_CODE_POINT];
        for (int c : s.codePoints().toArray()) {
            if (seenChars[c]) {
                return false;
            } else {
                seenChars[c] = true;
            }
        }
        return true;
    }

}