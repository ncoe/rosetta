import std.array : appender;
import std.regex : ctRegex, matchFirst;
import std.stdio : stderr;
import std.string : lineSplitter;
import std.uri : decodeComponent;

import vibe.core.stream : IOMode;
import vibe.inet.urltransfer : download;

enum SectionEnum {
    None,
    NotImplemented,
    DraftTasks,
    RequireAttention,
    NotConsidered,
    EndOfList,
}

string[] harvest(string lang) {
    string language = lang.decodeComponent;
    stderr.writeln("Harvesting data for ", language);

    string[] tasks;

    download("http://rosettacode.org/wiki/Reports:Tasks_not_implemented_in_" ~ lang, (scope input) {
        auto builder = appender!string;
        try {
            ubyte[128] buf;
            int len;
            do {
                len = input.read(buf, IOMode.once);
                if (len > 0) {
                    if (len == buf.length) {
                        builder ~= cast(string) buf;
                    } else {
                        builder ~= cast(string) buf[0 .. len];
                    }
                }
            } while (!input.empty);
        } catch (Exception e) {
            // Do not know what to do about this
            // stderr.writeln("[ERROR]", e.msg);
        }

        //<span class="mw-headline" id="Not_implemented">Not implemented</span>
        //<span class="mw-headline" id="Draft_tasks_without_implementation">Draft tasks without implementation</span>
        //<span class="mw-headline" id="Requiring_Attention">Requiring Attention</span>
        //<span class="mw-headline" id="Not_Considered">Not Considered</span>
        //<span class="mw-headline" id="End_of_List">End of List</span>
        auto headRegex = ctRegex!(`<span class="mw-headline" id="([^"]+)">[^<]+</span>`);

        //<li><a href="/wiki/15_puzzle_solver" title="15 puzzle solver">15 puzzle solver</a></li>
        auto taskRegex = ctRegex!(`<li><a href="/wiki/([^"]+)" title="[^"]+">[^<]+</a></li>`);

        string source = builder.data;
        SectionEnum section = SectionEnum.None;
        foreach (line; source.lineSplitter) {
            auto headMatch = matchFirst(line, headRegex);
            if (headMatch) {
                string head = headMatch[1];
                switch(head) {
                    case "Not_implemented":
                        section = SectionEnum.NotImplemented;
                        break;
                    case "Draft_tasks_without_implementation":
                        section = SectionEnum.DraftTasks;
                        break;
                    default:
                        section = SectionEnum.None;
                        break;
                }
            }

            if (section == SectionEnum.NotImplemented
             || section == SectionEnum.DraftTasks) {
                auto taskMatch = matchFirst(line, taskRegex);
                if (taskMatch) {
                    string task = taskMatch[1].decodeComponent;
                    string tl;
                    switch(language) {
                        case "C_sharp":
                            tl = "C#";
                            break;
                        case "F_sharp":
                            tl = "F#";
                            break;
                        default:
                            tl = language;
                            break;
                    }
                    tasks ~= task;
                }
            }
        }
    });

    return tasks;
}
