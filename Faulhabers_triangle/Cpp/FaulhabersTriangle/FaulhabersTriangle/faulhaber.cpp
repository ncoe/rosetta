#include <exception>
#include <iomanip>
#include <iostream>
#include <numeric>
#include <sstream>
#include <vector>

class Frac {
public:
	Frac(long n, long d) {
		if (d == 0) {
			throw new std::runtime_error("d must not be zero");
		}

		long nn = n;
		long dd = d;
		if (nn == 0) {
			dd = 1;
		} else if (dd < 0) {
			nn = -nn;
			dd = -dd;
		}

		long g = abs(std::gcd(nn, dd));
		if (g > 1) {
			nn /= g;
			dd /= g;
		}

		num = nn;
		denom = dd;
	}

	Frac operator-() const {
		return Frac(-num, denom);
	}

	Frac operator+(const Frac& rhs) const {
		return Frac(num*rhs.denom + denom * rhs.num, rhs.denom*denom);
	}

	Frac operator-(const Frac& rhs) const {
		return Frac(num*rhs.denom - denom * rhs.num, rhs.denom*denom);
	}

	Frac operator*(const Frac& rhs) const {
		return Frac(num*rhs.num, denom*rhs.denom);
	}

	friend std::ostream& operator<<(std::ostream&, const Frac&);

	static Frac ZERO() {
		return Frac(0, 1);
	}

private:
	long num;
	long denom;
};

std::ostream & operator<<(std::ostream & os, const Frac &f) {
	if (f.num == 0 || f.denom == 1) {
		return os << f.num;
	}

	std::stringstream ss;
	ss << f.num << "/" << f.denom;
	return os << ss.str();
}

Frac bernoulli(int n) {
	if (n < 0) {
		throw new std::runtime_error("n may not be negative or zero");
	}

	std::vector<Frac> a;
	for (int m = 0; m <= n; m++) {
		a.push_back(Frac(1, m + 1));
		for (int j = m; j >= 1; j--) {
			a[j - 1] = (a[j - 1] - a[j]) * Frac(j, 1);
		}
	}

	// returns 'first' Bernoulli number
	if (n != 1) return a[0];
	return -a[0];
}

int binomial(int n, int k) {
	if (n < 0 || k < 0 || n < k) {
		throw new std::runtime_error("parameters are invalid");
	}
	if (n == 0 || k == 0) return 1;

	int num = 1;
	for (int i = k + 1; i <= n; i++) {
		num *= i;
	}

	int denom = 1;
	for (int i = 2; i <= n - k; i++) {
		denom *= i;
	}

	return num / denom;
}

std::vector<Frac> faulhaberTraingle(int p) {
	std::vector<Frac> coeffs;

	for (int i = 0; i < p + 1; i++) {
		coeffs.push_back(Frac::ZERO());
	}

	Frac q{ 1, p + 1 };
	int sign = -1;
	for (int j = 0; j <= p; j++) {
		sign *= -1;
		coeffs[p - j] = q * Frac(sign, 1) * Frac(binomial(p + 1, j), 1) * bernoulli(j);
	}

	return coeffs;
}

int main() {
	using namespace std;

	for (int i = 0; i < 10; i++) {
		vector<Frac> coeffs = faulhaberTraingle(i);
		for (auto it = coeffs.begin(); it != coeffs.end(); it++) {
			cout << right << setw(5) << *it << "  ";
		}
		cout << endl;
	}

	return 0;
}
