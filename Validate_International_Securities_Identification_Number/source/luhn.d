//Taken from the following.
//https://rosettacode.org/wiki/Luhn_test_of_credit_card_numbers#D
import std.stdio;

struct Interval(T) {
    immutable T a, b;

    this(in T a_, in T b_) pure nothrow @nogc {
        this.a = a_;
        this.b = b_;
    }

    bool opBinaryRight(string op="in")(in T x)
    const pure nothrow @nogc {
        return x >= a && x <= b;
    }

    pure nothrow @safe @nogc const invariant {
        assert(a <= b);
    }
}

Interval!T interval(T)(in T a, in T b) pure nothrow @nogc {
    return Interval!T(a, b);
}

bool luhnTest(in string num) pure nothrow @nogc
in {
    assert(num.length <= 20);
} body {
    int sum = 0;
    bool od = true;
    bool ok = true;
    immutable int numLen = num.length;

    foreach_reverse (immutable p; 0 .. numLen) {
        immutable int i = num[p] - '0';
        if (i !in interval(0, 9)) {
            ok = false;
            break;
        }

        immutable int x = ((i * 2) % 10) + (i / 5);
        assert((numLen - p) in interval(0, 19));
        assert(sum in interval(0, (numLen - p) * 10));
        assert(i in interval(0, 9));
        assert(x in interval(0, 9));
        sum += od ? i : x;
        od = !od;
    }

    return ok && (sum % 10) == 0;
}
