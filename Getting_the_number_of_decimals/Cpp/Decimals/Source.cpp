#include <iomanip>
#include <iostream>
#include <sstream>

int findNumOfDec(double x) {
    std::stringstream ss;
    ss << std::fixed << std::setprecision(14) << x;

    auto s = ss.str();
    auto pos = s.find('.');
    if (pos == std::string::npos) {
        return 0;
    }

    auto tail = s.find_last_not_of('0');

    return tail - pos;
}

void test(double x) {
    std::cout << x << " has " << findNumOfDec(x) << " decimals\n";
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
