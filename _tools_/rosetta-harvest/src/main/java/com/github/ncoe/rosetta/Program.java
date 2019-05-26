package com.github.ncoe.rosetta;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import com.github.ncoe.rosetta.io.HtmlWriter;
import com.github.ncoe.rosetta.io.SpreadsheetWriter;
import com.github.ncoe.rosetta.util.LocalUtil;
import com.github.ncoe.rosetta.util.RemoteUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Program {
    public static void main(String[] args) {
        generate();
    }

    private static String fixLangName(String language) {
        switch (language) {
            case "C_sharp":
                return "C#";
            case "F_Sharp":
                return "F#";
            case "Visual_Basic_.NET":
                return "Visual Basic .NET";
            default:
                return language;
        }
    }

    private static void generate() {
        Set<String> languages = Set.of(
            "C",
            "C++",
            "C_sharp",
            "D",
            "F_Sharp",
            "Java",
            "Kotlin",
            "Lua",
            "Modula-2",
            "Perl",
            "Python",
            "Visual_Basic_.NET"
        );

        // Gather local solutions and statistics
        Map<String, Long> langStatMap;
        Map<String, Pair<String, FileTime>> pendingMap;
        Map<String, Set<String>> localTaskMap;
        try {
            localTaskMap = LocalUtil.classifyCurrent();
            pendingMap = LocalUtil.pendingSolutions();
            langStatMap = LocalUtil.languageStats();
        } catch (IOException e) {
            throw new UtilException(e);
        }

        // Aggregate local data into task information
        Map<String, TaskInfo> taskInfoMap = new HashMap<>();
        for (Entry<String, Set<String>> entry : localTaskMap.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                Set<String> langSet = entry.getValue();
                int cat = langSet.size() > 1 ? 2 : 1;
                info = new TaskInfo(cat, entry.getKey());
                taskInfoMap.put(entry.getKey(), info);
            } else {
                System.err.printf("[Program] Unexpected task re-definition: %s\n", entry.getKey());
            }
        }

        // Gather remote data for the target languages
        Map<String, Set<String>> langByTask = new HashMap<>();
        for (String language : languages) {
            String taskLang = fixLangName(language);
            Set<String> langSet = RemoteUtil.harvest(language);

            // Incorporate the tasks that could be implemented with this language
            for (String taskName : langSet) {
                if (langByTask.containsKey(taskName)) {
                    langSet = langByTask.get(taskName);
                } else {
                    langSet = new HashSet<>();
                    langByTask.put(taskName, langSet);
                }
                langSet.add(taskLang);
            }
        }

        // Aggregate remote data into task information
        for (Entry<String, Set<String>> entry : langByTask.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                Set<String> langSet = entry.getValue();
                int cat = langSet.size() > 1 ? 3 : 4;
                info = new TaskInfo(cat, entry.getKey());
                info.getLanguageSet().addAll(entry.getValue());
                taskInfoMap.put(entry.getKey(), info);
            } else {
                info.getLanguageSet().addAll(entry.getValue());
            }
        }

        // Update task information with in-progress solutions
        for (Entry<String, Pair<String, FileTime>> entry : pendingMap.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null != info) {
                Pair<String, FileTime> langTime = entry.getValue();

                info.setCategory(0);
                info.setNext(langTime.getLeft());
                info.setLastModified(langTime.getRight());
            }
        }

        // Add notes to aid in selecting a language to try out for a task
        addNotes(taskInfoMap);

        // Write summary information for manipulation, filtering, and analysis
        SpreadsheetWriter.writeReport(taskInfoMap.values(), langStatMap);
        HtmlWriter.writeReport(taskInfoMap.values());
    }

    private static void addNotes(Map<String, TaskInfo> taskInfoMap) {
        TaskInfo info;

        info = taskInfoMap.get("Append_a_record_to_the_end_of_a_text_file");
        info.setNote("file io");

        info = taskInfoMap.get("Arithmetic_coding/As_a_generalized_change_of_radix");
        info.setNote("big integer");

        info = taskInfoMap.get("Base58Check_encoding");
        info.setNote("big integer");

        info = taskInfoMap.get("Bilinear_interpolation");
        info.setNote("image io");

        info = taskInfoMap.get("Chat_server");
        info.setNote("network io");

        info = taskInfoMap.get("Cipolla's_algorithm");
        info.setNote("big integer");

        info = taskInfoMap.get("Create_a_file_on_magnetic_tape");
        info.setNote("file io");

        info = taskInfoMap.get("Eertree");
        info.setNote("nested functions");

        info = taskInfoMap.get("Egyptian_fractions");
        info.setNote("big integer");

        info = taskInfoMap.get("Lucky_and_even_lucky_numbers");
        info.setNote("commandline arguments");

        info = taskInfoMap.get("Get_system_command_output");
        info.setNote("process io");

        info = taskInfoMap.get("I_before_E_except_after_C");
        info.setNote("file io");

        info = taskInfoMap.get("Knuth's_power_tree");
        info.setNote("big decimal");

        info = taskInfoMap.get("Magic_squares_of_doubly_even_order");
        info.setNote("dynamic memory");

        info = taskInfoMap.get("Make_directory_path");
        info.setNote("file io");

        info = taskInfoMap.get("Markov_chain_text_generator");
        info.setNote("file io");

        info = taskInfoMap.get("Mersenne_primes");
        info.setNote("big integer");

        info = taskInfoMap.get("Narcissist");
        info.setNote("file io");

        info = taskInfoMap.get("Sierpinski_pentagon");
        info.setNote("image io/file io");

        info = taskInfoMap.get("Tonelli-Shanks_algorithm");
        info.setNote("big integer");

        info = taskInfoMap.get("Write_entire_file");
        info.setNote("file io");

        info = taskInfoMap.get("Write_to_Windows_event_log");
        info.setNote("windows");
    }
}
