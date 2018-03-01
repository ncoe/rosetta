public class DammAlgorithm {
    private static final int[][] table = new int[][]{
        new int[]{0, 3, 1, 7, 5, 9, 8, 6, 4, 2},
        new int[]{7, 0, 9, 2, 1, 5, 4, 8, 6, 3},
        new int[]{4, 2, 0, 6, 8, 7, 1, 3, 5, 9},
        new int[]{1, 7, 5, 0, 9, 8, 3, 4, 2, 6},
        new int[]{6, 1, 2, 3, 0, 4, 5, 9, 7, 8},
        new int[]{3, 6, 7, 4, 2, 0, 9, 5, 8, 1},
        new int[]{5, 8, 6, 9, 7, 2, 0, 1, 3, 4},
        new int[]{8, 9, 4, 5, 3, 6, 2, 0, 1, 7},
        new int[]{9, 4, 3, 8, 6, 1, 7, 2, 0, 5},
        new int[]{2, 5, 8, 1, 4, 3, 6, 7, 9, 0},
    };

    private static boolean damm(String s) {
        int interim = 0;
        for (char c : s.toCharArray()) interim = table[interim][c - '0'];
        return interim == 0;
    }

    public static void main(String[] args) {
        int[] numbers = new int[]{5724, 5727, 112946, 112949};
        for (Integer number : numbers) {
            boolean isValid = damm(number.toString());
            if (isValid) {
                System.out.printf("%6d is valid\n", number);
            } else {
                System.out.printf("%6d is invalid\n", number);
            }
        }
    }
}
