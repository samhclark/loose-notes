import java.util.HashSet;

public class Ch1Q8 {

    public static void main(String[] args) {
        int[][] input = {
            {0, 1, 2, 3},
            {4, 5, 0, 4},
            {6, 0, 9, 7},
            {9, 9, 9, 9}
        };
        printMatrix(input);
        zeroMatrix(input);
        printMatrix(input);
    }

    // Will there only be one element that is zero?
    // Assuming no.
    private static void zeroMatrix(int[][] matrix) {
        var rowsToZero = new HashSet<Integer>();
        var columnsToZero = new HashSet<Integer>();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == 0) {
                    rowsToZero.add(i);
                    columnsToZero.add(j);
                }
            }
        }
        
        for (Integer row : rowsToZero) {
            for (int i = 0; i < matrix[row].length; i++) {
                matrix[row][i] = 0;
            }
            // printMatrix(matrix);
        }

        // There's gotta be a way to get rid of these double writes
        // No need to touch every element again to zero it if it already
        // got zerod in the previous loop.
        for (Integer column : columnsToZero) {
            for (int i = 0; i < matrix.length; i++) {
                matrix[i][column] = 0;
            }
        }
    }

    // For debugging
    private static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }
}