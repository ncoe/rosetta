import std.conv;
import std.format;
import std.stdio;

enum n = 61;
int[] l = [0, 1];

int fusc(int n) {
    if (n < l.length) {
        return l[n];
    }
    int f = (n & 1) == 0 ? l[n >> 1] : l[(n - 1) >> 1] + l[(n + 1) >> 1];
    l ~= f;
    return f;
}

void main() {
    bool lst = true;
    int w = -1;
    int c = 0;
    int t;
    string fs = "%11s  %-9s";
    string res = "";
    for (int i = 0; i < int.max; i++) {
        int f = fusc(i);
        if (lst) {
            if (i < 61) {
                write(f, ' ');
            } else {
                lst = false;
                writeln;
                writeln("Points in the sequence where an item has more digits than any previous items:");
                writefln(fs, "Index\\", "/Value");
                writeln(res);
                res = "";
            }
        }
        t = f.to!string.length;
        if (t > w) {
            w = t;
            res ~= (res == "" ? "" : "\n") ~ format(fs, i, f);
            if (!lst) {
                writeln(res);
                res = "";
            }
            if (++c > 5) {
                break;
            }
        }
    }
    l.length = 0;
}
