package com.github.ncoe.rosetta.dto;

/**
 * Holds related forms of a language.
 */
public final class LanguageInfo {
    private final String language;
    private final String rosetta;
    private final String directoryName;
    private final String fileExtension;
    private final String className;
    private final int harvest;

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
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     * @param harvest       true if the language should be harvested for new tasks
     * @return the new info
     */
    public static LanguageInfo of(String language, String directoryName, String fileExtension, String className, int harvest) {
        return of(language, language, directoryName, fileExtension, className, harvest);
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
        return of(language, rosetta, directoryName, fileExtension, className, 0);
    }

    /**
     * Builder method.
     *
     * @param language      the language name
     * @param rosetta       the name used to link to the rosetta code articles
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     * @param harvest       true if the language should be harvested for new tasks
     * @return the new info
     */
    public static LanguageInfo of(String language, String rosetta, String directoryName, String fileExtension, String className, int harvest) {
        return new LanguageInfo(language, rosetta, directoryName, fileExtension, className, harvest);
    }

    /**
     * @param language      the language name
     * @param directoryName the directory representation
     * @param fileExtension the typical file extension
     * @param className     the css class name to use
     * @param harvest       true if the language should be harvested for new tasks
     */
    private LanguageInfo(String language, String rosetta, String directoryName, String fileExtension, String className, int harvest) {
        this.language = language;
        this.rosetta = rosetta;
        this.directoryName = directoryName;
        this.fileExtension = fileExtension;
        this.className = className;
        this.harvest = harvest;
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

    public int getHarvest() {
        return harvest;
    }
}
