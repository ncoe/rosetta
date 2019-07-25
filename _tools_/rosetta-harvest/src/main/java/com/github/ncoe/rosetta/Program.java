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
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;

/**
 * The class that links the others together.
 */
public final class Program {
    private static final Logger LOG = LoggerFactory.getLogger(Program.class);

    private static final String BIG_DECIMAL = "big decimal";
    private static final String BIG_INTEGER = "big integer";
    private static final String DYNAMIC_MEMORY = "dynamic memory";
    private static final String FILE_IO = "file io";
    private static final String IMAGE_IO = "image io";
    private static final String NESTED_FUNCTIONS = "nested functions";
    private static final String NETWORK_IO = "network io";
    private static final boolean COMBINE = false;

    private Program() {
        throw new NotImplementedException("No Program for you!");
    }

    /**
     * Entry point.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        makeOutputDirectory();
        generate();
    }

    private static void makeOutputDirectory() {
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
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private static void generate() {
        // Gather local solutions and statistics
        Map<String, Long> langStatMap;
        Map<String, Pair<String, FileTime>> pendingMap;
        Map<String, Set<String>> localTaskMap;
        try (Repository repository = LocalUtil.getRepository()) {
            localTaskMap = LocalUtil.classifyCurrent(repository);

            Pair<Map<String, Pair<String, FileTime>>, Map<String, Long>> taskSizePair
                = LocalUtil.pendingSolutions(repository);
            pendingMap = taskSizePair.getKey();

            langStatMap = LocalUtil.languageStats(repository);
            if (COMBINE) {
                Map<String, Long> langSizeMap = taskSizePair.getValue();
                langStatMap = Stream.concat(langStatMap.entrySet().stream(), langSizeMap.entrySet().stream())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, Long::sum));
            }
        } catch (IOException e) {
            throw new UtilException(e);
        }

        // Aggregate local data into task information
        Map<String, TaskInfo> taskInfoMap = new HashMap<>();
        for (Entry<String, Set<String>> entry : localTaskMap.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                Set<String> langSet = entry.getValue();
                int cat = langSet.size() > 1 ? 2 : langSet.size();
                info = new TaskInfo(cat, entry.getKey());
                taskInfoMap.put(entry.getKey(), info);
            } else if (LOG.isWarnEnabled()) {
                LOG.warn("Unexpected task re-definition: {}", value("taskName", entry.getKey()));
            }
        }

        // Gather remote data for the target languages
        Map<String, Set<String>> langByTask = new HashMap<>();
        LanguageUtil.rosettaSet().parallelStream().forEach(language -> {
            String taskLang = LanguageUtil.rosettaToLanguage(language);
            Set<String> langSet = RemoteUtil.harvest(language);

            // Incorporate the tasks that could be implemented with this language
            for (String taskName : langSet) {
                synchronized (langByTask) {
                    if (langByTask.containsKey(taskName)) {
                        langSet = langByTask.get(taskName);
                    } else {
                        langSet = new HashSet<>();
                        langByTask.put(taskName, langSet);
                    }
                    langSet.add(taskLang);
                }
            }
        });

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

                // A task could be zero if we saw it already, or if it is the final implementation,
                // but that can be handled with the next re-sync
                if (info.getCategory() != 0) {
                    if (info.getCategory() == 1.0) {
                        info.setNote("--- New Task ---");
                    }
                    info.setCategory(0);
                    info.setNext(langTime.getLeft());
                    info.setLastModified(langTime.getRight());
                }
            } else if (LOG.isErrorEnabled()) {
                LOG.error("Failed to retrieve data about pending task: {}", value("task", entry.getKey()));
            }
        }

        // Add notes to aid in selecting a language to try out for a task
        addNotes(taskInfoMap);

        // Write summary information for manipulation, filtering, and analysis
        SpreadsheetWriter.writeReport(taskInfoMap.values(), langStatMap);
        HtmlWriter.writeReport(taskInfoMap.values());
    }

    private static void addNotes(Map<String, TaskInfo> taskInfoMap) {
        addNote(taskInfoMap, "Abbreviations,_automatic", FILE_IO);
        addNote(taskInfoMap, "Append_a_record_to_the_end_of_a_text_file", FILE_IO);
        addNote(taskInfoMap, "Arithmetic_coding/As_a_generalized_change_of_radix", BIG_INTEGER);
        addNote(taskInfoMap, "Arithmetic-geometric_mean/Calculate_Pi", BIG_INTEGER + " / " + BIG_DECIMAL);
        addNote(taskInfoMap, "Base58Check_encoding", BIG_INTEGER);
        addNote(taskInfoMap, "Bell_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Bilinear_interpolation", IMAGE_IO);
        addNote(taskInfoMap, "Chat_server", NETWORK_IO);
        addNote(taskInfoMap, "Cipolla's_algorithm", BIG_INTEGER);
        addNote(taskInfoMap, "Create_a_file_on_magnetic_tape", FILE_IO);
        addNote(taskInfoMap, "Eertree", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Egyptian_fractions", BIG_INTEGER);
        addNote(taskInfoMap, "Lucky_and_even_lucky_numbers", "commandline arguments");
        addNote(taskInfoMap, "Get_system_command_output", "process io");
        addNote(taskInfoMap, "I_before_E_except_after_C", FILE_IO);
        addNote(taskInfoMap, "Knuth's_power_tree", BIG_DECIMAL);
        addNote(taskInfoMap, "Magic_squares_of_doubly_even_order", DYNAMIC_MEMORY);
        addNote(taskInfoMap, "Make_directory_path", FILE_IO);
        addNote(taskInfoMap, "Markov_chain_text_generator", FILE_IO);
        addNote(taskInfoMap, "Mersenne_primes", BIG_INTEGER);
        addNote(taskInfoMap, "Montgomery_reduction", BIG_INTEGER);
        addNote(taskInfoMap, "Narcissist", FILE_IO);
        addNote(taskInfoMap, "N-body_problem", FILE_IO);
        addNote(taskInfoMap, "Sierpinski_pentagon", IMAGE_IO + " / " + FILE_IO);
        addNote(taskInfoMap, "Suffix_tree", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Tonelli-Shanks_algorithm", BIG_INTEGER);
        addNote(taskInfoMap, "Write_entire_file", FILE_IO);
        addNote(taskInfoMap, "Write_to_Windows_event_log", "windows");

        // Tasks that look doable with the current set of languages (possibly where a language is wanted moved up in ranking)
        Set<String> topPickSet = Set.of(
            "Hilbert_curve",
            "Kernighans_large_earthquake_problem",
            "Largest_number_divisible_by_its_digits",
            "Latin_Squares_in_reduced_form",
            "Peaceful_chess_queen_armies",
            "Pell's_equation",
            "Square_but_not_cube",
            "Weird_numbers"
        );

        // Prioritize some tasks so that there is more than one task with the same prefix
        taskInfoMap.entrySet()
            .stream()
            .filter(entry -> {
                String key = entry.getKey();
                return StringUtils.startsWithAny(key,
                    "Arithmetic_coding"
                ) && !StringUtils.equalsAny(key,
                    "Arithmetic_coding/As_a_generalized_change_of_radix"
                ) || topPickSet.contains(key);
            })
            .map(Entry::getValue)
            .forEach(data -> {
                if (0 < data.getCategory() && data.getCategory() < 3) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("No longer need to process task [{}]", value("taskName", data.getTaskName()));
                    }
                } else if (data.getCategory() > 2) {
                    if (data.getCategory() == 3) {
                        if (topPickSet.contains(data.getTaskName())) {
                            data.setCategory(1.5);
                            data.setNote("Top pick");
                        } else {
                            data.setCategory(1.7);
                            data.setNote("Multiple Options :)");
                        }
                    } else {
                        data.setCategory(1.8);
                        data.setNote("Only one option :(");
                    }
                }
            });
    }

    private static void addNote(Map<String, TaskInfo> taskInfoMap, String taskName, String note) {
        TaskInfo info = taskInfoMap.get(taskName);
        if (null == info) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unknown task [{}] for adding a note", value("taskName", taskName));
            }
        } else {
            info.setNote(note);
        }
    }
}
