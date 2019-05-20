import std.array;
import std.algorithm;
import std.stdio;

import local : classifyCurrent;
import remote : harvest;
/* Look more into xlsxd sometime. Failed last time during a build. */

void writeTextSummary(string[][string] tasks) {
    auto of = File("unimplemented.txt", "w");
    foreach (k; tasks.keys.sort) {
        auto langs = tasks[k];

        if (langs[0] == "1") {
            if (langs.length > 1) {
                of.writeln("1|", k, "|", tasks[k][1..$]);
            }
        } else if (langs[0] == "2") {
            if (langs.length > 1) {
                of.writeln("2|", k, "|", tasks[k][1..$]);
            }
        } else if (tasks[k].length > 1) {
            of.writeln("3|", k, "|", tasks[k]);
        } else {
            of.writeln("4|", k, "|", tasks[k]);
        }
    }
}

string languageClasses(string[] languages) {
    auto classes = appender!string;
    foreach(lang; languages) {
        switch(lang) {
        case "C":
            classes ~= " clang";
            break;
        case "C++":
            classes ~= " cpp";
            break;
        case "C#":
            classes ~= " csharp";
            break;
        case "D":
            classes ~= " dlang";
            break;
        case "F#":
            classes ~= " fsharp";
            break;
        case "Java":
            classes ~= " java";
            break;
        case "Kotlin":
            classes ~= " kotlin";
            break;
        case "Lua":
            classes ~= " lua";
            break;
        case "Modula-2":
            classes ~= " modula2";
            break;
        case "Perl":
            classes ~= " perl";
            break;
        case "Python":
            classes ~= " python";
            break;
        case "Visual Basic .NET":
            classes ~= " vbnet";
            break;
        default:
            break;
        }
    }
    return classes.data;
}

