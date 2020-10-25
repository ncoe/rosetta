package com.github.ncoe.rosetta.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.Failable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static net.logstash.logback.argument.StructuredArguments.value;

public final class RemoteUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteUtil.class);
    private static final String HEAD = "HEAD";

    private static final Pattern HEAD_PATTERN = Pattern.compile("<span class=\"mw-headline\" id=\"([^\"]+)\">[^<]+</span>");
    private static final Pattern TASK_PATTERN = Pattern.compile("<li><a href=\"/wiki/([^\"]+)\" title=\"[^\"]+\">[^<]+</a></li>");

    private RemoteUtil() {
        throw new AssertionError("No instance for you!");
    }

    /**
     * @param language the language to find the unimplemented tasks for
     * @return the collection of unimplemented tasks for the given language
     */
    public static Set<String> harvest(String language) {
        var client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        var uri = buildUri("Reports:Tasks_not_implemented_in_" + language);
        LOG.info("Requesting data from: {}", uri);

        // prepare the request to the server
        var request = HttpRequest.newBuilder(uri)
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build();

        // send the request and process the response
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofLines()).thenApply(response -> {
            if (LOG.isInfoEnabled()) {
                int statusCode = response.statusCode();
                LOG.info(
                    "Status code for {}: {}",
                    value("language", language),
                    value("statusCode", statusCode)
                );
            }

            Set<String> taskSet = new HashSet<>();
            response.body().forEach(new Consumer<>() {
                private SectionEnum section = SectionEnum.None;

                @Override
                public void accept(String line) {
                    this.section = processLine(this.section, line, taskSet);
                }
            });
            return taskSet;
        }).join();
    }

    private static SectionEnum processLine(SectionEnum section, String line, Set<String> taskSet) {
        SectionEnum nextSection;

        // look for changes in the section headers in the document
        var headMatcher = HEAD_PATTERN.matcher(line);
        if (headMatcher.find()) {
            var head = headMatcher.group(1);
            switch (head) {
                case "Not_implemented":
                    nextSection = SectionEnum.NotImplemented;
                    break;
                case "Draft_tasks_without_implementation":
                    nextSection = SectionEnum.DraftTasks;
                    break;
                case "Requiring_Attention":
                    nextSection = SectionEnum.RequireAttention;
                    break;
                case "Not_Considered":
                    nextSection = SectionEnum.NotConsidered;
                    break;
                case "End_of_List":
                    nextSection = SectionEnum.EndOfList;
                    break;
                default:
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unknown section: {}", value("headline", head));
                    }
                case "Examples":
                case "Other_pages":
                    nextSection = SectionEnum.None;
                    break;
            }
        } else {
            nextSection = section;
        }

        // process the lines in the sections of interest
        if (nextSection == SectionEnum.NotImplemented || nextSection == SectionEnum.DraftTasks) {
            var taskMatcher = TASK_PATTERN.matcher(line);
            if (taskMatcher.find()) {
                var taskName = taskMatcher.group(1);
                var task = URLDecoder.decode(taskName, StandardCharsets.UTF_8);
                taskSet.add(task);
            }
        }

        return nextSection;
    }

    /**
     * @param taskName the name of the task to validate the existence of
     */
    static void validateTaskName(String taskName) {
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        var uri = buildUri(taskName);
        LOG.info("Checking the task name for: {}", uri);

        var request = HttpRequest.newBuilder()
            .uri(uri)
            .method(HEAD, HttpRequest.BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(5))
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            var statusCode = response.statusCode();
            if (LOG.isErrorEnabled() && statusCode != 200) {
                LOG.error(
                    "Unknown status ({}) for task name: {}",
                    value("statusCode", statusCode),
                    value("taskName", taskName)
                );
            }
        }).join();
    }

    private static URI buildUri(String partialPath) {
        var fixSlash = StringUtils.replaceChars(partialPath, '\\', '/');

        var path = "/wiki/" + fixSlash;

        return Failable.call(() -> new URI("http", "rosettacode.org", path, null));
    }

    /**
     * Sections within the document of tasks without implementations
     */
    enum SectionEnum {
        None,
        NotImplemented,
        DraftTasks,
        RequireAttention,
        NotConsidered,
        EndOfList,
    }
}
