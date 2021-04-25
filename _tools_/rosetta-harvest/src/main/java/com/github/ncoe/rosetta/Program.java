package com.github.ncoe.rosetta;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.io.HtmlWriter;
import com.github.ncoe.rosetta.io.SpreadsheetWriter;
import com.github.ncoe.rosetta.util.LanguageUtil;
import com.github.ncoe.rosetta.util.LocalUtil;
import com.github.ncoe.rosetta.util.MiscUtil;
import com.github.ncoe.rosetta.util.RemoteUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.TreeMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;
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

        LocalUtil localUtil = new LocalUtil();
        localUtil.setCheckTaskName(false);

        generate(localUtil);
    }

    private static void makeOutputDirectory() {
        var outPath = Path.of("out");
        if (Files.exists(outPath)) {
            var openPath = outPath.resolve("~$" + SpreadsheetWriter.FILENAME);
            if (Files.exists(openPath)) {
                throw new IllegalStateException("First close " + openPath);
            }
        } else {
            Failable.accept(Files::createDirectory, outPath);
        }
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private static void generate(LocalUtil localUtil) {
        // Gather local solutions and statistics
        Map<String, Long> langStatMap;
        Map<String, Pair<String, FileTime>> pendingMap;
        Map<String, Set<String>> localTaskMap;
        try (var repository = localUtil.getRepository()) {
            localTaskMap = localUtil.classifyCurrent(repository);

            var taskSizePair = localUtil.pendingSolutions(repository);
            pendingMap = taskSizePair.getKey();

            langStatMap = localUtil.languageStats(repository);
            if (COMBINE) {
                var langSizeMap = taskSizePair.getValue();
                langStatMap = Stream.concat(langStatMap.entrySet().stream(), langSizeMap.entrySet().stream())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, Long::sum));
            }
        } catch (IOException e) {
            throw Failable.rethrow(e);
        }

        // Aggregate local data into task information
        Map<String, TaskInfo> taskInfoMap = new TreeMap<>();
        for (var entry : localTaskMap.entrySet()) {
            var info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                var langSet = entry.getValue();
                int cat = MiscUtil.choice(langSet.size() > 1, 2, langSet.size());
                info = new TaskInfo(cat, entry.getKey());
                taskInfoMap.put(entry.getKey(), info);
            } else {
                LOG.warn("Unexpected task re-definition: {}", value("taskName", entry.getKey()));
            }
        }

        // Gather remote data for the target languages
        Map<String, Set<String>> langByTask = new HashMap<>();
        LanguageUtil.rosettaSet().parallelStream().forEach(language -> {
            var taskLang = LanguageUtil.rosettaToLanguage(language);
            try {
                var langSet = RemoteUtil.harvest(language);

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
            } catch (CompletionException e) {
                LOG.error("Failed to complete task for: " + taskLang, e);
            } catch (Exception e) {
                LOG.error("Unexpected error encountered fetching data for: " + taskLang, e);
            }
        });

        // Aggregate remote data into task information
        for (var entry : langByTask.entrySet()) {
            var info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                var langSet = entry.getValue();
                int cat = MiscUtil.choice(langSet.size() > 1, 3, 4);
                info = new TaskInfo(cat, entry.getKey());
                info.getLanguageSet().addAll(entry.getValue());
                taskInfoMap.put(entry.getKey(), info);
            } else {
                info.getLanguageSet().addAll(entry.getValue());
            }
        }

        // Update task information with in-progress solutions
        for (var entry : pendingMap.entrySet()) {
            var info = taskInfoMap.get(entry.getKey());
            if (null != info) {
                var langTime = entry.getValue();

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
            } else {
                LOG.error("Failed to retrieve data about pending task: {}", value("task", entry.getKey()));
            }
        }

        removeLanguages(taskInfoMap);

        // Add notes to aid in selecting a language to try out for a task
        addNotes(taskInfoMap);

        // Change the default priority of some tasks to make some stand out in various ways
        adjustPriority(taskInfoMap, pendingMap);

        // Write summary information for manipulation, filtering, and analysis
        SpreadsheetWriter.writeReport(taskInfoMap.values(), langStatMap);
        HtmlWriter.writeReport(taskInfoMap.values());
    }

    private static void removeLanguages(Map<String, TaskInfo> taskInfoMap) {
        //remove languages from tasks that are going to be too challenging at this time
        removeLanguage(taskInfoMap, "Arithmetic_coding/As_a_generalized_change_of_radix", "C");
        removeLanguage(taskInfoMap, "Arithmetic_coding/As_a_generalized_change_of_radix", "C++");
        removeLanguage(taskInfoMap, "Arithmetic_coding/As_a_generalized_change_of_radix", "Lua");
        removeLanguage(taskInfoMap, "Arithmetic-geometric_mean/Calculate_Pi", "Lua");
        removeLanguage(taskInfoMap, "Base58Check_encoding", "C");
        removeLanguage(taskInfoMap, "Base58Check_encoding", "C++");
        removeLanguage(taskInfoMap, "Base58Check_encoding", "Lua");
        removeLanguage(taskInfoMap, "Check_output_device_is_a_terminal", "Groovy");
        removeLanguage(taskInfoMap, "Check_output_device_is_a_terminal", "Java");
        removeLanguage(taskInfoMap, "De_Bruijn_sequences", "C");
        removeLanguage(taskInfoMap, "Eertree", "C");
        removeLanguage(taskInfoMap, "N-smooth_numbers", "C");
    }

    private static void removeLanguage(Map<String, TaskInfo> taskInfoMap, String task, String language) {
        var taskInfo = taskInfoMap.get(task);
        if (null != taskInfo) {
            var languageSet = taskInfo.getLanguageSet();
            languageSet.remove(language);
        }
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
        addNote(taskInfoMap, "Chemical_calculator", DYNAMIC_MEMORY);
        addNote(taskInfoMap, "Cipolla's_algorithm", BIG_INTEGER);
        addNote(taskInfoMap, "Cramer's_rule", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Create_a_file_on_magnetic_tape", FILE_IO);
        addNote(taskInfoMap, "De_Bruijn_sequences", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Eertree", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Egyptian_fractions", BIG_INTEGER);
        addNote(taskInfoMap, "Fermat_numbers", BIG_INTEGER + SLASH + "prime test");
        addNote(taskInfoMap, "Get_system_command_output", "process io");
        addNote(taskInfoMap, "I_before_E_except_after_C", FILE_IO);
        addNote(taskInfoMap, "Knuth's_power_tree", BIG_DECIMAL);
        addNote(taskInfoMap, "Kosaraju", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Lah_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Lucky_and_even_lucky_numbers", "commandline arguments");
        addNote(taskInfoMap, "Magic_squares_of_doubly_even_order", DYNAMIC_MEMORY);
        addNote(taskInfoMap, "Make_directory_path", FILE_IO);
        addNote(taskInfoMap, "Markov_chain_text_generator", FILE_IO);
        addNote(taskInfoMap, "Mersenne_primes", BIG_INTEGER);
        addNote(taskInfoMap, "Metallic_ratios", BIG_DECIMAL);
        addNote(taskInfoMap, "Montgomery_reduction", BIG_INTEGER);
        addNote(taskInfoMap, "N-body_problem", FILE_IO);
        addNote(taskInfoMap, "N-smooth_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Narcissist", FILE_IO);
        addNote(taskInfoMap, "Pell's_equation", BIG_INTEGER);
        addNote(taskInfoMap, "Pierpont_primes", BIG_INTEGER + SLASH + "prime test");
        addNote(taskInfoMap, "Rare_numbers", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Sierpinski_pentagon", IMAGE_IO + SLASH + FILE_IO);
        addNote(taskInfoMap, "Square_root_by_hand", BIG_INTEGER);
        addNote(taskInfoMap, "Suffix_tree", NESTED_FUNCTIONS);
        addNote(taskInfoMap, "Super-d_numbers", BIG_INTEGER);
        addNote(taskInfoMap, "Tonelli-Shanks_algorithm", BIG_INTEGER);
        addNote(taskInfoMap, "Word_break_problem", DYNAMIC_MEMORY);
        addNote(taskInfoMap, "Write_entire_file", FILE_IO);
        addNote(taskInfoMap, "Write_to_Windows_event_log", "windows");
        addNote(taskInfoMap, "Zumkeller_numbers", DYNAMIC_MEMORY);
    }

    private static void addNote(Map<String, TaskInfo> taskInfoMap, String taskName, String note) {
        var info = taskInfoMap.get(taskName);
        if (null == info) {
            LOG.error("Unknown task [{}] for adding a note", value("taskName", taskName));
        } else {
            info.setNote(note);
        }
    }

    private static void adjustPriority(Map<String, TaskInfo> taskInfoMap, Map<String, Pair<String, FileTime>> pendingMap) {
        Map<String, String> solAddMap = new TreeMap<>();
        //solAddMap.put("", "");

        //Arithmetic_coding/*
        //Powerful_numbers (there seems to be something missing from the description to properly show the set)

        // C
        //solAddMap.put("Cyclotomic_Polynomial", "C");       todo maybe later
        //solAddMap.put("Particle_Swarm_Optimization", "C"); maybe later
        solAddMap.put("Smallest_square_that_begins_with_n", "C");
        solAddMap.put("Special_factorials", "C");
        solAddMap.put("Strange_unique_prime_triplets", "C");
        //solAddMap.put("", "C");
        // C++
        //solAddMap.put("Chat_server", "C++");
        solAddMap.put("Multiple_regression", "C++");
        solAddMap.put("Sequence:_nth_number_with_exactly_n_divisors", "C++");
        solAddMap.put("Superpermutation_minimisation", "C++");
        //solAddMap.put("", "C++");
        // C#
        //solAddMap.put("Birthday_problem", "C#");
        //solAddMap.put("Casting_out_nines", "C#");
        //solAddMap.put("Square-free_integers", "C#");
        //solAddMap.put("Super-d_numbers", "C#");
        //solAddMap.put("", "C#");
        // Visual Basic .NET
        //solAddMap.put("Birthday_problem", "Visual Basic .NET");
        solAddMap.put("Circular_primes", "Visual Basic .NET");
        solAddMap.put("Text_between", "Visual Basic .NET");
        solAddMap.put("Visualize_a_tree", "Visual Basic .NET");
        //solAddMap.put("", "Visual Basic .NET");

        // D
        //solAddMap.put("Fermat_numbers", "D"); todo need to figure out what is going wrong
        solAddMap.put("Nice_primes", "D");
        solAddMap.put("Sum_of_divisors", "D");
        solAddMap.put("XXXX_redacted", "D");
        //solAddMap.put("", "D");
        // LLVM
        //solAddMap.put("", "LLVM");
        //solAddMap.put("", "LLVM");
        //solAddMap.put("", "LLVM");
        // Lua
        solAddMap.put("Brace_expansion", "Lua");
        solAddMap.put("Rare_numbers", "Lua");
        solAddMap.put("Tau_function", "Lua");
        //solAddMap.put("", "Lua");
        // Perl
        //solAddMap.put("", "Perl");
        //solAddMap.put("", "Perl");
        //solAddMap.put("", "Perl");
        // Ruby
        //solAddMap.put("Cyclotomic_Polynomial", "Ruby");
        solAddMap.put("First_power_of_2_that_has_leading_decimal_digits_of_12", "Ruby");
        solAddMap.put("Peaceful_chess_queen_armies", "Ruby");
        solAddMap.put("Self_numbers", "Ruby");
        //solAddMap.put("", "Ruby");

        // Groovy
        solAddMap.put("De_Bruijn_sequences", "Groovy");
        solAddMap.put("Floyd-Warshall_algorithm", "Groovy");
        solAddMap.put("Fraction_reduction", "Groovy");
        solAddMap.put("Sorting_algorithms/Cycle_sort", "Groovy");
        //solAddMap.put("", "Groovy");
        // Java
        solAddMap.put("Pseudo-random_numbers/Combined_recursive_generator_MRG32k3a", "Java");
        solAddMap.put("Tau_number", "Java");
        solAddMap.put("Two_bullet_roulette", "Java");
        solAddMap.put("Two_identical_strings", "Java");
        //solAddMap.put("", "Java");
        // Kotlin
        solAddMap.put("Rosetta_Code/Find_bare_lang_tags", "Kotlin");
        solAddMap.put("Sum_of_the_digits_of_n_is_substring_of_n", "Kotlin");
        solAddMap.put("Three_word_location", "Kotlin");
        //solAddMap.put("Weather_Routing", "Kotlin"); //todo not quite working...
        //solAddMap.put("", "Kotlin");
        // Scala
        //solAddMap.put("", "Scala");

        //Pseudo-random_numbers/Combined_recursive_generator_MRG32k3a

        var incFunc = new DoubleSupplier() {
            private final AtomicInteger nextCat = new AtomicInteger(170);

            @Override
            public double getAsDouble() {
                int tmp = nextCat.incrementAndGet();
                if (tmp % 10 == 0) {
                    tmp = nextCat.incrementAndGet();
                }
                return 0.01 * tmp;
            }
        };

        Map<String, Double> solCatMap = new HashMap<>();
        solCatMap.put("C", incFunc.getAsDouble());                    //vs (*)
        solCatMap.put("Kotlin", incFunc.getAsDouble());               //id
        solCatMap.put("D", incFunc.getAsDouble());                    //np
        solCatMap.put("C++", incFunc.getAsDouble());                  //vs
        solCatMap.put("Java", incFunc.getAsDouble());                 //id
        solCatMap.put("Lua", incFunc.getAsDouble());                  //np
        solCatMap.put("Visual Basic .NET", incFunc.getAsDouble());    //vs
        solCatMap.put("Groovy", incFunc.getAsDouble());               //id
        solCatMap.put("Ruby", incFunc.getAsDouble());                 //np

        //solCatMap.put("Perl", incFunc.getAsDouble());                 //np
        //solCatMap.put("C#", incFunc.getAsDouble());                   //vs
        //solCatMap.put("Scala", incFunc.getAsDouble());                //id

        //solCatMap.put("LLVM", incFunc.getAsDouble());                 //np

        // Prioritize some tasks so that there is more than one task with the same prefix
        taskInfoMap.entrySet()
            .stream()
            .filter(entry -> {
                var key = entry.getKey();
                return StringUtils.startsWithAny(key,
                    "Arithmetic_coding"
                ) && !StringUtils.equalsAny(key,
                    "Arithmetic_coding/As_a_generalized_change_of_radix"
                ) || solAddMap.containsKey(key);
            })
            .map(Entry::getValue)
            .forEach(data -> {
                var taskName = data.getTaskName();
                if (solAddMap.containsKey(taskName)) {
                    var language = solAddMap.get(taskName);
                    if (data.getLanguageSet().contains(language)) {
                        if (pendingMap.containsKey(taskName)) {
                            var langFileTime = pendingMap.get(taskName);
                            var lang = langFileTime.getKey();
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
                            if (data.getCategory() != 1.0) {
                                // only overwrite the category if it does not require another solution to balance
                                data.setCategory(solCatMap.getOrDefault(language, data.getCategory()));
                            }
                            data.setNext("try with " + language);
                        }
                    } else {
                        LOG.warn(
                            "No longer need to provide a solution to task [{}] using {}",
                            value("taskName", taskName), value("language", language)
                        );
                    }
                } else if (0 < data.getCategory() && data.getCategory() < 3) {
                    LOG.warn("No longer need to process task [{}]", value("taskName", taskName));
                }
            });
    }
}
