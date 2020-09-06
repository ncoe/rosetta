package com.github.ncoe.rosetta.io;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.util.LanguageUtil;
import com.github.ncoe.rosetta.util.LocalUtil;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.function.Failable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * For generating a web page that can easily be filtered to get a new task to work on.
 */
public final class HtmlWriter {
    private HtmlWriter() {
        throw new NotImplementedException("No HtmlWriter for you!");
    }

    /**
     * @param taskInfoCollection the tasks that could use an implementation for a language
     */
    public static void writeReport(Collection<TaskInfo> taskInfoCollection) {
        var templatePath = Path.of("src", "main", "resources");
        var outputPath = Path.of(LocalUtil.OUTPUT_DIRECTORY, "rosetta.html");

        // pre-filter tasks that are in-progress or that have no language to write an implementation for
        var taskList = taskInfoCollection.stream()
            .filter(
                task -> !task.getLanguageSet().isEmpty()
                    && task.getCategory() > 0
            )
            .sorted()
            .collect(Collectors.toList());

        try {
            var cfg = new Configuration(Configuration.VERSION_2_3_27);
            cfg.setDirectoryForTemplateLoading(templatePath.toFile());
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);

            var langMap = LanguageUtil.mapLanguageToClass();
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("langMap", langMap);
            dataMap.put("taskInfoList", taskList);

            var temp = cfg.getTemplate("template.fthl");

            try (var writer = Files.newBufferedWriter(outputPath)) {
                temp.process(dataMap, writer);
            }
        } catch (Exception e) {
            throw Failable.rethrow(e);
        }
    }
}
