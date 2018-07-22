using System;
using System.Linq;

namespace ChineseRemainderTheorem {
    class Program {
        static int ChineseRemainder(int[] n, int[] a) {
            int prod = n.Aggregate(1, (i, j) => i * j);
            int p;
            int sm = 0;
            for (int i = 0; i < n.Length; i++) {
                p = prod / n[i];
                sm += a[i] * MulInv(p, n[i]) * p;
            }
            return sm % prod;
        }

        static int MulInv(int a, int b) {
            int b0 = b;
            int x0 = 0;
            int x1 = 1;

            if (b == 1) {
                return 1;
            }

            while (a > 1) {
                int q = a / b;
                int amb = a % b;
                a = b;
                b = amb;
                int xqx = x1 - q * x0;
                x1 = x0;
                x0 = xqx;
            }

            if (x1 < 0) {
                x1 += b0;
            }

            return x1;
        }

        static void Main(string[] args) {
            int[] n = { 3, 5, 7 };
            int[] a = { 2, 3, 2 };
            Console.WriteLine(ChineseRemainder(n, a));
        }
    }
}
