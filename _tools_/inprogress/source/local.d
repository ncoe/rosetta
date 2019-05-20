import std.array : array;
import std.file : dirEntries, isDir;
import std.path : pathSplitter;
import std.process : executeShell;
import std.stdio : stderr;
import std.string : chomp;

string getBaseDir() {
    auto cmd = executeShell("git rev-parse --show-toplevel");
    if (cmd.status == 0) {
        return chomp(cmd.output);
    } else {
        stderr.writeln("Failed to execute command, status=", cmd.status);
        throw new Exception("Failed to execute command.");
    }
}

private string fixName(string name) {
    switch (name) {
    case "Abbreviations_automatic":
        name = "Abbreviations,_automatic";
        break;
    case "Abundant_deficient_perfect":
        name = "Abundant,_deficient_and_perfect_number_classifications";
        break;
    case "Cipollas_algorithm":
        name = "Cipolla's_algorithm";
        break;
    case "Euler_sum_of_powers":
        name = "Euler's_sum_of_powers_conjecture";
        break;
    case "Faulhabers_formula":
        name = "Faulhaber's_formula";
        break;
    case "Faulhabers_triangle":
        name = "Faulhaber's_triangle";
        break;
    case "Floyds_triangle":
        name = "Floyd's_triangle";
        break;
    case "Horners_rule_for_polynomial_evaluation":
        name = "Horner's_rule_for_polynomial_evaluation";
        break;
    case "Knuths_power_tree":
        name = "Knuth's_power_tree";
        break;
    case "Pascals_triangle":
        name = "Pascal's_triangle";
        break;
    case "Recamans_sequence":
        name = "Recaman's_sequence";
        break;
    case "Sailors_coconuts_and_a_monkey_problem":
        name = "Sailors,_coconuts_and_a_monkey_problem";
        break;
    default:
        break;
    }

    version (none) {
        import vibe.http.client;
        auto res = requestHTTP("http://rosettacode.org/wiki/" ~ name,
            (scope req) {
                req.method = HTTPMethod.HEAD;
            }
        );
        if (res.statusCode >= 400) {
            stderr.writeln("    Unknown task: ", name);
        }
    }

    return name;
}

private string[] listLanguages(string baseDir) {
    import std.file : SpanMode;

    string[] languages;
    foreach (string fullName; dirEntries(baseDir, SpanMode.shallow)) {
        if (isDir(fullName)) {
            auto fileName = pathSplitter(fullName).array[$-1];
            switch (fileName) {
            case "Cpp":
                languages ~= "C++";
                break;
            case "CS":
                languages ~= "C#";
                break;
            case "d":
                stderr.writeln("    Fix needed in ", baseDir);
                languages ~= "D";
                break;
            case "FS":
                languages ~= "F#";
                break;
            case "Modula2":
                stderr.writeln("    Fix needed in ", baseDir);
                languages ~= "Modula-2";
                break;

            case "C":
            case "D":
            case "Java":
            case "Kotlin":
            case "Lua":
            case "Modula-2":
            case "Perl":
            case "Python":
            case "Visual Basic .NET":
                languages ~= fileName;
                break;
            default:
                stderr.writeln("    Unknown language [", fileName, "] in ", baseDir);
                break;
            }
        }
    }
    return languages;
}

private string[][string] currentSolutions(string baseDir, bool fix = true) {
    import std.file : SpanMode;

    string[][string] solAA;

    foreach (string fullName; dirEntries(baseDir, SpanMode.shallow)) {
        if (isDir(fullName)) {
            auto fileName = pathSplitter(fullName).array[$-1];
            if (fileName != ".git" && fileName != ".idea" && fileName != "_tools_") {
                switch (fileName) {
                case "Arithmetic_coding":
                case "Arithmetic-geometric_mean":
                case "Averages":
                case "Continued_fraction":
                case "Hello_world":
                case "Parsing":
                case "Reflection":
                    auto subAA = currentSolutions(fullName, false);
                    foreach(k,v; subAA) {
                        auto name = fixName(fileName ~ '/' ~ k);
                        solAA[name] = v;
                    }
                    break;
                default:
                    auto languageList = listLanguages(fullName);
                    if (languageList.length > 0) {
                        if (fix) {
                            auto name = fixName(fileName);
                            solAA[name] = languageList;
                        } else {
                            solAA[fileName] = languageList;
                        }
                    }
                }
            }
        }
    }

    return solAA;
}

string[][string] classifyCurrent() {
    auto baseDir = getBaseDir();
    auto solAA = currentSolutions(baseDir);

    string[][string] tasks;
    foreach(k,v; solAA) {
        if (v.length == 1) {
            tasks[k] ~= "1";
        } else {
            tasks[k] ~= "2";
        }
    }
    return tasks;
}

/* git ls-files --others --exclude-standard */
/* git status --porcelain */
