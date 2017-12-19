//https://rosettacode.org/wiki/Check_output_device_is_a_terminal
import std.stdio;

extern(C) int isatty(int);

void main() {
    writeln("Stdout is tty: ", stdout.fileno.isatty == 1);
}
