//https://rosettacode.org/wiki/Parallel_Brute_Force
import std.digest.sha;
import std.range;
import std.stdio;

// Find the five lower-case letter strings representing the following sha256 hashes
immutable p1 = cast(ubyte[32]) x"1115dd800feaacefdf481f1f9070374a2a81e27880f187396db67958b207cbad";
immutable p2 = cast(ubyte[32]) x"3a7bd3e2360a3d29eea436fcfb7e44c735d117c42d1c1835420b6b9942dd4f1b";
immutable p3 = cast(ubyte[32]) x"74e1bb62f8dabb8125a58852b63bdf6eaef667cb56ac7f7cdba6d7305c50a22f";

void main() {
    char[5] psw;
    foreach(char a; 'a'..'z'+1) {
        psw[0] = a;
        foreach(char b; 'a'..'z'+1) {
            psw[1] = b;
            foreach(char c; 'a'..'z'+1) {
                psw[2] = c;
                foreach(char d; 'a'..'z'+1) {
                    psw[3] = d;
                    foreach(char e; 'a'..'z'+1) {
                        psw[4] = e;
                        auto hash = psw.sha256Of;
                        if (equal(hash, p1) || equal(hash, p2) || equal(hash, p3)) {
                            writefln("%s <=> %(%x%)", psw, hash);
                        }
                    }
                }
            }
        }
    }
}

//Specialization that supports static arrays too
bool equal(T)(const T[] p, const T[] q) {
    if (p.length != q.length) {
        return false;
    }

    for(int i=0; i<p.length; i++) {
        if (p[i] != q[i]) {
            return false;
        }
    }

    return true;
}
