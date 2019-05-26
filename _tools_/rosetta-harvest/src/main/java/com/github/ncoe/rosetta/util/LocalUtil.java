package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.exception.UtilException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * For gathering data on the local system about solutions
 */
public class LocalUtil {
    private LocalUtil() {
        throw new NotImplementedException("No LocalUtil for you!");
    }

    /**
     * @return The base path of the current repository (based on the current working directory)
     * @throws IOException if something happens gathering data
     */
    private static String getBasePath() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("git", "rev-parse", "--show-toplevel");
        Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        return br.readLine();
    }

    /**
     * @param directory the directory name for the solution
     * @return the task name according to rosetta code
     */
    private static String directoryToTask(String directory) {
        String name;
        switch (directory) {
            case "Abbreviations_automatic":
                name = "Abbreviations,_automatic";
                break;
            case "Abundant_deficient_and_perfect_number_classifications":
                name = "Abundant,_deficient_and_perfect_number_classifications";
                break;
            case "Cipollas_algorithm":
                name = "Cipolla's_algorithm";
                break;
            case "Eulers_sum_of_powers_conjecture":
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
                name = directory;
        }

        // Optional extra check that the task name is now valid (good for verifying solutions for new tasks)
        if (BooleanUtils.toBoolean(System.getProperty("validateTaskName"))) {
            RemoteUtil.validateTaskName(name);
        }

        return name;
    }

    private static List<Path> processPathForTasks(Path currentPath) {
        List<Path> taskList = new ArrayList<>();
        try {
            Files.walk(currentPath, 1)
                .filter(p -> Files.isDirectory(p))
                .forEach(p -> {
                    Path fileNamePath = p.getFileName();
                    String fileName = fileNamePath.toString();
                    if (LanguageUtil.isLanguageDirectory(fileName)) {
                        // Found another task to dissect later
                        taskList.add(p);
                    } else if (!Objects.equals(currentPath, p)) {
                        // Do not self recurse
                        List<Path> innerTaskList = processPathForTasks(p);
                        taskList.addAll(innerTaskList);
                    }
                });
        } catch (IOException e) {
            throw new UtilException(e);
        }
        return taskList;
    }

    /**
     * @return a map of the tasks that have current solutions, and what languages there are for solutions to each
     * @throws IOException if something happens gathering data
     */
    public static Map<String, Set<String>> classifyCurrent() throws IOException {
        Path basePath = Paths.get(getBasePath());
        return processPathForTasks(basePath)
            .stream()
            .map(basePath::relativize)
            .collect(
                Collectors.toMap(
                    keyPath -> {
                        Path taskPath = keyPath.getParent();
                        String taskDir = taskPath.toString();
                        return directoryToTask(taskDir);
                    },
                    valuePath -> {
                        Path langPath = valuePath.getFileName();
                        String dirLang = langPath.toString();
                        String language = LanguageUtil.directoryToLanguage(dirLang);

                        Set<String> languageSet = new HashSet<>();
                        languageSet.add(language);
                        return languageSet;
                    },
                    (acc, next) -> {
                        acc.addAll(next);
                        return acc;
                    }
                )
            );
    }

    /**
     * @param langMap  a map of languages and sizes of solution implementations
     * @param fullPath the next path to augment the current totals with
     * @throws IOException if something happens gathering data
     */
    private static void addLanguageStat(Map<String, Long> langMap, Path fullPath) throws IOException {
        String fullPathStr = fullPath.toString();

        // Known directories and files that do not need to be considered for tracking metrics
        if (StringUtils.containsAny(fullPathStr, "/_tools_/", "\\_tools_\\", ".gitignore", ".gitattributes", "LICENSE", "submit.template")) {
            return;
        }

        Path fileName = fullPath.getFileName();
        String fileNameStr = fileName.toString();
        String extension = StringUtils.substringAfterLast(fileNameStr, ".").toUpperCase();

        // determine what language the file contributes a solution to
        String language = LanguageUtil.extensionToLanguage(extension);
        if (null != language) {
            // augment the current metrics
            long size = Files.size(fullPath);
            langMap.merge(language, size, Long::sum);
        }
    }

    /**
     * @return a map showing by language the amount of code needed for solutions
     * @throws IOException if something happens gathering data
     */
    public static Map<String, Long> languageStats() throws IOException {
        Path basePath = Paths.get(getBasePath());

        ProcessBuilder builder = new ProcessBuilder("git", "ls-tree", "--full-tree", "-r", "--name-only", "HEAD");
        Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        Map<String, Long> langMap = new HashMap<>();
        String line;

        while (null != (line = br.readLine())) {
            Path fullPath = basePath.resolve(line);
            if (Files.isRegularFile(fullPath)) {
                addLanguageStat(langMap, fullPath);
            }
        }

        return langMap;
    }

    /**
     * @param path the path to examine
     * @return either (task name, language) or null if analysis fails
     */
    private static Pair<String, String> extractSolution(Path path) {
        String fileName = path.getFileName().toString();
        String extension = StringUtils.substringAfterLast(fileName, ".");

        // The text files either server as input to a program, or hold a submission that is waiting to be submitted.
        if ("txt".equalsIgnoreCase(extension)) {
            return null;
        }

        StringBuilder taskName = null;
        String language = null;

        for (Path p : path) {
            String str = p.toString();

            // Special cases where a directory name is avoiding special characters
            if ("Cpp".equals(str)) {
                language = "C++";
                break;
            }
            if ("CS".equals(str)) {
                language = "C#";
                break;
            }
            if ("FS".equals(str)) {
                language = "F#";
                break;
            }

            // All other known languages
            if (StringUtils.equalsAny(str, "C", "D", "Java", "Kotlin", "Lua", "Modula-2", "Perl", "Python", "Visual Basic .NET")) {
                language = str;
                break;
            }

            // Either a new language or a task that has a hierarchy
            if (null == taskName) {
                taskName = new StringBuilder(str);
            } else {
                taskName.append("/").append(str);
            }
        }

        // This should never happen
        assert null != taskName;

        // A new language has been added, or something is non-standard and needs to be corrected
        if (null == language) {
            System.err.printf("[LocalUtil] <UNKNOWN language> for %s\n", taskName);
            return null;
        }

        return Pair.of(taskName.toString(), language);
    }

    /**
     * @return tasks that have a pending solution, and what language the pending solution is written in
     * @throws IOException if something happens gathering data
     */
    public static Map<String, Pair<String, FileTime>> pendingSolutions() throws IOException {
        Path basePath = Paths.get(getBasePath());
        Path currentPath = Paths.get("").toAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder("git", "status", "-u");
        Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        Map<String, Pair<String, FileTime>> taskMap = new HashMap<>();
        String line;

        while (null != (line = br.readLine())) {
            if (line.length() > 0 && line.charAt(0) == '\t' && !StringUtils.startsWithAny(line, "\tnew file:", "\tmodified:", "\trenamed:", "\tdeleted:")) {
                Path fullPath = currentPath.resolve(line.substring(1)).normalize();

                // ignore changes in tooling for this purpose
                if ("_tools_".equals(fullPath.getRoot().toString())) {
                    continue;
                }

                // check if there is a pending solution
                Path relativePath = basePath.relativize(fullPath);
                Pair<String, String> solution = extractSolution(relativePath);
                if (null != solution) {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(fullPath);
                    taskMap.put(solution.getKey(), Pair.of(solution.getValue(), lastModifiedTime));
                }
            }
        }

        return taskMap;
    }
}
