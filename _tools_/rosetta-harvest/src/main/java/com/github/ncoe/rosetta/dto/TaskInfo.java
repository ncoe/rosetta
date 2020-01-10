package com.github.ncoe.rosetta.dto;

import java.nio.file.attribute.FileTime;
import java.util.Set;
import java.util.TreeSet;

/**
 * Information gathered regarding a task.
 */
public class TaskInfo implements Comparable<TaskInfo> {
    private double category;
    private String taskName;
    private Set<String> languageSet;
    private String next;
    private FileTime lastModified;
    private String note;

    /**
     * @param category the category to use
     * @param taskName the name of the task
     */
    public TaskInfo(double category, String taskName) {
        this.category = category;
        this.taskName = taskName;
        this.languageSet = new TreeSet<>();
    }

    public double getCategory() {
        return this.category;
    }

    public void setCategory(double category) {
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

    /**
     * @param other the task to compare this one to
     * @return the result of the comparison
     */
    public int compareTo(TaskInfo other) {
        int compare = Double.compare(this.category, other.category);
        if (compare == 0) {
            if (this.category == 0 && null != this.lastModified && null != other.lastModified) {
                return this.lastModified.compareTo(other.lastModified);
            } else {
                return this.taskName.compareTo(other.taskName);
            }
        } else {
            return compare;
        }
    }
}
