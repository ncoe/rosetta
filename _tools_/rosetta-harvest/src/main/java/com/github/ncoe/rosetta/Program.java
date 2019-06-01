package com.github.ncoe.rosetta;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import com.github.ncoe.rosetta.io.HtmlWriter;
import com.github.ncoe.rosetta.io.SpreadsheetWriter;
import com.github.ncoe.rosetta.util.LanguageUtil;
import com.github.ncoe.rosetta.util.LocalUtil;
import com.github.ncoe.rosetta.util.RemoteUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The class that links the others together.
 */
public final class Program {
    private static final String BIG_DECIMAL = "big decimal";
    private static final String BIG_INTEGER = "big integer";
    private static final String DYNAMIC_MEMORY = "dynamic memory";
    private static final String FILE_IO = "file io";
    private static final String IMAGE_IO = "image io";
    private static final String NESTED_FUNCTIONS = "nested functions";
    private static final String NETWORK_IO = "network io";

    private Program() {
        throw new NotImplementedException("No Program for you!");
    }

    /**
     * Entry point.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        Path outPath = Path.of("out");
        if (Files.exists(outPath)) {
            Path openPath = outPath.resolve("~$" + SpreadsheetWriter.FILENAME);
            if (Files.exists(openPath)) {
                throw new UtilException("First close " + openPath.toString());
            }
        } else {
            try {
                Files.createDirectory(outPath);
            } catch (IOException e) {
                throw new UtilException(e);
            }
        }

        generate();
    }

    private static void generate() {
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
        for (String language : LanguageUtil.LANGUAGES) {
            String taskLang = LanguageUtil.rosettaToLanguage(language);
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

                if (info.getCategory() == 0) {
                    System.out.printf("There are multiple solutions for [%s], additionally %s\n", entry.getKey(), langTime.getKey());
                } else {
                    info.setCategory(0);
                    info.setNext(langTime.getLeft());
                    info.setLastModified(langTime.getRight());
                }
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
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Arithmetic_coding/As_a_generalized_change_of_radix");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Arithmetic-geometric_mean/Calculate_Pi");
        info.setNote(BIG_INTEGER + " / " + BIG_DECIMAL);

        info = taskInfoMap.get("Base58Check_encoding");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Bilinear_interpolation");
        info.setNote(IMAGE_IO);

        info = taskInfoMap.get("Chat_server");
        info.setNote(NETWORK_IO);

        info = taskInfoMap.get("Cipolla's_algorithm");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Create_a_file_on_magnetic_tape");
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Eertree");
        info.setNote(NESTED_FUNCTIONS);

        info = taskInfoMap.get("Egyptian_fractions");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Lucky_and_even_lucky_numbers");
        info.setNote("commandline arguments");

        info = taskInfoMap.get("Get_system_command_output");
        info.setNote("process io");

        info = taskInfoMap.get("I_before_E_except_after_C");
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Knuth's_power_tree");
        info.setNote(BIG_DECIMAL);

        info = taskInfoMap.get("Magic_squares_of_doubly_even_order");
        info.setNote(DYNAMIC_MEMORY);

        info = taskInfoMap.get("Make_directory_path");
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Markov_chain_text_generator");
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Mersenne_primes");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Montgomery_reduction");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Narcissist");
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Sierpinski_pentagon");
        info.setNote(IMAGE_IO + " / " + FILE_IO);

        info = taskInfoMap.get("Suffix_tree");
        info.setNote(NESTED_FUNCTIONS);

        info = taskInfoMap.get("Tonelli-Shanks_algorithm");
        info.setNote(BIG_INTEGER);

        info = taskInfoMap.get("Write_entire_file");
        info.setNote(FILE_IO);

        info = taskInfoMap.get("Write_to_Windows_event_log");
        info.setNote("windows");

        // Prioritize some tasks so that there is more than one task with the same prefix
        taskInfoMap.entrySet()
            .stream()
            .filter(entry -> {
                String key = entry.getKey();
                return StringUtils.startsWithAny(key,
                    "Arithmetic-geometric_mean",
                    "Arithmetic_coding",
                    "Averages",
                    "Hello_world",
                    "Parsing",
                    "Reflection"
                ) && !StringUtils.equalsAny(key,
                    "Arithmetic-geometric_mean/Calculate_Pi",
                    "Arithmetic_coding/As_a_generalized_change_of_radix",
                    "Averages/Pythagorean_means",
                    "Hello_world/Newline_omission",
                    "Parsing/Shunting-yard_algorithm",
                    "Reflection/List_methods"
                );
            })
            .map(Entry::getValue)
            .forEach(data -> {
                if (0 < data.getCategory() && data.getCategory() < 3) {
                    System.err.printf("No longer need to process task [%s]\n", data.getTaskName());
                } else if (data.getCategory() > 2) {
                    if (data.getCategory() == 3) {
                        data.setCategory(1.7);
                        data.setNote("Multiple Options :)");
                    } else {
                        data.setCategory(1.8);
                        data.setNote("Only one option :(");
                    }
                }
            });

        // Follow priority with tasks that have one language left to work on
        taskInfoMap.values()
            .stream()
            .filter(data -> data.getCategory() == 2 && data.getLanguageSet().size() == 1)
            .forEach(data -> data.setCategory(1.9));
    }
}