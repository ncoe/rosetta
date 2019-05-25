package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.exception.UtilException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LocalUtil {
    private static final boolean validateTaskName = false;

    private LocalUtil() {
        throw new NotImplementedException("No LocalUtil for you!");
    }

    private static String getBasePath() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("git", "rev-parse", "--show-toplevel");
        Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        return br.readLine();
    }

    private static String directoryToTask(String directory) {
        String name;
        switch (directory) {
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
                name = directory;
        }

        //todo replace with a property check
        if (validateTaskName) {
            RemoteUtil.validateTaskName(name);
        }

        return name;
    }

    private static String fixLanguage(String lang) {
        switch (lang) {
            case "Cpp":
                return "C++";
            case "CS":
                return "C#";
            case "d":
                System.err.println("Case fix needed for D.");
                return "D";
            case "FS":
                return "F#";
            case "Modula2":
                System.err.println("Name fix needed for Modula-2.");
                return "Modula-2";
            default:
                return lang;
        }
    }

    private static Set<String> collectLanguageList(Path taskPath) {
        try {
            return Files.find(taskPath, 1, (path, bfa) -> Files.isDirectory(path))
                .filter(p -> !Objects.equals(taskPath, p))
                .map(Path::getFileName)
                .map(Path::toString)
                .map(LocalUtil::fixLanguage)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    private static Map<String, Set<String>> currentSolutions(String basePathStr) throws IOException {
        Path basePath = Paths.get(basePathStr);
        return new TreeMap<>(
            Files.find(basePath, 1, (path, bfa) -> Files.isDirectory(path)).filter(p -> {
                String fileName = p.getFileName().toString();
                return !StringUtils.equalsAny(fileName, "_tools_", ".idea", ".git")
                    && !Objects.equals(basePath, p);
            }).collect(Collectors.toMap(pk -> {
                String dir = pk.getFileName().toString();
                return directoryToTask(dir);
            }, LocalUtil::collectLanguageList))
        );
    }

    public static Map<String, Set<String>> classifyCurrent() {
        try {
            String basePath = getBasePath();
            return currentSolutions(basePath);
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

    private static void addLanguageStat(Map<String, Long> langMap, Path fullPath) throws IOException {
        String fullPathStr = fullPath.toString();
        if (!StringUtils.containsAny(fullPathStr, "/_tools_/", "\\_tools_\\", ".gitignore", ".gitattributes", "LICENSE", "submit.template")) {
            Path fileName = fullPath.getFileName();
            String fileNameStr = fileName.toString();
            String extension = StringUtils.substringAfterLast(fileNameStr, ".").toUpperCase();

            String language;
            switch (extension) {
                case "C":
                    language = "C";
                    break;
                case "CPP":
                    language = "C++";
                    break;
                case "CS":
                    language = "C#";
                    break;
                case "D":
                    language = "D";
                    break;
                case "FS":
                    language = "F#";
                    break;
                case "JAVA":
                    language = "Java";
                    break;
                case "KT":
                    language = "Kotlin";
                    break;
                case "LUA":
                    language = "Lua";
                    break;
                case "MOD":
                    language = "Modula-2";
                    break;
                case "PL":
                    language = "Perl";
                    break;
                case "PY":
                    language = "Python";
                    break;
                case "VB":
                    language = "Visual Basic .NET";
                    break;
                case "":
                case "JSON":
                case "MD":
                case "YML":
                    return;
                default:
                    System.err.printf("Unknown file extension for %s\n", fileNameStr);
                    return;
            }

            long size = Files.size(fullPath);
            langMap.merge(language, size, Long::sum);
        }
    }

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

    private static Pair<String, String> extractSolution(Path path) {
        String fileName = path.getFileName().toString();
        String extension = StringUtils.substringAfterLast(fileName, ".");
        if ("txt".equalsIgnoreCase(extension)) {
            return null;
        } else {
            StringBuilder taskName = null;
            String language = null;

            for (Path p : path) {
                String str = p.toString();
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

                if (StringUtils.equalsAny(str, "C", "D", "Java", "Kotlin", "Lua", "Modula-2", "Perl", "Python", "Visual Basic .NET")) {
                    language = str;
                    break;
                }

                if (null == taskName) {
                    taskName = new StringBuilder(str);
                } else {
                    taskName.append("/").append(str);
                }
            }

            if (null == taskName) {
                return null;
            }

            if (null == language) {
                System.err.printf("<UNKNOWN language> for %s\n", taskName);
                return null;
            }

            return Pair.of(taskName.toString(), language);
        }
    }

    public static Map<String, String> pendingSolutions() throws IOException {
        Path basePath = Paths.get(getBasePath());
        Path currentPath = Paths.get("").toAbsolutePath();

        ProcessBuilder builder = new ProcessBuilder("git", "status", "-u");
        Process process = builder.start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        Map<String, String> taskMap = new HashMap<>();

        String line;
        while (null != (line = br.readLine())) {
            if (line.length() > 0 && line.charAt(0) == '\t' && !StringUtils.startsWithAny(line, "\tnew file:", "\tmodified:")) {
                Path fullPath = currentPath.resolve(line.substring(1)).normalize();
                if (!"_tools_".equals(fullPath.getRoot().toString())) {
                    Path relativePath = basePath.relativize(fullPath);
                    Pair<String, String> solution = extractSolution(relativePath);
                    if (null != solution) {
                        taskMap.put(solution.getKey(), solution.getValue());
                    }
                }
            }
        }

        return taskMap;
    }
}
