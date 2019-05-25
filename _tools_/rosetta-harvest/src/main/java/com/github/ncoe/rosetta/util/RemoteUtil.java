package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.exception.UtilException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class RemoteUtil {
    private RemoteUtil() {
        throw new NotImplementedException("No RemoteUtil for you!");
    }

    public static void validateTaskName(String name) {
        HttpUriRequest request = RequestBuilder.head("http://rosettacode.org/wiki/" + name).build();
        HttpClientBuilder builder = HttpClientBuilder.create();
        CloseableHttpClient client = builder.build();

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                System.err.printf("Unknown status (%d) or task name: %s\n", statusCode, name);
            }
        } catch (IOException e) {
            throw new UtilException(e);
        }
    }

    public static Set<String> harvest(String language) {
        Pattern headPattern = Pattern.compile("<span class=\"mw-headline\" id=\"([^\"]+)\">[^<]+</span>");
        Pattern taskPattern = Pattern.compile("<li><a href=\"/wiki/([^\"]+)\" title=\"[^\"]+\">[^<]+</a></li>");
        Set<String> taskSet = new HashSet<>();
        String uri = "http://rosettacode.org/wiki/Reports:Tasks_not_implemented_in_" + language;
        HttpUriRequest request = RequestBuilder.get(uri).build();
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(false).build();
        CloseableHttpClient client = builder.setDefaultRequestConfig(config).disableRedirectHandling().build();

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            System.err.printf("Status code for %s: %d - %s\n", language, statusCode, statusLine.getReasonPhrase());
            if (statusCode == 301) {
                System.err.println(uri);
                HeaderIterator it = response.headerIterator();

                while(it.hasNext()) {
                    Header header = it.nextHeader();
                    System.out.printf("    %s\n", header);
                }
            }

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            LineIterator li = IOUtils.lineIterator(is, StandardCharsets.UTF_8);
            RemoteUtil.SectionEnum section = RemoteUtil.SectionEnum.None;

            while(li.hasNext()) {
                String line = li.nextLine();
                Matcher headMatcher = headPattern.matcher(line);
                if (headMatcher.find()) {
                    String head = headMatcher.group(1);
                    switch(head) {
                        case "Not_implemented":
                            section = RemoteUtil.SectionEnum.NotImplemented;
                            break;
                        case "Draft_tasks_without_implementation":
                            section = RemoteUtil.SectionEnum.DraftTasks;
                            break;
                        default:
                            section = RemoteUtil.SectionEnum.None;
                            break;
                    }
                }

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

    enum SectionEnum {
        None,
        NotImplemented,
        DraftTasks,
        RequireAttention,
        NotConsidered,
        EndOfList;
    }
}
