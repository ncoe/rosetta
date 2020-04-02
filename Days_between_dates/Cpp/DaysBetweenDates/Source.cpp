#include <iomanip>
#include <iostream>

class Date {
private:
    int year, month, day;

public:
    Date(std::string str) {
        if (isValidDate(str)) {
            year = atoi(&str[0]);
            month = atoi(&str[5]);
            day = atoi(&str[8]);
        } else {
            throw std::exception("Invalid date");
        }
    }

    int getYear() {
        return year;
    }

    int getMonth() {
        return month;
    }

    int getDay() {
        return day;
    }

    // YYYY-MM-DD
    static bool isValidDate(std::string str) {
        if (str.length() != 10 || str[4] != '-' || str[7] != '-') {
            return false;
        }

        if (!isdigit(str[0]) || !isdigit(str[1]) || !isdigit(str[2]) || !isdigit(str[3])) {
            return false; // the year is not valid
        }
        if (!isdigit(str[5]) || !isdigit(str[6])) {
            return false; // the month is not valid
        }
        if (!isdigit(str[8]) || !isdigit(str[9])) {
            return false; // the day is not valid
        }

        int year = atoi(&str[0]);
        int month = atoi(&str[5]);
        int day = atoi(&str[8]);

        // quick checks
        if (year <= 0 || month <= 0 || day <= 0) {
            return false;
        }
        if (month > 12) {
            return false;
        }

        switch (month) {
        case 2:
            if (day > 29) {
                return false;
            }
            if (!isLeapYear(year) && day == 29) {
                return false;
            }
            break;
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            if (day > 31) {
                return false;
            }
            break;
        default:
            if (day > 30) {
                return false;
            }
            break;
        }

        return true;
    }

    static bool isLeapYear(int year) {
        if (year > 1582) {
            return ((year % 4 == 0) && (year % 100 > 0))
                || (year % 400 == 0);
        }
        if (year > 10) {
            return year % 4 == 0;
        }
        // not bothering with earlier leap years
        return false;
    }

    friend std::ostream &operator<<(std::ostream &, Date &);
};

std::ostream &operator<<(std::ostream &os, Date &d) {
    os << std::setfill('0') << std::setw(4) << d.year << '-';
    os << std::setfill('0') << std::setw(2) << d.month << '-';
    os << std::setfill('0') << std::setw(2) << d.day;
    return os;
}

int diffDays(Date date1, Date date2) {
    int d1m = (date1.getMonth() + 9) % 12;
    int d1y = date1.getYear() - d1m / 10;

    int d2m = (date2.getMonth() + 9) % 12;
    int d2y = date2.getYear() - d2m / 10;

    int days1 = 365 * d1y + d1y / 4 - d1y / 100 + d1y / 400 + (d1m * 306 + 5) / 10 + (date1.getDay() - 1);
    int days2 = 365 * d2y + d2y / 4 - d2y / 100 + d2y / 400 + (d2m * 306 + 5) / 10 + (date2.getDay() - 1);

    return days2 - days1;
}

int main() {
    std::string ds1 = "2019-01-01";
    std::string ds2 = "2019-12-02";

    if (Date::isValidDate(ds1) && Date::isValidDate(ds2)) {
        Date d1(ds1);
        Date d2(ds2);
        std::cout << "Days difference : " << diffDays(d1, d2);
    } else {
        std::cout << "Dates are invalid.\n";
    }

    return 0;
}
