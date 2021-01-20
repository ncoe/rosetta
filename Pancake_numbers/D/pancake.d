import std.stdio;

int pancake(int n) {
    int gap = 2, sum = 2, adj = -1;
    while (sum < n) {
        adj++;
        gap = 2 * gap - 1;
        sum += gap;
    }
    return n + adj;
}

void main() {
    foreach (i; 0..4) {
        foreach (j; 1..6) {
            int n = 5 * i + j;
            writef("p(%2d) = %2d  ", n, pancake(n));
        }
        writeln;
    }
}
