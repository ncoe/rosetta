package com.github.ncoe.rosetta.util;

import net.logstash.logback.marker.Markers;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;

/**
 * For gathering data on the local system about solutions.
 */
public final class LocalUtil {
    /**
     * Output directory name.
     */
    public static final String OUTPUT_DIRECTORY = "out";

    private static final Logger LOG = LoggerFactory.getLogger(LocalUtil.class);

    private final Map<String, Integer> directoryMap = new ConcurrentHashMap<>();
    private boolean checkTaskName = false;

    /**
     * @param checkTaskName true if task names should be verified
     */
    public void setCheckTaskName(boolean checkTaskName) {
        this.checkTaskName = checkTaskName;
    }

    /**
     * @return the git repository for the project
     * @throws IOException if not repository is found, e.g.
     */
    public Repository getRepository() throws IOException {
        var builder = new FileRepositoryBuilder();
        return builder.readEnvironment()
            .findGitDir()
            .build();
    }

    /**
     * @param directory the directory name for the solution
     * @return the task name according to rosetta code
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private String directoryToTask(String directory) {
        String name = switch (directory) {
            case "Abbreviations_automatic" -> "Abbreviations,_automatic";
            case "Abundant_deficient_and_perfect_number_classifications" -> "Abundant,_deficient_and_perfect_number_classifications";
            case "Cheryls_Birthday" -> "Cheryl's_Birthday";
            case "Cipollas_algorithm" -> "Cipolla's_algorithm";
            case "Cramers_rule" -> "Cramer's_rule";
            case "Eulers_sum_of_powers_conjecture" -> "Euler's_sum_of_powers_conjecture";
            case "Faulhabers_formula" -> "Faulhaber's_formula";
            case "Faulhabers_triangle" -> "Faulhaber's_triangle";
            case "Floyds_triangle" -> "Floyd's_triangle";
            case "Horners_rule_for_polynomial_evaluation" -> "Horner's_rule_for_polynomial_evaluation";
            case "Knuths_power_tree" -> "Knuth's_power_tree";
            case "Pascals_triangle" -> "Pascal's_triangle";
            case "Pells_equation" -> "Pell's_equation";
            //http://rosettacode.org/wiki/Primality_by_Wilson%27s_theorem#D
            case "Primalty_by_Wilsons_theorem" -> "Primalty_by_Wilson's_theorem";
            case "Recamans_sequence" -> "Recaman's_sequence";
            case "Sailors_coconuts_and_a_monkey_problem" -> "Sailors,_coconuts_and_a_monkey_problem";
            case "Sequence_nth_number_with_exactly_n_divisors" -> "Sequence:_nth_number_with_exactly_n_divisors";
            default -> directory;
        };

        // Optional extra check that the task name is now valid (good for verifying solutions for new tasks)
        if (this.checkTaskName) {
            this.directoryMap.computeIfAbsent(name, s -> {
                RemoteUtil.validateTaskName(name);
                return 1;
            });
        }

        return name.replace("\\", "/");
    }

    /**
     * @param currentPath the path to consider
     * @return the list of tasks that have been identified
     */
    private static List<Path> processPathForTasks(Path currentPath) {
        List<Path> taskList = new ArrayList<>();
        try (var pathStream = Files.walk(currentPath, 1)) {
            pathStream.filter(Files::isDirectory)
                .forEach(p -> {
                    var fileNamePath = p.getFileName();
                    var fileName = fileNamePath.toString();
                    if (LanguageUtil.isLanguageDirectory(fileName)) {
                        // Found another task to dissect later
                        taskList.add(p);
                    } else if (!Objects.equals(currentPath, p)) {
                        // Do not self recurse
                        var innerTaskList = processPathForTasks(p);
                        taskList.addAll(innerTaskList);
                    }
                });
        } catch (IOException e) {
            throw Failable.rethrow(e);
        }
        return taskList;
    }

