using System;
using System.Text;
using System.Text.RegularExpressions;

namespace Validate_ISIN {
    class Program {
        static int DigitValue(char c, int b) {
            if (c >= '0' && c <= '9') {
                return c - '0';
            }
            return c - 'A' + 10;
        }

        static int Digit(char c, int b) {
            int result = DigitValue(c, b);
            if (result >= b) {
                Console.Error.WriteLine("Invalid Number");
                return -1;
            }
            return result;
        }

        static bool ISINtest(string isin) {
            isin = isin.Trim().ToUpper();
            Regex r = new Regex("^[A-Z]{2}[A-Z0-9]{9}\\d$");
            if (!r.IsMatch(isin)) {
                return false;
            }

            StringBuilder sb = new StringBuilder();
            foreach (char c in isin.Substring(0, 12)) {
                sb.Append(Digit(c, 36));
            }

            return LuhnTest(sb.ToString());
        }

        static string ReverseString(string input) {
            char[] intermediate = input.ToCharArray();
            Array.Reverse(intermediate);
            return new string(intermediate);
        }

        static bool LuhnTest(string number) {
            int s1 = 0;
            int s2 = 0;
            string reverse = ReverseString(number);
            for (int i = 0; i < reverse.Length; i++) {
                int digit = Digit(reverse[i], 10);
                //This is for odd digits, they are 1-indexed in the algorithm.
                if (i % 2 == 0) {
                    s1 += digit;
                }
                else { // Add 2 * digit for 0-4, add 2 * digit - 9 for 5-9.
                    s2 += 2 * digit;
                    if (digit >= 5) {
                        s2 -= 9;
                    }
                }
            }

            return (s1 + s2) % 10 == 0;
        }

        static void Main(string[] args) {
            string[] isins = {
                "US0378331005",
                "US0373831005",
                "U50378331005",
                "US03378331005",
                "AU0000XVGZA3",
                "AU0000VXGZA3",
                "FR0000988040"
            };
            foreach (string isin in isins) {
                Console.WriteLine("{0} is {1}", isin, ISINtest(isin) ? "valid" : "not valid");
            }
        }
    }
}
