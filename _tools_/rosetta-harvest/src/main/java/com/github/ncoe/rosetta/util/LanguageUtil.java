package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.dto.LanguageInfo;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common place to handle manipulation among the various forms of the solution languages.
 */
public final class LanguageUtil {
    /**
     * List of languages to do solutions for, or to consider solutions for.
     */
    public static final List<LanguageInfo> LANG_INFO = List.of(
//        LanguageInfo.of("", "", "", ""),
        LanguageInfo.of("ALGOL", "ALGOL", "alg", "algol", false),   //http://rosettacode.org/wiki/Category:ALGOL_68
        LanguageInfo.of("Batch_File", "Batch", "bat", "batch", true),
        LanguageInfo.of("C", "C", "c", "clang"),
        LanguageInfo.of("C++", "Cpp", "cpp", "cpp"),
        LanguageInfo.of("C#", "C_sharp", "CS", "cs", "csharp", false),
//      "Clojure",                //http://rosettacode.org/wiki/Category:Clojure (JVM language)
        LanguageInfo.of("COBOL", "COBOL", "cob", "cobol", false),
        LanguageInfo.of("D", "D", "d", "dlang"),
//      "Eiffel",                 //http://rosettacode.org/wiki/Factorial#Eiffel (.NET CLI language)
        LanguageInfo.of("F#", "F_Sharp", "FS", "fs", "fsharp", false),
//      "Factor",                 //http://rosettacode.org/wiki/Category:Factor (like forth)
        LanguageInfo.of("Fortran", "Fortran", "for", "fortran", false),
        LanguageInfo.of("Go", "Go", "go", "go", true), //http://rosettacode.org/wiki/Category:Go
        LanguageInfo.of("Groovy", "Groovy", "groovy", "groovy"),
        LanguageInfo.of("Java", "Java", "java", "java_"),
        LanguageInfo.of("JavaScript", "JavaScript", "js", "javascript", false),
        LanguageInfo.of("Julia", "Julia", "jl", "julia", true),
        LanguageInfo.of("Kotlin", "Kotlin", "kt", "kotlin"),
        LanguageInfo.of("Lisp", "Lisp", "lisp", "lisp", false), //http://rosettacode.org/wiki/Category:Lisp
        LanguageInfo.of("LLVM", "LLVM", "ll", "llvm", false),
        LanguageInfo.of("Lua", "Lua", "lua", "lua"),
        LanguageInfo.of("Modula-2", "Modula-2", "mod", "modula2", false),
//      "Oxygene",                //http://rosettacode.org/wiki/Category:Oxygene (JVM Language)
        LanguageInfo.of("Pascal", "Pascal", "pas", "pascal", false),    //http://rosettacode.org/wiki/Category:Pascal
        LanguageInfo.of("Perl", "Perl", "pl", "perl", true),
        LanguageInfo.of("PowerShell", "PowerShell", "ps1", "powershell", true),
        LanguageInfo.of("Python", "Python", "py", "python", true),
        LanguageInfo.of("Ruby", "Ruby", "ruby", "ruby", true), //http://rosettacode.org/wiki/Category:Ruby
        LanguageInfo.of("Rust", "Rust", "rs", "rust", true),   //http://rosettacode.org/wiki/Category:Rust
        LanguageInfo.of("Scala", "Scala", "scala", "scala", true),
//      "Microsoft_Small_Basic",  //http://rosettacode.org/wiki/Category:Microsoft_Small_Basic (.NET CLI language)
        // Standard ML
        LanguageInfo.of("TypeScript", "TypeScript", "ts", "typescript", false), //http://rosettacode.org/wiki/Category:TypeScript
        LanguageInfo.of("Visual Basic .NET", "Visual_Basic_.NET", "Visual Basic .NET", "vb", "vbnet", true)
    );

    private LanguageUtil() {
        throw new NotImplementedException("No LanguageUtil for you!");
    }

    /**
     * @return the set of languages and understood by rosetta code for articles
     */
    public static Set<String> rosettaSet() {
        return LANG_INFO.stream()
            .filter(LanguageInfo::getHarvest)
            .map(LanguageInfo::getRosetta)
            .collect(Collectors.toSet());
    }

    /**
     * @param name the name to classify
     * @return true of the given string matches a known language directory
     */
    public static boolean isLanguageDirectory(String name) {
        return LANG_INFO.stream()
            .anyMatch(li -> StringUtils.equals(li.getDirectoryName(), name));
    }

    /**
     * @param name the directory form of a language
     * @return the natural form of the language (the identity for anything else)
     */
    public static String directoryToLanguage(String name) {
        Optional<String> langOpt = LANG_INFO.stream()
            .filter(li -> StringUtils.equals(li.getDirectoryName(), name))
            .map(LanguageInfo::getLanguage)
            .findAny();

        if (langOpt.isPresent()) {
            return langOpt.get();
        } else {
            System.err.printf("[LanguageUtil] Unknown language: %s\n", name);
            return name;
        }
    }

    /**
     * @param ext the extension to consider
     * @return the language most associated with the given extension
     */
    public static String extensionToLanguage(String ext) {
        if (StringUtils.equalsAnyIgnoreCase(ext,
            "", "css", "htm", "json", "md", "png", "xml", "yml")
        ) {
            return null;
        }

        Optional<String> langOpt = LANG_INFO.stream()
            .filter(li -> StringUtils.equalsIgnoreCase(li.getFileExtension(), ext))
            .map(LanguageInfo::getLanguage)
            .findAny();

        if (langOpt.isPresent()) {
            return langOpt.get();
        } else {
            System.err.printf("[LanguageUtil] Unknown file extension: %s\n", ext);
            return null;
        }
    }

    /**
     * @param language the language to translate
     * @return the css class to use for filtering
     */
    public static String classForLanguage(String language) {
        Optional<String> classOpt = LANG_INFO.stream()
            .filter(li -> StringUtils.equals(li.getLanguage(), language))
            .map(LanguageInfo::getClassName)
            .findAny();

        if (classOpt.isPresent()) {
            return classOpt.get();
        } else {
            System.err.printf("[LanguageUtil] unknown class name for %s\n", language);
            return null;
        }
    }

    /**
     * @param language the form rosetta code uses for page definitions
     * @return the standard from of the language name
     */
    public static String rosettaToLanguage(String language) {
        return LANG_INFO.stream()
            .filter(li -> StringUtils.equals(li.getRosetta(), language))
            .map(LanguageInfo::getLanguage)
            .findFirst()
            .orElse(language);
    }
}
