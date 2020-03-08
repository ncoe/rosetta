package com.github.ncoe.rosetta;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import com.github.ncoe.rosetta.io.HtmlWriter;
import com.github.ncoe.rosetta.io.SpreadsheetWriter;
import com.github.ncoe.rosetta.util.LanguageUtil;
import com.github.ncoe.rosetta.util.LocalUtil;
import com.github.ncoe.rosetta.util.MiscUtil;
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
    private static final String SLASH = " / ";
    private static final boolean COMBINE = true;

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
                int cat = MiscUtil.choice(langSet.size() > 1, 2, langSet.size());
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
                int cat = MiscUtil.choice(langSet.size() > 1, 3, 4);
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

        // Change the default priority of some tasks to make some stand out in various ways
        adjustPriority(taskInfoMap, pendingMap);

        // Write summary information for manipulation, filtering, and analysis
        SpreadsheetWriter.writeReport(taskInfoMap.values(), langStatMap);
        HtmlWriter.writeReport(taskInfoMap.values());
    }

    private static void addNotes(Map<String, TaskInfo> taskInfoMap) {
        addNote(taskInfoMap, "Abbreviations,_automatic", FILE_IO);
        addNote(taskInfoMap, "Append_a_record_to_the_end_of_a_text_file", FILE_IO);
        addNote(taskInfoMap, "Arithmetic_coding/As_a_generalized_change_of_radix", BIG_INTEGER);
        addNote(taskInfoMap, "Arithmetic-geometric_mean/Calculate_Pi", BIG_INTEGER + SLASH + BIG_DECIMAL);
        addNote(taskInfoMap, "Base58Check_encoding", BIG_INTEGER);
        addNote(taskInfoMap, "Bell_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Bilinear_interpolation", IMAGE_IO);
        addNote(taskInfoMap, "Chat_server", NETWORK_IO);
        addNote(taskInfoMap, "Chemical_Calculator", DYNAMIC_MEMORY);
        addNote(taskInfoMap, "Cipolla's_algorithm", BIG_INTEGER);
        addNote(taskInfoMap, "Cramer's_rule", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Create_a_file_on_magnetic_tape", FILE_IO);
        addNote(taskInfoMap, "De_Bruijn_sequences", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Eertree", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Egyptian_fractions", BIG_INTEGER);
        addNote(taskInfoMap, "Lucky_and_even_lucky_numbers", "commandline arguments");
        addNote(taskInfoMap, "Get_system_command_output", "process io");
        addNote(taskInfoMap, "I_before_E_except_after_C", FILE_IO);
        addNote(taskInfoMap, "Knuth's_power_tree", BIG_DECIMAL);
        addNote(taskInfoMap, "Kosaraju", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Lah_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Magic_squares_of_doubly_even_order", DYNAMIC_MEMORY);
        addNote(taskInfoMap, "Make_directory_path", FILE_IO);
        addNote(taskInfoMap, "Markov_chain_text_generator", FILE_IO);
        addNote(taskInfoMap, "Mersenne_primes", BIG_INTEGER);
        addNote(taskInfoMap, "Metallic_ratios", BIG_DECIMAL);
        addNote(taskInfoMap, "Montgomery_reduction", BIG_INTEGER);
        addNote(taskInfoMap, "Narcissist", FILE_IO);
        addNote(taskInfoMap, "N-body_problem", FILE_IO);
        addNote(taskInfoMap, "N-smooth_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Pell's_equation", BIG_INTEGER);
        addNote(taskInfoMap, "Pierpont_primes", BIG_INTEGER + SLASH + "prime test");
        addNote(taskInfoMap, "Rare_numbers", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Sierpinski_pentagon", IMAGE_IO + SLASH + FILE_IO);
        addNote(taskInfoMap, "Suffix_tree", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Super-d_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Tonelli-Shanks_algorithm", BIG_INTEGER);
        addNote(taskInfoMap, "Write_entire_file", FILE_IO);
        addNote(taskInfoMap, "Write_to_Windows_event_log", "windows");
        addNote(taskInfoMap, "Zumkeller_numbers", DYNAMIC_MEMORY);
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

    private static void adjustPriority(Map<String, TaskInfo> taskInfoMap, Map<String, Pair<String, FileTime>> pendingMap) {
        Map<String, String> solAddMap = new HashMap<>();

        // C
        solAddMap.put("Cipolla's_algorithm", "C");
        solAddMap.put("Montgomery_reduction", "C");
        solAddMap.put("Pell's_equation", "C");
        solAddMap.put("Tonelli-Shanks_algorithm", "C");
        // C++
        solAddMap.put("Birthday_problem", "C++");
        solAddMap.put("Days_between_dates", "C++");
        solAddMap.put("Mersenne_primes", "C++");
        // Visual Basic .NET
        solAddMap.put("Handle_a_signal", "Visual Basic .NET");
        solAddMap.put("Kernighans_large_earthquake_problem", "Visual Basic .NET");
        solAddMap.put("Validate_International_Securities_Identification_Number", "Visual Basic .NET");

        // D
        solAddMap.put("Determine_if_a_string_is_squeezable", "D");
        solAddMap.put("Pierpont_primes", "D");
        solAddMap.put("Random_Latin_Squares", "D");
        // Lua
        solAddMap.put("Balanced_ternary", "Lua");
        solAddMap.put("Eban_numbers", "Lua");
        solAddMap.put("Reflection/List_properties", "Lua");
        // Ruby
        solAddMap.put("Addition_chains", "Ruby");
        solAddMap.put("Chemical_Calculator", "Ruby");
        solAddMap.put("Determine_if_two_triangles_overlap", "Ruby");

        // Groovy
        solAddMap.put("Burrows–Wheeler_transform", "Groovy");
        solAddMap.put("Cantor_set", "Groovy");
        solAddMap.put("Card_shuffles", "Groovy");
        // Java
        solAddMap.put("Decision_tables", "Java");
        solAddMap.put("Peaceful_chess_queen_armies", "Java");
        solAddMap.put("Rare_numbers", "Java");
        // Kotlin
        solAddMap.put("Cyclotomic_Polynomial", "Kotlin");
        solAddMap.put("Strong_and_weak_primes", "Kotlin");
        solAddMap.put("Successive_prime_differences", "Kotlin");

        //CHECKSTYLE:OFF InnerAssignment
        double solCat = 1.7;
        Map<String, Double> solCatMap = Map.of(
            "C", solCat += 0.01,                    //vs
            "Kotlin", solCat += 0.01,               //id
            "D", solCat += 0.01,                    //np
            "C++", solCat += 0.01,                  //vs
            "Java", solCat += 0.01,                 //id
            "Lua", solCat += 0.01,                  //np
            "Visual Basic .NET", solCat += 0.01,    //vs
            "Groovy", solCat += 0.01,               //id
            "Ruby", solCat += 0.01,                 //np

            "END", solCat
        );
        //CHECKSTYLE:ON InnerAssignment

        // Prioritize some tasks so that there is more than one task with the same prefix
        taskInfoMap.entrySet()
            .stream()
            .filter(entry -> {
                String key = entry.getKey();
                return StringUtils.startsWithAny(key,
                    "Arithmetic_coding"
                ) && !StringUtils.equalsAny(key,
                    "Arithmetic_coding/As_a_generalized_change_of_radix"
                ) || solAddMap.containsKey(key);
            })
            .map(Entry::getValue)
            .forEach(data -> {
                String taskName = data.getTaskName();
                if (solAddMap.containsKey(taskName)) {
                    String language = solAddMap.get(taskName);
                    if (data.getLanguageSet().contains(language)) {
                        if (pendingMap.containsKey(taskName)) {
                            Pair<String, FileTime> langFileTime = pendingMap.get(taskName);
                            String lang = langFileTime.getKey();
                            if (StringUtils.equals(language, lang)) {
                                LOG.warn("Solution has been prepared for {}", taskName);
                            } else {
                                LOG.warn("Solution was submitted for {}, but for a different language.", taskName);
                            }
                        } else {
                            if (data.getCategory() > 2) {
                                if (null == data.getNote()) {
                                    data.setNote("(NEW TASK)");
                                } else {
                                    data.setNote(data.getNote() + "(NEW TASK)");
                                }
                            }
                            data.setCategory(solCatMap.getOrDefault(language, 1.7));
                            data.setNext("try with " + language);
                        }
                    } else if (LOG.isWarnEnabled()) {
                        LOG.warn("No longer need to provide a solution to task [{}] using {}", value("taskName", taskName), value("language", language));
                    }
                } else if (0 < data.getCategory() && data.getCategory() < 3) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("No longer need to process task [{}]", value("taskName", taskName));
                    }
                }
            });
    }
}
