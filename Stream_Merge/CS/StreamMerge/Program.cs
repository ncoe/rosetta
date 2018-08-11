using System;
using System.Collections.Generic;
using System.Linq;

namespace StreamMerge {
    class Program {
        static void Merge2<T>(IEnumerable<T> i1, IEnumerable<T> i2, Action<T> output) where T : IComparable {
            IEnumerator<T> e1 = i1.GetEnumerator();
            IEnumerator<T> e2 = i2.GetEnumerator();

            bool hasA = e1.MoveNext();
            bool hasB = e2.MoveNext();
            while (hasA || hasB) {
                if (hasA) {
                    if (hasB) {
                        IComparable a = e1.Current;
                        IComparable b = e2.Current;
                        if (a.CompareTo(b) < 0) {
                            output.Invoke(e1.Current);
                            hasA = e1.MoveNext();
                        }
                        else {
                            output.Invoke(e2.Current);
                            hasB = e2.MoveNext();
                        }
                    }
                    else {
                        output.Invoke(e1.Current);
                        hasA = e1.MoveNext();
                    }
                }
                else if (hasB) {
                    output.Invoke(e2.Current);
                    hasB = e2.MoveNext();
                }
            }
        }

        static void MergeN<T>(Action<T> output, params IEnumerable<T>[] enumerables) where T : IComparable {
            if (enumerables.Length == 0) {
                return;
            }
            if (enumerables.Length == 1) {
                IEnumerator<T> e = enumerables[0].GetEnumerator();
                while (e.MoveNext()) {
                    output.Invoke(e.Current);
                }
                return;
            }

            int count = enumerables.Length;
            IEnumerator<T>[] eArr = new IEnumerator<T>[count];
            bool[] hasN = new bool[count];
            for (int i = 0; i < count; i++) {
                eArr[i] = enumerables[i].GetEnumerator();
                hasN[i] = eArr[i].MoveNext();
            }

            while (hasN.Aggregate(false, (a, b) => a || b)) {
                int index = -1;
                T value = default(T);
                for (int i = 0; i < count; i++) {
                    if (hasN[i]) {
                        if (index == -1) {
                            value = eArr[i].Current;
                            index = i;
                        }
                        else if (eArr[i].Current.CompareTo(value) < 0) {
                            value = eArr[i].Current;
                            index = i;
                        }
                    }
                }

                output.Invoke(value);
                hasN[index] = eArr[index].MoveNext();
            }
        }

        static void Main(string[] args) {
            List<int> a = new List<int>() { 1, 4, 7, 10 };
            List<int> b = new List<int>() { 2, 5, 8, 11 };
            List<int> c = new List<int>() { 3, 6, 9, 12 };

            Merge2(a, b, m => Console.Write("{0} ", m));
            Console.WriteLine();
            MergeN(m => Console.Write("{0} ", m), a, b, c);
            Console.WriteLine();
        }
    }
}
