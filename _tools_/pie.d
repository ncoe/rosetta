import std.array;
import std.file;
import std.path;
import std.process;
import std.stdio;
import std.string;

string getBaseDir() {
    auto cmd = executeShell("git rev-parse --show-toplevel");
    if (cmd.status == 0) {
        return chomp(cmd.output);
    } else {
        stderr.writeln("Failed to execute command, status=", cmd.status);
        throw new Exception("Failed to execute command.");
    }
}

// Output statistics for a breakdown of the languages used for solutions
void main() {
    auto baseDir = getBaseDir();

    auto cmd = executeShell("git ls-tree --full-tree -r --name-only HEAD");

    if (cmd.status == 0) {
        auto files = splitLines(cmd.output);
        ulong[string] freq;

        foreach(path; files) {
            auto pathArr = pathSplitter(path).array;
            auto filename = pathArr[$-1];
            auto ext = extension(filename);
            auto absName = buildPath(baseDir, path);

            if (!exists(absName)) {
                continue;
            }

            ulong sz = getSize(absName);
            switch(ext) {
                case ".c":
                    freq["C"] += sz;
                    break;
                case ".cpp":
                    freq["C++"] += sz;
                    break;
                case ".cs":
                    freq["C#"] += sz;
                    break;
                case ".d":
                    freq["D"] += sz;
                    break;
                case ".fs":
                    freq["F#"] += sz;
                    break;
                case ".java":
                    freq["Java"] += sz;
                    break;
                case ".kt":
                    freq["Kotlin"] += sz;
                    break;
                case ".lua":
                    freq["Lua"] += sz;
                    break;
                case ".mod":
                    freq["Modula-2"] += sz;
                    break;
                case ".pl":
                    freq["Perl"] += sz;
                    break;
                case ".py":
                    freq["Python"] += sz;
                    break;
                case ".vb":
                    freq["Visual Basic .NET"] += sz;
                    break;

                case ".bat":
                case ".gif":
                case ".jpg":
                case ".json":
                case ".svg":
                case ".txt":
                    writeln("Extranious file: ", path);
                    break;

                case "":
                case ".md":
                case ".template":
                case ".yml":
                    // ignore control files
                    break;
                default:
                    writeln("Unknown Extension: ", ext);
                    break;
            }
        }

        // writeln(freq);
        foreach (k,v; freq) {
            writeln(k, ',', v);
        }
    } else {
        stderr.writeln("Failed to execute command, status=", cmd.status);
    }
}
