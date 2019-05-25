package com.github.ncoe.rosetta.dto;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.builder.CompareToBuilder;

public class TaskInfo implements Comparable<TaskInfo> {
    private int category;
    private String taskName;
    private Set<String> languageSet;
    private String next;

    public TaskInfo(int category, String taskName) {
        this.category = category;
        this.taskName = taskName;
        this.languageSet = new HashSet<>();
    }

    public int getCategory() {
        return this.category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public Set<String> getLanguageSet() {
        return this.languageSet;
    }

    public String getNext() {
        return this.next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public int compareTo(TaskInfo o2) {
        return new CompareToBuilder()
            .append(this.category, o2.category)
            .append(this.taskName, o2.taskName)
            .build();
    }
}
