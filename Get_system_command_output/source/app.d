//https://rosettacode.org/wiki/Get_system_command_output
import std.process;
import std.stdio;

void main() {
    auto cmd = executeShell("echo hello");

    if (cmd.status == 0) {
        writeln("Output: ", cmd.output);
    } else {
        writeln("Failed to execute command, status=", cmd.status);
    }
}
