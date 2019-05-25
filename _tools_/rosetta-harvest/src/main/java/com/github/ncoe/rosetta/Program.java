package com.github.ncoe.rosetta;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import com.github.ncoe.rosetta.io.ExcelWriter;
import com.github.ncoe.rosetta.io.HtmlWriter;
import com.github.ncoe.rosetta.util.LocalUtil;
import com.github.ncoe.rosetta.util.RemoteUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Program {
    public static void main(String[] args) {
        generate();
    }

    private static String fixLangName(String language) {
        switch (language) {
            case "C_sharp":
                return "C#";
            case "F_Sharp":
                return "F#";
            case "Visual_Basic_.NET":
                return "Visual Basic .NET";
            default:
                return language;
        }
    }

    private static void generate() {
        Set<String> languages = Set.of(
            "C",
//            "C++",
            "C_sharp",
            "D",
            "F_Sharp",
            "Java",
            "Kotlin",
            "Lua",
            "Modula-2",
            "Perl",
            "Python",
            "Visual_Basic_.NET"
        );

        Map<String, Long> langStatMap;
        Map<String, String> pendingMap;
        Map<String, Set<String>> localTaskMap;
        try {
            localTaskMap = LocalUtil.classifyCurrent();
            pendingMap = LocalUtil.pendingSolutions();
            langStatMap = LocalUtil.languageStats();
        } catch (IOException e) {
            throw new UtilException(e);
        }

        Map<String, TaskInfo> taskInfoMap = new HashMap<>();
        for (Entry<String, Set<String>> entry : localTaskMap.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                Set<String> langSet = entry.getValue();
                int cat = langSet.size() > 1 ? 2 : 1;
                info = new TaskInfo(cat, entry.getKey());
                taskInfoMap.put(entry.getKey(), info);
            } else {
                System.err.printf("Unexpected task re-definition: %s\n", entry.getKey());
            }
        }

        Map<String, Set<String>> langByTask = new HashMap<>();
        for (String language : languages) {
            String taskLang = fixLangName(language);

            Set<String> langSet = RemoteUtil.harvest(language);
            for (String taskName : langSet) {
                if (langByTask.containsKey(taskName)) {
                    langSet = langByTask.get(taskName);
                } else {
                    langSet = new HashSet<>();
                    langByTask.put(taskName, langSet);
                }
                langSet.add(taskLang);
            }
        }

        for (Entry<String, Set<String>> entry : langByTask.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null == info) {
                Set<String> langSet = entry.getValue();
                int cat = langSet.size() > 1 ? 3 : 4;
                info = new TaskInfo(cat, entry.getKey());
                info.getLanguageSet().addAll(entry.getValue());
                taskInfoMap.put(entry.getKey(), info);
            } else {
                info.getLanguageSet().addAll(entry.getValue());
            }
        }

        for (Entry<String, String> entry : pendingMap.entrySet()) {
            TaskInfo info = taskInfoMap.get(entry.getKey());
            if (null != info) {
                info.setCategory(0);
                info.setNext(entry.getValue());
            }
        }

        ExcelWriter.writeReport(taskInfoMap.values(), langStatMap);
        HtmlWriter.writeReport(taskInfoMap.values());
    }
}
