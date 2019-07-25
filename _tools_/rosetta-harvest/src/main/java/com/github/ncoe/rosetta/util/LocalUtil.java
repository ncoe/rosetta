package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.exception.UtilException;
import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.util.Asserts;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private LocalUtil() {
        throw new NotImplementedException("No LocalUtil for you!");
    }

    /**
     * @return the git repository for the project
     * @throws IOException if not repository is found, e.g.
     */
    public static Repository getRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment()
            .findGitDir()
            .build();
    }

    /**
     * @param directory the directory name for the solution
     * @return the task name according to rosetta code
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private static String directoryToTask(String directory) {
        String name;
        switch (directory) {
            case "Abbreviations_automatic":
                name = "Abbreviations,_automatic";
                break;
            case "Abundant_deficient_and_perfect_number_classifications":
                name = "Abundant,_deficient_and_perfect_number_classifications";
                break;
            case "Cheryls_Birthday":
                name = "Cheryl's_Birthday";
                break;
            case "Cipollas_algorithm":
                name = "Cipolla's_algorithm";
                break;
            case "Cramers_rule":
                name = "Cramer's_rule";
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

        return name.replace("\\", "/");
    }

    /**
     * @param currentPath the path to consider
     * @return the list of tasks that have been identified
     */
    private static List<Path> processPathForTasks(Path currentPath) {
        List<Path> taskList = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(currentPath, 1)) {
            pathStream.filter(p -> Files.isDirectory(p))
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
     * @param repository the repository to extract current languages from for each task
     * @return a map of the tasks that have current solutions, and what languages there are for solutions to each
     */
    public static Map<String, Set<String>> classifyCurrent(Repository repository) {
        Path basePath = repository.getWorkTree().toPath();
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
        if (StringUtils.containsAny(fullPathStr,
            ".gitignore", ".gitattributes", "LICENSE", "submit.template", "template.fthl")
        ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Saw the path {} and ignored it", value("filePath", fullPathStr));
            }
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
     * @param repository the repository to walk for language statistics
     * @return a map showing by language the amount of code needed for solutions
     * @throws IOException if something happens gathering data
     */
    public static Map<String, Long> languageStats(Repository repository) throws IOException {
        Path basePath = repository.getWorkTree().toPath();
        Ref head = repository.exactRef("HEAD");

        Map<String, Long> langMap = new HashMap<>();
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(head.getObjectId());
            RevTree tree = commit.getTree();
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                while (treeWalk.next()) {
                    Path fullPath = basePath.resolve(treeWalk.getPathString());
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
    private static Pair<String, String> extractSolution(Path path) {
        String fileName = path.getFileName().toString();
        String extension = StringUtils.substringAfterLast(fileName, ".");

        // The html files help to demonstrate the javascript submissions
        // The text files either server as input to a program, or hold a submission that is waiting to be submitted.
        if (StringUtils.equalsAnyIgnoreCase(extension, "fthl", "htm", "html", "txt")) {
            return null;
        }

        StringBuilder taskName = null;
        String language = null;

        for (Path p : path) {
            String str = p.toString();
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
        Asserts.check(!taskName.toString().contains("\\"), "The paths should be normalized for consistent results.");

        // A new language has been added, or something is non-standard and needs to be corrected
        if (null == language) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unknown language for {} (was null)", value("taskName", taskName));
            }
            return null;
        }

        String taskNameCorrected = directoryToTask(taskName.toString());
        return Pair.of(taskNameCorrected, language);
    }

    /**
     * @param repository the repository to find pending changes in
     * @return tasks that have a pending solution, and what language the pending solution is written in
     */
    public static Pair<Map<String, Pair<String, FileTime>>, Map<String, Long>> pendingSolutions(Repository repository) {
        String baseDirStr = repository.getWorkTree().toString();
        Path basePath = Path.of(baseDirStr);
        Map<String, Pair<String, FileTime>> taskMap = new HashMap<>();
        Map<String, Long> langSizeMap = new HashMap<>();

        try (Git git = new Git(repository)) {
            Status status = git.status().call();
            Set<String> uncommittedSet = status.getUncommittedChanges()
                .stream()
                .filter(p -> !StringUtils.startsWith(p, "_tools_"))
                .collect(Collectors.toSet());
            Set<String> untrackedSet = status.getUntracked()
                .stream()
                .filter(p -> !StringUtils.startsWith(p, "_tools_"))
                .collect(Collectors.toSet());
            uncommittedSet.addAll(untrackedSet);
            uncommittedSet.removeAll(status.getMissing());
            uncommittedSet.removeAll(status.getRemoved());
            uncommittedSet.removeAll(status.getModified());
            if (LOG.isDebugEnabled()) {
                String setStr = StringUtils.join(uncommittedSet, ", ");
                LOG.debug(Markers.append("finalSet", uncommittedSet), "The final set considered is: [{}]", setStr);
            }

            for (String changePathStr : uncommittedSet) {
                Path changePath = Path.of(changePathStr);

                // check if there is a pending solution (taskName, language)
                Pair<String, String> solution = extractSolution(changePath);
                if (null != solution) {
                    Pair<String, FileTime> info = taskMap.get(solution.getKey());
                    if (null == info) {
                        Path fullPath = basePath.resolve(changePath);
                        FileTime lastModifiedTime = Files.getLastModifiedTime(fullPath);
                        long fileSize = Files.size(fullPath);
                        langSizeMap.merge(solution.getValue(), fileSize, Long::sum);
                        // taskName -> (language, modificationTime)
                        taskMap.put(solution.getKey(), Pair.of(solution.getValue(), lastModifiedTime));
                    } else if (LOG.isInfoEnabled()) {
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
            throw new UtilException(e);
        }

        return Pair.of(taskMap, langSizeMap);
    }
}
