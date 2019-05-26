package com.github.ncoe.rosetta.dto;

public class LanguageInfo {
    private String language;
    private String directoryName;
    private String fileExtension;
    private String className;

    public static LanguageInfo of(String language, String directoryName, String fileExtension, String className) {
        return new LanguageInfo(language, directoryName, fileExtension, className);
    }

    public LanguageInfo(String language, String directoryName, String fileExtension, String className) {
        this.language = language;
        this.directoryName = directoryName;
        this.fileExtension = fileExtension;
        this.className = className;
    }

    public String getLanguage() {
        return language;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getClassName() {
        return className;
    }
}
