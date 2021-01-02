#include <iomanip>
#include <iostream>

int pancake(int n) {
    int gap = 2, sum = 2, adj = -1;
    while (sum < n) {
        adj++;
        gap = gap * 2 - 1;
        sum += gap;
    }
    return n + adj;
}

int main() {
    for (int i = 0; i < 4; i++) {
        for (int j = 1; j < 6; j++) {
            int n = i * 5 + j;
            std::cout << "p(" << std::setw(2) << n << ") = " << std::setw(2) << pancake(n) << "  ";
        }
        std::cout << '\n';
    }
    return 0;
}
