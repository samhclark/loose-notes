public class Ch1Q7 {

    public static void main(String[] args) {
        final int[][] input = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1, 2}, {3, 4, 5, 6}};
        printMatrix(input);
        printMatrix(rotate90(input));
    }

    // Rotate a NxN matrix by 90 degrees where each element 4 bytes
    // Thinking about graphs from math class I'm gonna assume "rotate by 90"
    // means counterclockwise.
    //
    private static int[][] rotate90(int[][] in) {
        final int N = in.length;
        for (int i = 0; i < N; i++) {
            if (in[i].length != N) {
                throw new RuntimeException("rotate90 requires the input to be square (NxN)");
            } 
        }

        // Now that I'm sure the input is nice and valid
        // 
        // input: a b c
        //        d e f
        //        g h i
        // 
        // out  : c f i
        //        b e h
        //        a d g
        //
        // so lets do it the easy way
        //   a: 0,0 -> 2,0
        //   b: 0,1 -> 1,0
        //   c: 0,2 -> 0,0
        //   d: 1,0 -> 2,1
        //   ...
        //   i: 2,2 -> 0,2
        int[][] out = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                out[(N-1)-j][i] = in[i][j];
            }
        }
        return out;
    }

    // For debugging
    private static void printMatrix(int[][] matrix) {
        System.out.print("\n   0 1 2 4\n   -------\n");
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(i + "| ");
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }
}
