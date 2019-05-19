import std.algorithm;
import std.stdio;
import std.string;

import local : classifyCurrent;
import remote : harvest;
/* Look more into xlsxd sometime. Failed last time during a build. */

void main() {
    auto langArr = [
        "C",
        "C_sharp",
        "C%2B%2B",
        "D",
        "F_Sharp",
        // "Go",
        "Java",
        "Kotlin",
        "Lua",
        "Modula-2",
        "Pascal",
        "Perl",
        "Python",
        // "Ruby",
        "Visual Basic .NET",
    ];

    auto tasks = classifyCurrent();

    foreach (lang; langArr) {
        auto langTasks = harvest(lang);
        foreach (task; langTasks) {
            tasks[task] ~= lang;
        }
    }

    auto of = File("unimplemented.txt", "w");
    foreach (k; tasks.keys.sort) {
        auto taskName = k;
        auto langs = tasks[k];

        if (isNumeric(taskName)) {
            taskName = "'" ~ taskName;
        }

        if (langs[0] == "1") {
            if (langs.length > 1) {
                of.writeln("1|", taskName, "|", tasks[k][1..$]);
            }
        } else if (langs[0] == "2") {
            if (langs.length > 1) {
                of.writeln("2|", taskName, "|", tasks[k][1..$]);
            }
        } else if (tasks[k].length > 1) {
            of.writeln("3|", taskName, "|", tasks[k]);
        } else {
            of.writeln("4|", taskName, "|", tasks[k]);
        }
    }
}
