package com.github.ncoe.rosetta.dto;

/**
 * Holds related forms of a language.
 */
public class LanguageInfo {
    private String language;
    private String directoryName;
    private String fileExtension;
    private String className;

    /**
     * Builder method.
     *
     * @param language      the language name
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     * @return the new info
     */
    public static LanguageInfo of(String language, String directoryName, String fileExtension, String className) {
        return new LanguageInfo(language, directoryName, fileExtension, className);
    }

    /**
     * @param language      the language name
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     */
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
