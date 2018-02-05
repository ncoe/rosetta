import java.util.Arrays;

public class SortThreeVariables {
    private static class Triple<T> {
        T first, second, third;

        Triple(T x, T y, T z) {
            first = x;
            second = y;
            third = z;
        }
    }

    private static <T extends Comparable> Triple<T> sortThree(T x, T y, T z) {
        Comparable[] a = new Comparable[]{x, y, z};
        Arrays.sort(a);
        return new Triple<>((T) a[0], (T) a[1], (T) a[2]);
    }

    private static <T> void printThree(T x, T y, T z) {
        System.out.printf("x=%s%ny=%s%nz=%s%n%n", x, y, z);
    }

    public static void main(String[] args) {
        String x = "lions, and tigers, and";
        String y = "bears, oh my!";
        String z = "(from the \"Wizard of OZ\")";
        Triple<String> t = sortThree(x, y, z);
        x = t.first;
        y = t.second;
        z = t.third;
        printThree(x, y, z);

        Integer x2 = 77444;
        Integer y2 = -12;
        Integer z2 = 0;
        Triple<Integer> t2 = sortThree(x2, y2, z2);
        x2 = t2.first;
        y2 = t2.second;
        z2 = t2.third;
        printThree(x2, y2, z2);

        Double x3 = 174.5;
        Double y3 = -62.5;
        Double z3 = 41.7;
        Triple<Double> t3 = sortThree(x3, y3, z3);
        x3 = t3.first;
        y3 = t3.second;
        z3 = t3.third;
        printThree(x3, y3, z3);
    }
}
