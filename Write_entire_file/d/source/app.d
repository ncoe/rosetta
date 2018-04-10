//https://rosettacode.org/wiki/Write_entire_file
import std.stdio;

void main() {
    auto file = File("new.txt", "wb");
    file.writeln("Hello World!");
}