void writeHtmlSummary(string[][string] tasks) {
    import local : getBaseDir;
    import vibe.textfilter.html : htmlEscape;

    auto base = getBaseDir();

    auto of = File("output/taskSummary.html", "w");
    of.writeln(`<!DOCTYPE html>`);
    of.writeln(`<html>`);
    of.writeln(`<head>`);
    of.writeln(`  <meta charset="UTF-8">`);
    of.writeln(`  <title>Task Summary</title>`);
    of.writeln(`  <link rel="stylesheet" type="text/css" href="style.css">`);
    of.writeln(`</head>`);
    of.writeln(`<body>`);
    of.writeln(`  <div id="taskBtnContainer">`);
    of.writeln(`    <button id="allTask" class="btn activeTask" onclick="filterTaskSelection('all')">Show all</button>`);
    of.writeln(`    <button id="TaskType1" class="btn" onclick="filterTaskSelection('TaskType1')">One Solution</button>`);
    of.writeln(`    <button id="TaskType2" class="btn" onclick="filterTaskSelection('TaskType2')">Multiple Solutions</button>`);
    of.writeln(`    <button id="TaskType3" class="btn" onclick="filterTaskSelection('TaskType3')">Multiple Options</button>`);
    of.writeln(`    <button id="TaskType4" class="btn" onclick="filterTaskSelection('TaskType4')">One Option</button>`);
    of.writeln(`  </div>`);
    of.writeln(`  <div id="taskBtnContainer">`);
    of.writeln(`    <button id="allLang" class="btn activeLang" onclick="filterLangSelection('all')">Show all</button>`);
    of.writeln(`    <button id="clang" class="btn" onclick="filterLangSelection('clang')">C</button>`);
    of.writeln(`    <button id="cpp" class="btn" onclick="filterLangSelection('cpp')">C++</button>`);
    of.writeln(`    <button id="csharp" class="btn" onclick="filterLangSelection('csharp')">C#</button>`);
    of.writeln(`    <button id="dlang" class="btn" onclick="filterLangSelection('dlang')">D</button>`);
    of.writeln(`    <button id="fsharp" class="btn" onclick="filterLangSelection('fsharp')">F#</button>`);
    of.writeln(`    <button id="java" class="btn" onclick="filterLangSelection('java')">Java</button>`);
    of.writeln(`    <button id="kotlin" class="btn" onclick="filterLangSelection('kotlin')">Kotlin</button>`);
    of.writeln(`    <button id="lua" class="btn" onclick="filterLangSelection('lua')">Lua</button>`);
    of.writeln(`    <button id="modula2" class="btn" onclick="filterLangSelection('modula2')">Modula-2</button>`);
    of.writeln(`    <button id="perl" class="btn" onclick="filterLangSelection('perl')">Perl</button>`);
    of.writeln(`    <button id="python" class="btn" onclick="filterLangSelection('python')">Python</button>`);
    of.writeln(`    <button id="vbnet" class="btn" onclick="filterLangSelection('vbnet')">Visual Basic .NET</button>`);
    of.writeln(`  </div>`);
    of.writeln(`  <div class="container">`);
    of.writeln(`    <table>`);
    of.writeln(`      <tr>`);
    of.writeln(`        <th>Task Type</th>`);
    of.writeln(`        <th>Task Name</th>`);
    of.writeln(`        <th>Open Languages</th>`);
    of.writeln(`      </tr>`);

    scope(exit) {
        of.writeln(`      </table>`);
        of.writeln(`    </div>`);
        of.writeln(`    <script language="JavaScript" src="script.js" type="text/javascript"></script>`);
        of.writeln(`  </body>`);
        of.writeln(`</html>`);
    }

    foreach (k; tasks.keys.sort) {
        auto taskName = htmlEscape(k);
        auto langs = tasks[k];
        auto classStr = languageClasses(langs);

        if (langs[0] == "1") {
            if (langs.length > 1) {
                langs = langs[1..$];

                of.writefln(`<tr class="taskFilter TaskType1%s">`, classStr);
                of.writeln(`  <td>One Solution</td>`);
            } else {
                continue;
            }
        } else if (langs[0] == "2") {
            if (langs.length > 1) {
                langs = langs[1..$];

                of.writefln(`<tr class="taskFilter TaskType2%s">`, classStr);
                of.writeln(`  <td>Multiple Solution</td>`);
            } else {
                continue;
            }
        } else if (tasks[k].length > 1) {
            of.writefln(`<tr class="taskFilter TaskType3%s">`, classStr);
            of.writeln(`  <td>Multiple Options</td>`);
        } else {
            of.writefln(`<tr class="taskFilter TaskType4%s">`, classStr);
            of.writeln(`  <td>One Option</td>`);
        }

        of.writefln(`  <td>%s</td>`, taskName);
        of.writeln(`  <td>`);
        foreach(lang; langs) {
            switch(lang) {
            case "C":
                of.writeln(`    <img src="images/c.png" alt="C" title="C" style="width:50px;height:50px;">`);
                break;
            case "C++":
                of.writeln(`    <img src="images/cpp.png" alt="C++" title="C++" style="width:50px;height:50px;">`);
                break;
            case "C#":
                of.writeln(`    <img src="images/csharp.png" alt="C#" title="C#" style="width:50px;height:50px;">`);
                break;
            case "D":
                of.writeln(`    <img src="images/d.png" alt="D" title="D" style="width:50px;height:50px;">`);
                break;
            case "F#":
                of.writeln(`    <img src="images/fsharp.png" alt="F#" title="F#" style="width:50px;height:50px;">`);
                break;
            case "Java":
                of.writeln(`    <img src="images/java.png" alt="Java" title="Java" style="width:50px;height:50px;">`);
                break;
            case "Kotlin":
                of.writeln(`    <img src="images/kotlin.png" alt="Kotlin" title="Kotlin" style="width:50px;height:50px;">`);
                break;
            case "Lua":
                of.writeln(`    <img src="images/lua.png" alt="Lua" title="Lua" style="width:50px;height:50px;">`);
                break;
            case "Modula-2":
                of.writeln(`    <img src="images/m2.png" alt="Modula-2" title="Modula-2" style="width:50px;height:50px;">`);
                break;
            case "Perl":
                of.writeln(`    <img src="images/perl.png" alt="Perl" title="Perl" style="width:50px;height:50px;">`);
                break;
            case "Python":
                of.writeln(`    <img src="images/python.png" alt="Python" title="Python" style="width:50px;height:50px;">`);
                break;
            case "Visual Basic .NET":
                of.writeln(`    <img src="images/vbnet.png" alt="Visual Basic .NET" title="Visual Basic .NET" style="width:50px;height:50px;">`);
                break;
            default:
                stderr.writeln("Unknown image language: ", lang);
                of.writefln(`    <img src="images/unknown.png" alt="%s" title="%s" style="width:50px;height:50px;">`, lang, lang);
                break;
            }
        }
        of.writeln(`  </td>`);
        of.writeln(`</tr>`);
    }
}

void main() {
    auto langArr = [
        "C",
        "C%2B%2B",
        "C_sharp",
        "D",
        "F_Sharp",
        // "Go",
        "Java",
        "Kotlin",
        "Lua",
        "Modula-2",
        // "Pascal",
        "Perl",
        "Python",
        // "Ruby",
        "Visual Basic .NET",
    ];

    auto tasks = classifyCurrent();

    foreach (lang; langArr) {
        auto langTasks = harvest(lang);
        foreach (task; langTasks) {
            string tl;
            switch(lang) {
            case "C%2B%2B":
                tl = "C++";
                break;
            case "C_sharp":
                tl = "C#";
                break;
            case "F_Sharp":
            case "F_sharp":
                tl = "F#";
                break;
            default:
                tl = lang;
                break;
            }
            tasks[task] ~= tl;
        }
    }

    // writeTextSummary(tasks);
    writeHtmlSummary(tasks);
}
