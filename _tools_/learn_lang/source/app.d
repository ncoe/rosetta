import std.algorithm;
import std.array;
import std.range;
import std.regex;
import std.stdio;
import std.string;
import std.uri;
import vibe.core.stream;
import vibe.inet.urltransfer;

immutable orphanTasks = [
    "Binary_digits",
    "Box_the_compass",
    "Factorial",
    "Fibonacci_sequence",
    "FizzBuzz",
    "Hello_world/Standard_error",
    "Integer_sequence",
    "Logical_operations"
];
bool[string] languages;

void filterTask(string task) {
    //<li class="toclevel-1 tocsection-19"><a href="#BASIC"><span class="tocnumber">19</span> <span class="toctext">BASIC</span></a>
    auto tocRegex = ctRegex!(`<a href="#([^"]+)"`);

    //http://rosettacode.org/wiki/Binary_digits
    download("http://rosettacode.org/wiki/" ~ task, (scope input) {
        auto builder = appender!string;
        try {
            ubyte[128] buf;
            int len;
            do {
                len = input.read(buf, IOMode.once);
                if (len > 0) {
                    if (len == buf.length) {
                        builder ~= cast(string) buf;
                        // writeln(buf);
                    } else {
                        builder ~= cast(string) buf[0 .. len];
                        // writeln(buf[0..len]);
                    }
                }
            } while (!input.empty);
        } catch (Exception e) {
            // Do not know what to do about this
            // stderr.writeln("[ERROR]", e.msg);
        }

        string source = builder.data;
        foreach (line; source.lineSplitter) {
            // writeln(line);
            auto tocMatch = matchFirst(line, tocRegex);
            if (tocMatch) {
                string lang = tocMatch[1];
                // writeln(lang, " is not an option for ", task);
                languages.remove(lang);
            }
        }
    });
}

void main() {
    //<a title="Reports:Tasks not implemented in ALGOL" href="/wiki/Reports:Tasks_not_implemented_in_ALGOL">Reports:Tasks not implemented in ALGOL</a>
    auto linkRegex = ctRegex!(`href="/wiki/Reports:Tasks_not_implemented_in_([^"]+)"`);

    download("http://rosettacode.org/wiki/Category:Unimplemented_tasks_by_language", (scope input) {
        auto builder = appender!string;
        try {
            ubyte[128] buf;
            int len;
            do {
                len = input.read(buf, IOMode.once);
                if (len > 0) {
                    if (len == buf.length) {
                        builder ~= cast(string) buf;
                        // writeln(buf);
                    } else {
                        builder ~= cast(string) buf[0 .. len];
                        // writeln(buf[0..len]);
                    }
                }
            } while (!input.empty);
        } catch (Exception e) {
            // Do not know what to do about this
            // stderr.writeln("[ERROR]", e.msg);
        }

        string source = builder.data;
        foreach (line; source.lineSplitter) {
            // writeln(line);
            auto linkMatch = matchFirst(line, linkRegex);
            if (linkMatch) {
                string lang = linkMatch[1];
                // writeln("Found ", lang);
                languages[lang] = true;
            }
        }
    });

    foreach (task; orphanTasks) {
        filterTask(task);
    }

    // writeln(languages.keys);
    foreach (lang; languages.keys.sort) {
        writeln(lang);
    }
}
