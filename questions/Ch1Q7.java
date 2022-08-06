public class Ch1Q7 {

    public static void main(String[] args) {
        final int[][] input = new int[8][8];
        for (int i = 0; i < 64; i++) {
            input[i/8][i%8] = i+1;
        }
        printMatrix(input);
        printMatrix(rotate90(input));
    }

    // Rotate a NxN matrix by 90 degrees where each element 4 bytes
    // Thinking about graphs from math class I'm gonna assume "rotate by 90"
    // means counterclockwise.
    //
    // O(N^2)
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
        // 
        // using second array
        // ```
        // int[][] out = new int[N][N];
        // for (int i = 0; i < N; i++) {
        //     for (int j = 0; j < N; j++) {
        //         out[(N-1)-j][i] = in[i][j];
        //     }
        // }
        // return out;
        // ```

        // in place using XOR
        //
        // XOR has got to be the way, right? Should allow me to mask it in and out
        //
        // No. Wrote a bunch of different variations and I can't get it to work. 
        //
        // The solution is to stop looking at each complete row at once and instead
        // look at an element in the same place on each edge. So, like, rotate all
        // four corners at once. Then rotate all the elements just clockwise of the
        // four corers. Keep moving clockwise until that outermost ring (what the 
        // book and a popular SO answer call a "layer") is done. Then move _inward_ 
        // and repeat
        //
        // so the outmost loop indicates what ring we're on
        // I'll use the same term, "layer" cause it's growing on me
        //
        // why N/2 and not N+1/2? If you have at 3x3 matrix, although you have 2 layers,
        // you don't actually want to move anything on that inner layer, because there
        // is only one element so it's already in the right place
        for (int layer = 0; layer < N / 2; layer++) {
            // Then we need to look at how many of these elements to move
            // In a 4x4 matrix, we do three rotations in the outer layer.
            // then we do one rotation in the inner layer
            // 
            // so we're going to define a start and end point for our iteration
            // you can just imagine that we're looking at the top of the matrix
            int start = layer;
            int end = N - 1 - layer;
            for (int i = layer; i < end; i++) {
                
                // Rotate them all counterclockwise,
                // but you want to move through all the points clockwise
                // so you don't overwrite the next one you need to move
                // 
                // The tricky part here is how to actually write the coords
                // that point to the element on each side.
                //
                // top:
                //   row: the same as the layer we're in.
                //   col: what we're iterating through (i)
                // left:
                //   row: what we've called `end` minus `i` because we need to be moving up
                //   col: is the layer we're in
                // right:
                //   row: moves down. just i.
                //   col: it's what we've called `end`
                // bottom:
                //   row: fixed, and what we called `end`?
                //   col: moves right. `end` + `layer` - `i`? Which is just...N-1-i.

                int backwardsIterator = N - 1 - i; // This works because i already takes `layer` into account.

                int temp = in[start][i];

                // Move the right to the top
                in[start][i] = in[i][end];

                // Move the bottom to the right
                in[i][end] = in[end][backwardsIterator]; // wrong: N - 1 - layer - i

                // Move the left to the bottom
                in[end][backwardsIterator] = in[backwardsIterator][start];

                // Move the top to the left (but pull from temp)
                in[backwardsIterator][start] = temp;
            }
        }
        return in;
    }

    // I don't really get the way the solution describes it...gonna work it out to see
    //
    // Okay now I get it.
    private static int[][] whatTheBookSays(int[][] matrix) {
        if (matrix.length == 0 || matrix.length != matrix[0].length) {
            throw new RuntimeException("rotate90 requires the input to be square (NxN)");
        }
        int N = matrix.length;
        for (int layer = 0; layer < (N / 2); layer++) { // what the hell is a layer?
            int first = layer;
            int last = N - 1 - layer;
            for (int i = first; i < last; i++) {
                int offset = i - first;
                int top = matrix[first][i];

                matrix[first][i] = matrix[last-offset][first];
                matrix[last-offset][first] = matrix[last][last-offset];
                matrix[last][last-offset] = matrix[i][last];
                matrix[i][last] = matrix[i][last];
                matrix[i][last] = top;
            }
        }
        return matrix;
    }

    // For debugging
    private static void printMatrix(int[][] matrix) {
        System.out.print("\n    0  1  2  4  5  6  7  8\n  ------------------------\n");
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(i + "| ");
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] < 10) {
                    System.out.print(" ");
                }
                System.out.print(matrix[i][j] + " ");
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }
}
