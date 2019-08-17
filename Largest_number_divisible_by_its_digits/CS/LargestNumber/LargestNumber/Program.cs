using System;
using System.Collections.Generic;
using System.Linq;

namespace LargestNumber {
    class Program {
        static bool ChkDec(int num) {
            HashSet<int> set = new HashSet<int>();

            return num.ToString()
                .Select(c => c - '0')
                .All(d => (d != 0) && (num % d == 0) && set.Add(d));
        }

        static void Main() {
            int result = Enumerable.Range(0, 98764321)
                .Reverse()
                .Where(ChkDec)
                .First();
            Console.WriteLine(result);
        }
    }
}
