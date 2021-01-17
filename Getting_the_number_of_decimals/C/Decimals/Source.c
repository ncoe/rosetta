#include <stdio.h>

int findNumOfDec(double x) {
    char buffer[128];
    int pos, num;

    sprintf(buffer, "%.14f", x);

    pos = 0;
    num = 0;
    while (buffer[pos] != 0 && buffer[pos] != '.') {
        pos++;
    }
    if (buffer[pos] != 0) {
        pos++; // skip over the decimal
        while (buffer[pos] != 0) {
            pos++; // find the end of the string
        }
        pos--; //reverse past the null sentiel
        while (buffer[pos] == '0') {
            pos--; // reverse past any zeros
        }
        while (buffer[pos] != '.') {
            num++;
            pos--; // only count decimals from this point
        }
    }
    return num;
}

void test(double x) {
    int num = findNumOfDec(x);
    printf("%f has %d decimals\n", x, num);
}

int main() {
    test(12.0);
    test(12.345);
    test(12.345555555555);
    test(12.3450);
    test(12.34555555555555555555);
    test(1.2345e+54);
    return 0;
}
