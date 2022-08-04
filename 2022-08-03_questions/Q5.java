public class Q5 {
    public static void main(String[] args) {
        System.out.println(oneAway("Enterprise", "nterprise"));
    }

    // Three scenarios: insert char, remove, replace
    // In all cases, the lengths at least are all the same or off by one.
    // Insert and remove can be treated as the same case. 
    // And each one can have only one change, so we could try iterating through 
    // the strings and making sure there is only one difference.
    //
    // Runs in O(n)
    private static boolean oneAway(String a, String b) {
        // Make `a` the longer of the two
        if (b.length() > a.length()) {
            String temp = a;
            a = b;
            b = temp;
        }

        // If they are off by more than one, we know there is more than one edit
        if (a.length() - b.length() > 1) {
            return false;
        }

        int numEdits = 0;
        int i = 0;
        for (int j = 0; j < b.length(); j++) {
            if (a.charAt(i) != b.charAt(j)) {
                numEdits += 1;

                // This case means it was an insert or delete.
                // Both iterators get moved forward on each loop, but
                // we want to stay on the same char in `b`
                // 
                // Current state
                // ex a = foobar, b = fobar
                //          ^           ^
                //
                // Both move up
                //    a = foobar, b = fobar
                //           ^           ^
                //
                // Move b back to line up the comparison
                //    a = foobar, b = fobar
                //           ^          ^
                //
                // If it was a replacement we keep on moving
                if (a.length() > b.length()) {
                    j -= 1;
                }
            }
            i++;
        }
        return (numEdits < 2);
    }
}