    /**
     * @param repository the repository to extract current languages from for each task
     * @return a map of the tasks that have current solutions, and what languages there are for solutions to each
     */
    public Map<String, Set<String>> classifyCurrent(Repository repository) {
        var basePath = repository.getWorkTree().toPath();

        return processPathForTasks(basePath)
            .stream()
            .map(basePath::relativize)
            .collect(
                Collectors.toMap(
                    keyPath -> {
                        var taskPath = keyPath.getParent();
                        var taskDir = taskPath.toString();
                        return directoryToTask(taskDir);
                    },
                    valuePath -> {
                        var langPath = valuePath.getFileName();
                        var dirLang = langPath.toString();
                        var language = LanguageUtil.directoryToLanguage(dirLang);

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
        var fullPathStr = fullPath.toString();

        // Known directories and files that do not need to be considered for tracking metrics
        if (StringUtils.containsAny(fullPathStr,
            ".gitignore", ".gitattributes", "LICENSE", "submit.template", "template.fthl", "_fix_")
        ) {
            LOG.debug("Saw the path {} and ignored it", value("filePath", fullPathStr));
            return;
        }

        var fileName = fullPath.getFileName();
        var fileNameStr = fileName.toString();
        var extension = FilenameUtils.getExtension(fileNameStr).toUpperCase();

        // determine what language the file contributes a solution to
        var language = LanguageUtil.extensionToLanguage(extension);
        if (null != language && Files.exists(fullPath)) {
            // augment the current metrics
            long size = Files.size(fullPath);
            langMap.merge(language, size, Long::sum);
        }
    }

    /**
     * @param repository the repository to walk for language statistics
     * @return a map showing by language the amount of code needed for solutions
     * @throws IOException if something happens gathering data
     */
    public Map<String, Long> languageStats(Repository repository) throws IOException {
        var basePath = repository.getWorkTree().toPath();
        Ref head = repository.exactRef("HEAD");

        Map<String, Long> langMap = new HashMap<>();
        try (var revWalk = new RevWalk(repository)) {
            var commit = revWalk.parseCommit(head.getObjectId());
            var tree = commit.getTree();
            try (var treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                    var fullPath = basePath.resolve(treeWalk.getPathString());
                    addLanguageStat(langMap, fullPath);
                }
            }
        }

        return langMap;
    }

    /**
     * @param path the path to examine
     * @return either (task name, language) or null if analysis fails
     */
    private Pair<String, String> extractSolution(Path path) {
        var fileName = path.getFileName().toString();
        var extension = FilenameUtils.getExtension(fileName);

        // The html files help to demonstrate the javascript submissions
        // The text files either server as input to a program, or hold a submission that is waiting to be submitted.
        if (StringUtils.equalsAnyIgnoreCase(extension, "fthl", "htm", "html", "txt")) {
            return null;
        }

        StringBuilder taskName = null;
        String language = null;

        for (var p : path) {
            var str = p.toString();
            if (LanguageUtil.isLanguageDirectory(str)) {
                language = LanguageUtil.directoryToLanguage(str);
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
        Objects.requireNonNull(taskName, "This should not have happened.");
        MiscUtil.assertFalse(taskName.toString().contains("\\"), "The paths should be normalized for consistent results, saw: " + taskName);

        // A new language has been added, or something is non-standard and needs to be corrected
        if (null == language) {
            LOG.error("Unknown language for {} (was null)", value("taskName", taskName));
            return null;
        }

        var taskNameCorrected = directoryToTask(taskName.toString());
        LOG.trace("Identified solved task `{}` for language {}", taskNameCorrected, language);
        return Pair.of(taskNameCorrected, language);
    }

    /**
     * @param repository the repository to find pending changes in
     * @return tasks that have a pending solution, and what language the pending solution is written in
     */
    public Pair<Map<String, Pair<String, FileTime>>, Map<String, Long>> pendingSolutions(Repository repository) {
        var baseDirStr = repository.getWorkTree().toString();
        var basePath = Path.of(baseDirStr);
        Map<String, Pair<String, FileTime>> taskMap = new TreeMap<>();
        Map<String, Long> langSizeMap = new TreeMap<>();

        try (Git git = new Git(repository)) {
            var status = git.status().call();
            var uncommittedSet = status.getUncommittedChanges()
                .stream()
                .filter(p -> !StringUtils.startsWith(p, "_tools_"))
                .collect(Collectors.toSet());
            var untrackedSet = status.getUntracked()
                .stream()
                .filter(p -> !StringUtils.startsWith(p, "_tools_"))
                .collect(Collectors.toSet());
            uncommittedSet.addAll(untrackedSet);
            uncommittedSet.removeAll(status.getMissing());
            uncommittedSet.removeAll(status.getRemoved());
            uncommittedSet.removeAll(status.getModified());

            var setStr = StringUtils.join(uncommittedSet, ", ");
            LOG.debug(Markers.append("finalSet", uncommittedSet), "The final set considered is: [{}]", setStr);

            for (var changePathStr : uncommittedSet) {
                var changePath = Path.of(changePathStr);

                // check if there is a pending solution (taskName, language)
                var solution = extractSolution(changePath);
                if (null != solution) {
                    var info = taskMap.get(solution.getKey());
                    if (null == info || Objects.equals(solution.getValue(), info.getKey())) {
                        var fullPath = basePath.resolve(changePath);
                        var lastModifiedTime = Files.getLastModifiedTime(fullPath);
                        long fileSize = Files.size(fullPath);
                        langSizeMap.merge(solution.getValue(), fileSize, Long::sum);
                        // taskName -> (language, modificationTime)
                        taskMap.put(solution.getKey(), Pair.of(solution.getValue(), lastModifiedTime));
                    } else {
                        LOG.info(
                            "There are multiple solutions for [{}], additionally, {} (Previously saw {})",
                            value("taskName", solution.getKey()),
                            value("language", solution.getValue()),
                            value("language", info.getKey())
                        );
                    }
                }
            }
        } catch (GitAPIException | IOException e) {
            throw Failable.rethrow(e);
        }

        return Pair.of(taskMap, langSizeMap);
    }
}
