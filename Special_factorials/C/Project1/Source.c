#include <math.h>
#include <stdint.h>
#include <stdio.h>

/* n! = 1 * 2 * 3 * ... * n */
uint64_t factorial(int n) {
    uint64_t result = 1;
    int i;

    for (i = 1; i <= n; i++) {
        result *= i;
    }

    return result;
}

/* if(n!) = n */
int inverse_factorial(uint64_t f) {
    int p = 1;
    int i = 1;

    if (f == 1) {
        return 0;
    }

    while (p < f) {
        p *= i;
        i++;
    }

    if (p == f) {
        return i - 1;
    }
    return -1;
}

/* sf(n) = 1! * 2! * 3! * ... . n! */
uint64_t super_factorial(int n) {
    uint64_t result = 1;
    int i;

    for (i = 1; i <= n; i++) {
        result *= factorial(i);
    }

    return result;
}

/* H(n) = 1^1 * 2^2 * 3^3 * ... * n^n */
uint64_t hyper_factorial(int n) {
    uint64_t result = 1;
    int i;

    for (i = 1; i <= n; i++) {
        result *= (uint64_t)powl(i, i);
    }

    return result;
}

/* af(n) = -1^(n-1)*1! + -1^(n-2)*2! + ... + -1^(0)*n! */
uint64_t alternating_factorial(int n) {
    uint64_t result = 0;
    int i;

    for (i = 1; i <= n; i++) {
        if ((n - i) % 2 == 0) {
            result += factorial(i);
        } else {
            result -= factorial(i);
        }
    }

    return result;
}

/* n$ = n ^ (n - 1) ^ ... ^ (2) ^ 1 */
uint64_t exponential_factorial(int n) {
    uint64_t result = 0;
    int i;

    for (i = 1; i <= n; i++) {
        result = (uint64_t)powl(i, (long double)result);
    }

    return result;
}

void test_factorial(int count, uint64_t(*func)(int), char *name) {
    int i;

    printf("First %d %s:\n", count, name);
    for (i = 0; i < count ; i++) {
        printf("%llu ", func(i));
    }
    printf("\n");
}

void test_inverse(uint64_t f) {
    int n = inverse_factorial(f);
    if (n < 0) {
        printf("rf(%llu) = No Solution\n", f);
    } else {
        printf("rf(%llu) = %d\n", f, n);
    }
}

int main() {
    int i;

    /* cannot display the 10th result correctly */
    test_factorial(9, super_factorial, "super factorials");
    printf("\n");

    /* cannot display the 9th result correctly */
    test_factorial(8, super_factorial, "hyper factorials");
    printf("\n");

    test_factorial(10, alternating_factorial, "alternating factorials");
    printf("\n");

    test_factorial(5, exponential_factorial, "exponential factorials");
    printf("\n");

    test_inverse(1);
    test_inverse(2);
    test_inverse(6);
    test_inverse(24);
    test_inverse(120);
    test_inverse(720);
    test_inverse(5040);
    test_inverse(40320);
    test_inverse(362880);
    test_inverse(3628800);
    test_inverse(119);

    return 0;
}
