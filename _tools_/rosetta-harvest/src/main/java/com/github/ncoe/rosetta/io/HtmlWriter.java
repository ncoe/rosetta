package com.github.ncoe.rosetta.io;

import com.github.ncoe.rosetta.dto.TaskInfo;
import com.github.ncoe.rosetta.exception.UtilException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HtmlWriter {
    private HtmlWriter() {
        throw new NotImplementedException("No HtmlWriter for you!");
    }

    private static String buildClasses(Set<String> languageSet) {
        StringBuilder sb = new StringBuilder();

        for (String language : languageSet) {
            switch (language) {
                case "C":
                    sb.append(" clang");
                    break;
                case "C++":
                    sb.append(" cpp");
                    break;
                case "C#":
                    sb.append(" csharp");
                    break;
                case "D":
                    sb.append(" dlang");
                    break;
                case "F#":
                    sb.append(" fsharp");
                    break;
                case "Java":
                    sb.append(" java");
                    break;
                case "Kotlin":
                    sb.append(" kotlin");
                    break;
                case "Lua":
                    sb.append(" lua");
                    break;
                case "Modula-2":
                    sb.append(" modula2");
                    break;
                case "Perl":
                    sb.append(" perl");
                    break;
                case "Python":
                    sb.append(" python");
                    break;
                case "Visual Basic .NET":
                    sb.append(" vbnet");
                    break;
                default:
                    System.err.printf("Unknown class for language: %s\n", language);
                    break;
            }
        }

        return sb.toString();
    }

    private static void writeCss(Writer writer) throws IOException {
        writer.write("table, th, td {\n");
        writer.write("    border: 1px solid black;\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write(".container {\n");
        writer.write("    margin-top: 20px;\n");
        writer.write("    overflow: hidden;\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write("/* Style the buttons */\n");
        writer.write(".btn {\n");
        writer.write("    border: none;\n");
        writer.write("    outline: none;\n");
        writer.write("    padding: 12px 16px;\n");
        writer.write("    background-color: #f1f1f1;\n");
        writer.write("    cursor: pointer;\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write(".btn:hover {\n");
        writer.write("    background-color: #ddd;\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write(".btn.activeTask, .btn.activeLang {\n");
        writer.write("    background-color: #666;\n");
        writer.write("    color: white;\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write(".taskFilter {\n");
        writer.write("    display: none;\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write(".show {\n");
        writer.write("    display: table-row;\n");
        writer.write("}\n");
    }

    private static void writeJavaScript(Writer writer) throws IOException {
        writer.write("filterTaskSelection(\"all\")\n");
        writer.write("function filterTaskSelection(c) {\n");
        writer.write("    var lang = document.getElementsByClassName(\"activeLang\")[0].id;\n");
        writer.write("    var x, i;\n");
        writer.write("    x = document.getElementsByClassName(\"taskFilter\");\n");
        writer.write("    if (c == \"all\") c = \"\";\n");
        writer.write("    for (i = 0; i < x.length; i++) {\n");
        writer.write("        w3RemoveClass(x[i], \"show\");\n");
        writer.write("        if (x[i].className.indexOf(c) > -1) {\n");
        writer.write("            if (lang === \"allLang\" || x[i].className.indexOf(lang) > -1) {\n");
        writer.write("                w3AddClass(x[i], \"show\");\n");
        writer.write("            }\n");
        writer.write("        }\n");
        writer.write("    }\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write("filterLangSelection(\"all\")\n");
        writer.write("function filterLangSelection(c) {\n");
        writer.write("    var task = document.getElementsByClassName(\"activeTask\")[0].id;\n");
        writer.write("    var x, i;\n");
        writer.write("    x = document.getElementsByClassName(\"taskFilter\");\n");
        writer.write("    if (c == \"all\") c = \"\";\n");
        writer.write("    for (i = 0; i < x.length; i++) {\n");
        writer.write("        w3RemoveClass(x[i], \"show\");\n");
        writer.write("        if (x[i].className.indexOf(c) > -1) {\n");
        writer.write("            if (task === \"allTask\" || x[i].className.indexOf(task) > -1) {\n");
        writer.write("                w3AddClass(x[i], \"show\");\n");
        writer.write("            }\n");
        writer.write("        }\n");
        writer.write("    }\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write("function w3AddClass(element, name) {\n");
        writer.write("    var i, arr1, arr2;\n");
        writer.write("    arr1 = element.className.split(\" \");\n");
        writer.write("    arr2 = name.split(\" \");\n");
        writer.write("    for (i = 0; i < arr2.length; i++) {\n");
        writer.write("        if (arr1.indexOf(arr2[i]) == -1) {element.className += \" \" + arr2[i];}\n");
        writer.write("    }\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write("function w3RemoveClass(element, name) {\n");
        writer.write("    var i, arr1, arr2;\n");
        writer.write("    arr1 = element.className.split(\" \");\n");
        writer.write("    arr2 = name.split(\" \");\n");
        writer.write("    for (i = 0; i < arr2.length; i++) {\n");
        writer.write("        while (arr1.indexOf(arr2[i]) > -1) {\n");
        writer.write("            arr1.splice(arr1.indexOf(arr2[i]), 1);\n");
        writer.write("        }\n");
        writer.write("    }\n");
        writer.write("    element.className = arr1.join(\" \");\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write("// Add active class to the current task filter button (highlight it)\n");
        writer.write("var taskBtnContainer = document.getElementById(\"taskBtnContainer\");\n");
        writer.write("var taskBtns = taskBtnContainer.getElementsByClassName(\"btn\");\n");
        writer.write("for (var i = 0; i < taskBtns.length; i++) {\n");
        writer.write("    taskBtns[i].addEventListener(\"click\", function(){\n");
        writer.write("        var current = document.getElementsByClassName(\"activeTask\");\n");
        writer.write("        current[0].className = current[0].className.replace(\" activeTask\", \"\");\n");
        writer.write("        this.className += \" activeTask\";\n");
        writer.write("    });\n");
        writer.write("}\n");
        writer.write("\n");
        writer.write("// Add active class to the current language filter button (highlight it)\n");
        writer.write("var langBtnContainer = document.getElementById(\"langBtnContainer\");\n");
        writer.write("var langBtns = langBtnContainer.getElementsByClassName(\"btn\");\n");
        writer.write("for (var i = 0; i < langBtns.length; i++) {\n");
        writer.write("    langBtns[i].addEventListener(\"click\", function(){\n");
        writer.write("        var current = document.getElementsByClassName(\"activeLang\");\n");
        writer.write("        current[0].className = current[0].className.replace(\" activeLang\", \"\");\n");
        writer.write("        this.className += \" activeLang\";\n");
        writer.write("    });\n");
        writer.write("}\n");
    }

    public static void writeReport(Collection<TaskInfo> taskInfoCollection) {
        List<TaskInfo> taskList = taskInfoCollection.stream()
            .filter((task) -> !task.getLanguageSet().isEmpty())
            .sorted()
            .collect(Collectors.toList());

        Path path = Paths.get("target", "rosetta.html");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("  <head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <title>Task Summary</title>\n");
            writer.write("    <style>\n");
            writeCss(writer);
            writer.write("    </style>\n");
            writer.write("  </head>\n");
            writer.write("  <body>\n");
            writer.write("  <div id=\"taskBtnContainer\">\n");
            writer.write("    <button id=\"allTask\" class=\"btn activeTask\" onclick=\"filterTaskSelection('all')\">Show all</button>\n");
            writer.write("    <button id=\"TaskType1\" class=\"btn\" onclick=\"filterTaskSelection('TaskType1')\">One Solution</button>\n");
            writer.write("    <button id=\"TaskType2\" class=\"btn\" onclick=\"filterTaskSelection('TaskType2')\">Multiple Solutions</button>\n");
            writer.write("    <button id=\"TaskType3\" class=\"btn\" onclick=\"filterTaskSelection('TaskType3')\">Multiple Options</button>\n");
            writer.write("    <button id=\"TaskType4\" class=\"btn\" onclick=\"filterTaskSelection('TaskType4')\">One Option</button>\n");
            writer.write("  </div>\n");
            writer.write("  <div id=\"langBtnContainer\">\n");
            writer.write("    <button id=\"allLang\" class=\"btn activeLang\" onclick=\"filterLangSelection('all')\">Show all</button>\n");
            writer.write("    <button id=\"clang\" class=\"btn\" onclick=\"filterLangSelection('clang')\">C</button>\n");
            writer.write("    <button id=\"cpp\" class=\"btn\" onclick=\"filterLangSelection('cpp')\">C++</button>\n");
            writer.write("    <button id=\"csharp\" class=\"btn\" onclick=\"filterLangSelection('csharp')\">C#</button>\n");
            writer.write("    <button id=\"dlang\" class=\"btn\" onclick=\"filterLangSelection('dlang')\">D</button>\n");
            writer.write("    <button id=\"fsharp\" class=\"btn\" onclick=\"filterLangSelection('fsharp')\">F#</button>\n");
            writer.write("    <button id=\"java\" class=\"btn\" onclick=\"filterLangSelection('java')\">Java</button>\n");
            writer.write("    <button id=\"kotlin\" class=\"btn\" onclick=\"filterLangSelection('kotlin')\">Kotlin</button>\n");
            writer.write("    <button id=\"lua\" class=\"btn\" onclick=\"filterLangSelection('lua')\">Lua</button>\n");
            writer.write("    <button id=\"modula2\" class=\"btn\" onclick=\"filterLangSelection('modula2')\">Modula-2</button>\n");
            writer.write("    <button id=\"perl\" class=\"btn\" onclick=\"filterLangSelection('perl')\">Perl</button>\n");
            writer.write("    <button id=\"python\" class=\"btn\" onclick=\"filterLangSelection('python')\">Python</button>\n");
            writer.write("    <button id=\"vbnet\" class=\"btn\" onclick=\"filterLangSelection('vbnet')\">Visual Basic .NET</button>\n");
            writer.write("  </div>\n");
            writer.write("  <div class=\"container\">");
            writer.write("    <table>\n");
            writer.write("      <tr>\n");
            writer.write("        <th>Task Type</th>\n");
            writer.write("        <th>Task Name</th>\n");
            writer.write("        <th>Open Languages</th>\n");
            writer.write("      </tr>\n");

            for (TaskInfo info : taskList) {
                String classStr = buildClasses(info.getLanguageSet());
                switch (info.getCategory()) {
                    case 0:
                        writer.write("<tr>\n");
                        writer.write("<td>In Progress</td>\n");
                        break;
                    case 1:
                        writer.write("<tr class=\"taskFilter TaskType1");
                        writer.write(classStr);
                        writer.write("\">\n");
                        writer.write("  <td>One Solution</td>\n");
                        break;
                    case 2:
                        writer.write("<tr class=\"taskFilter TaskType2");
                        writer.write(classStr);
                        writer.write("\">\n");
                        writer.write("  <td>Multiple Solutions</td>\n");
                        break;
                    case 3:
                        writer.write("<tr class=\"taskFilter TaskType3");
                        writer.write(classStr);
                        writer.write("\">\n");
                        writer.write("  <td>Multiple Options</td>\n");
                        break;
                    case 4:
                        writer.write("<tr class=\"taskFilter TaskType4");
                        writer.write(classStr);
                        writer.write("\">\n");
                        writer.write("  <td>One Option</td>\n");
                        break;
                    default:
                        System.err.printf("Unknown category: %d\n", info.getCategory());
                }

                writer.write("  <td>");
                writer.write(info.getTaskName());
                writer.write("  </td>\n");

                StringBuilder sb = new StringBuilder();
                for (String lang : info.getLanguageSet()) {
                    sb.append(" / ");
                    if (StringUtils.equals(lang, info.getNext())) {
                        sb.append("<u>").append(lang).append("</u>");
                    } else {
                        sb.append(lang);
                    }
                }

                writer.write("  <td>");
                writer.write(sb.toString());
                writer.write("  </td>\n");
                writer.write("</tr>\n");
            }

            writer.write("      </table>\n");
            writer.write("    </div>\n");
            writer.write("<script language=\"JavaScript\" type=\"text/javascript\">\n");
            writeJavaScript(writer);
            writer.write("</script>\n");
            writer.write("  </body>\n");
            writer.write("</html>\n");

        } catch (IOException e) {
            throw new UtilException(e);
        }
    }
}
