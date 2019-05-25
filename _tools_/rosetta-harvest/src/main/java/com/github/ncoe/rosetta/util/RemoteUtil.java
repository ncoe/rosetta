package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.exception.UtilException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For gathering data about tasks that could be worked on
 */
public class RemoteUtil {
    private RemoteUtil() {
        throw new NotImplementedException("No RemoteUtil for you!");
    }

    /**
     * @param name the name of the task to validate the existence of
     */
    public static void validateTaskName(String name) {
        HttpUriRequest request = RequestBuilder.head("http://rosettacode.org/wiki/" + name).build();
        HttpClientBuilder builder = HttpClientBuilder.create();
        CloseableHttpClient client = builder.build();

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                System.err.printf("[RemoteUtil] Unknown status (%d) or task name: %s\n", statusCode, name);
            }
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

    /**
     * @param language the language to find the unimplemented tasks for
     * @return the collection of unimplemented tasks for the given language
     */
    public static Set<String> harvest(String language) {
        // prepare patterns for extracting data from the document
        Pattern headPattern = Pattern.compile("<span class=\"mw-headline\" id=\"([^\"]+)\">[^<]+</span>");
        Pattern taskPattern = Pattern.compile("<li><a href=\"/wiki/([^\"]+)\" title=\"[^\"]+\">[^<]+</a></li>");

        // prepare the request to the server
        String uri = "http://rosettacode.org/wiki/Reports:Tasks_not_implemented_in_" + language;
        HttpUriRequest request = RequestBuilder.get(uri).build();
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(false).build();
        CloseableHttpClient client = builder.setDefaultRequestConfig(config).disableRedirectHandling().build();

        // execute and process the request
        Set<String> taskSet = new HashSet<>();
        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            System.err.printf("[RemoteUtil] Status code for %s: %d - %s\n", language, statusCode, statusLine.getReasonPhrase());
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
                // todo: currently used to track down the issue with harvesting data about c++ tasks
                System.err.printf("[RemoteUtil] target uri: %s\n", uri);
                HeaderIterator it = response.headerIterator();

                while (it.hasNext()) {
                    Header header = it.nextHeader();
                    System.out.printf("[RemoteUtil] Header: %s\n", header);
                }

                return Collections.emptySet();
            }

            // prepare to extract the lines from the response
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            LineIterator li = IOUtils.lineIterator(is, StandardCharsets.UTF_8);

            RemoteUtil.SectionEnum section = RemoteUtil.SectionEnum.None;
            while (li.hasNext()) {
                String line = li.nextLine();

                // look for changes in the section headers in the document
                Matcher headMatcher = headPattern.matcher(line);
                if (headMatcher.find()) {
                    String head = headMatcher.group(1);
                    switch (head) {
                        case "Not_implemented":
                            section = RemoteUtil.SectionEnum.NotImplemented;
                            break;
                        case "Draft_tasks_without_implementation":
                            section = RemoteUtil.SectionEnum.DraftTasks;
                            break;
                        case "Requiring_Attention":
                            section = SectionEnum.RequireAttention;
                            break;
                        case "Not_Considered":
                            section = SectionEnum.NotConsidered;
                            break;
                        case "End_of_List":
                            section = SectionEnum.EndOfList;
                            break;
                        default:
                            System.err.printf("[RemoteUtil] Unknown section: %s\n", head);
                        case "Examples":
                        case "Other_pages":
                            section = RemoteUtil.SectionEnum.None;
                            break;
                    }
                }

                // process the lines in the sections of interest
                if (section == RemoteUtil.SectionEnum.NotImplemented || section == RemoteUtil.SectionEnum.DraftTasks) {
                    Matcher taskMatcher = taskPattern.matcher(line);
                    if (taskMatcher.find()) {
                        String taskName = taskMatcher.group(1);
                        String task = URLDecoder.decode(taskName, StandardCharsets.UTF_8);
                        taskSet.add(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new UtilException(e);
        }

        return taskSet;
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
