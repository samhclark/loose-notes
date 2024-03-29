public class Ch1Q9 {

    public static void main(String[] args) {
        System.out.println(isRotation("waterbotle", "erbotlewat"));
    }

    private static boolean isRotation(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        var sb = new StringBuilder(2 * a.length());
        // This is wild, no need to build the string weird like this.
        // Check for a counter example before making it complicated.
        // String bigstring = sb.append(a.substring(a.length() / 2))
        //     .append(a)
        //     .append(a.substring(0, a.length() / 2))
        //     .toString();

        String bigstring = sb.append(a)
            .append(a)
            .toString();
        
        
        return bigstring.contains(b);
    }
}