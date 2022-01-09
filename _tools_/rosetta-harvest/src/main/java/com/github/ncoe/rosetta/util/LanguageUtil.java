package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.dto.LanguageInfo;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common place to handle manipulation among the various forms of the solution languages.
 */
public final class LanguageUtil {
    private static final Logger LOG = LoggerFactory.getLogger(LanguageUtil.class);

    /**
     * List of languages to do solutions for, or to consider solutions for.
     */
    private static final List<LanguageInfo> LANG_INFO = List.of(
//        LanguageInfo.of("", "", "", ""),
        LanguageInfo.of("ALGOL", "ALGOL", "alg", "algol", -1),   //http://rosettacode.org/wiki/Category:ALGOL_68
        LanguageInfo.of("Batch_File", "Batch", "bat", "batch", -1),
        LanguageInfo.of("C", "C", "c", "clang", 1),
        LanguageInfo.of("C++", "Cpp", "cpp", "cpp", 1),
        LanguageInfo.of("C#", "C_sharp", "CS", "cs", "csharp", 1),
//      "Clojure",                //http://rosettacode.org/wiki/Category:Clojure (JVM language)
        LanguageInfo.of("COBOL", "COBOL", "cob", "cobol", -1),
        LanguageInfo.of("D", "D", "d", "dlang", 1),
//      "Eiffel",                 //http://rosettacode.org/wiki/Factorial#Eiffel (.NET CLI language)
        LanguageInfo.of("F#", "F_Sharp", "FS", "fs", "fsharp", 0),
//      "Factor",                 //http://rosettacode.org/wiki/Category:Factor (like forth)
        LanguageInfo.of("Fortran", "Fortran", "for", "fortran", -1),
        LanguageInfo.of("Go", "Go", "go", "go", 0), //http://rosettacode.org/wiki/Category:Go
        LanguageInfo.of("Groovy", "Groovy", "groovy", "groovy", 1),
        LanguageInfo.of("Java", "Java", "java", "java_", 1),
        LanguageInfo.of("JavaScript", "JavaScript", "js", "javascript", 0),
        LanguageInfo.of("Julia", "Julia", "jl", "julia", -1),
        LanguageInfo.of("Kotlin", "Kotlin", "kt", "kotlin", 1),
        LanguageInfo.of("Lisp", "Lisp", "lisp", "lisp", -1), //http://rosettacode.org/wiki/Category:Lisp
        LanguageInfo.of("LLVM", "LLVM", "ll", "llvm", 0),
        LanguageInfo.of("Lua", "Lua", "lua", "lua", 1),
        LanguageInfo.of("Modula-2", "Modula-2", "mod", "modula2", -1),
//      "Oxygene",                //http://rosettacode.org/wiki/Category:Oxygene (JVM Language)
        LanguageInfo.of("Pascal", "Pascal", "pas", "pascal", 0),    //http://rosettacode.org/wiki/Category:Pascal
        LanguageInfo.of("Perl", "Perl", "pl", "perl", 1),
        LanguageInfo.of("PowerShell", "PowerShell", "ps1", "powershell", -1),
        LanguageInfo.of("Python", "Python", "py", "python", 0),
        LanguageInfo.of("Ruby", "Ruby", "rb", "ruby", 1), //http://rosettacode.org/wiki/Category:Ruby
        LanguageInfo.of("Rust", "Rust", "rs", "rust", 0),   //http://rosettacode.org/wiki/Category:Rust
        LanguageInfo.of("Scala", "Scala", "scala", "scala", 0),
//      "Microsoft_Small_Basic",  //http://rosettacode.org/wiki/Category:Microsoft_Small_Basic (.NET CLI language)
        // Standard ML
        LanguageInfo.of("TypeScript", "TypeScript", "ts", "typescript", -1), //http://rosettacode.org/wiki/Category:TypeScript
        LanguageInfo.of("Visual Basic .NET", "Visual_Basic_.NET", "Visual Basic .NET", "vb", "vbnet", 1)
    );

    private LanguageUtil() {
        throw new NotImplementedException("No instance for you!");
    }

    /**
     * @param lang the language to get information about
     * @return the requested language info, if it exists
     */
    public static Optional<LanguageInfo> find(String lang) {
        return LANG_INFO.stream()
            .filter(info -> StringUtils.equals(info.language(), lang))
            .findFirst();
    }

    /**
     * @return the set of languages and understood by rosetta code for articles
     */
    public static Set<String> rosettaSet() {
        return LANG_INFO.stream()
            .filter(d -> d.harvest() > 0)
            .map(LanguageInfo::rosetta)
            .collect(Collectors.toSet());
    }

    /**
     * @param name the name to classify
     * @return true of the given string matches a known language directory
     */
    public static boolean isLanguageDirectory(String name) {
        return LANG_INFO.stream()
            .anyMatch(li -> StringUtils.equals(li.directoryName(), name));
    }

    /**
     * @param name the directory form of a language
     * @return the natural form of the language (the identity for anything else)
     */
    public static String directoryToLanguage(String name) {
        var langOpt = LANG_INFO.stream()
            .filter(li -> StringUtils.equals(li.directoryName(), name))
            .map(LanguageInfo::language)
            .findAny();

        if (langOpt.isPresent()) {
            return langOpt.get();
        }

        LOG.error("Unknown language: {}", name);
        return name;
    }

    /**
     * @param ext the extension to consider
     * @return the language most associated with the given extension
     */
    public static String extensionToLanguage(String ext) {
        if (StringUtils.equalsAnyIgnoreCase(ext,
            "", "css", "htm", "json", "md", "png", "txt", "xml", "yml")
        ) {
            return null;
        }

        var langOpt = LANG_INFO.stream()
            .filter(li -> StringUtils.equalsIgnoreCase(li.fileExtension(), ext))
            .map(LanguageInfo::language)
            .findAny();

        if (langOpt.isPresent()) {
            return langOpt.get();
        }

        LOG.error("Unknown file extension: {}", ext);
        return null;
    }

    /**
     * @param language the form rosetta code uses for page definitions
     * @return the standard from of the language name
     */
    public static String rosettaToLanguage(String language) {
        return LANG_INFO.stream()
            .filter(li -> StringUtils.equals(li.rosetta(), language))
            .map(LanguageInfo::language)
            .findFirst()
            .orElse(language);
    }

    /**
     * @return a map of the languages to the class to use for filtering in html
     */
    public static Map<String, String> mapLanguageToClass() {
        return LanguageUtil.LANG_INFO
            .stream()
            .filter(lang -> lang.harvest() > 0)
            .collect(Collectors.toMap(
                LanguageInfo::language,
                LanguageInfo::className,
                (e1, e2) -> e2,
                LinkedHashMap::new
            ));
    }
}
