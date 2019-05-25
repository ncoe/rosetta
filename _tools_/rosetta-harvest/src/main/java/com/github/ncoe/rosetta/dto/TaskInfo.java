package com.github.ncoe.rosetta.dto;

import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;

public class TaskInfo implements Comparable<TaskInfo> {
    private int category;
    private String taskName;
    private Set<String> languageSet;
    private String next;
    private FileTime lastModified;
    private String note;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNext() {
        return this.next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public FileTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(FileTime lastModified) {
        this.lastModified = lastModified;
    }

    public int compareTo(TaskInfo o2) {
        int compare = Integer.compare(this.category, o2.category);
        if (compare == 0) {
            if (this.category == 0 && null != this.lastModified && null != o2.lastModified) {
                return this.lastModified.compareTo(o2.lastModified);
            } else {
                return this.taskName.compareTo(o2.taskName);
            }
        } else {
            return compare;
        }
    }
}
