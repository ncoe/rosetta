package com.github.ncoe.rosetta.dto;

/**
 * Holds related forms of a language.
 */
public class LanguageInfo {
    private String language;
    private String rosetta;
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
        return of(language, language, directoryName, fileExtension, className);
    }

    /**
     * Builder method.
     *
     * @param language      the language name
     * @param rosetta       the name used to link to the rosetta code articles
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     * @return the new info
     */
    public static LanguageInfo of(String language, String rosetta, String directoryName, String fileExtension, String className) {
        return new LanguageInfo(language, rosetta, directoryName, fileExtension, className);
    }

    /**
     * @param language      the language name
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     */
    public LanguageInfo(String language, String rosetta, String directoryName, String fileExtension, String className) {
        this.language = language;
        this.rosetta = rosetta;
        this.directoryName = directoryName;
        this.fileExtension = fileExtension;
        this.className = className;
    }

    public String getLanguage() {
        return language;
    }

    public String getRosetta() {
        return rosetta;
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
