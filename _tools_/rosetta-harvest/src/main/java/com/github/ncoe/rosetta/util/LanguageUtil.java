package com.github.ncoe.rosetta.util;

import com.github.ncoe.rosetta.dto.LanguageInfo;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Common place to handle manipulation among the various forms of the solution languages.
 */
public final class LanguageUtil {
    /**
     * Languages to use to discover tasks.
     */
    public static final Set<String> LANGUAGES = Set.of(
        "ALGOL",                    //http://rosettacode.org/wiki/Category:ALGOL_68
//        "Clojure",                  //http://rosettacode.org/wiki/Category:Clojure (JVM language)
//        "Eiffel",                   //http://rosettacode.org/wiki/Factorial#Eiffel (.NET CLI language)
        "Go",                       //http://rosettacode.org/wiki/Category:Go
        "Lisp",                     //http://rosettacode.org/wiki/Category:Lisp
//        "Oxygene",                  //http://rosettacode.org/wiki/Category:Oxygene (JVM Language)
        "Pascal",                   //http://rosettacode.org/wiki/Category:Pascal
        "Ruby",                     //http://rosettacode.org/wiki/Category:Ruby
        "Rust",                     //http://rosettacode.org/wiki/Category:Rust
//        "Microsoft_Small_Basic",    //http://rosettacode.org/wiki/Category:Microsoft_Small_Basic (.NET CLI language)
        "TypeScript",               //http://rosettacode.org/wiki/Category:TypeScript
//end of search for languages to consider learning
        "C",
        "C++",
        "C_sharp",      //third language
        "D",            //second, want to make top
        "F_Sharp",
        "Groovy",
        "Java",         //top language
        "JavaScript",
        "Kotlin",
        "LLVM",
        "Lua",
        "Modula-2",     //want to make secondary language
        "Perl",
        "Python",
        "Scala",
        "Visual_Basic_.NET"
    );

    private static final List<LanguageInfo> LANG_INFO = List.of(
        LanguageInfo.of("ALGOL", "ALGOL", "alg", "algol"),
        LanguageInfo.of("C", "C", "c", "clang"),
        LanguageInfo.of("C++", "Cpp", "cpp", "cpp"),
        LanguageInfo.of("C#", "CS", "cs", "csharp"),
        LanguageInfo.of("D", "D", "d", "dlang"),
        LanguageInfo.of("F#", "FS", "fs", "fsharp"),
        LanguageInfo.of("Go", "Go", "go", "go"),
        LanguageInfo.of("Groovy", "Groovy", "groovy", "groovy"),
        LanguageInfo.of("Java", "Java", "java", "java"),
        LanguageInfo.of("JavaScript", "JavaScript", "js", "javascript"),
        LanguageInfo.of("Kotlin", "Kotlin", "kt", "kotlin"),
        LanguageInfo.of("Lisp", "Lisp", "lisp", "lisp"),
        LanguageInfo.of("LLVM", "LLVM", "ll", "llvm"),
        LanguageInfo.of("Lua", "Lua", "lua", "lua"),
        LanguageInfo.of("Modula-2", "Modula-2", "mod", "modula2"),
        LanguageInfo.of("Pascal", "Pascal", "pas", "pascal"),
        LanguageInfo.of("Perl", "Perl", "pl", "perl"),
        LanguageInfo.of("Python", "Python", "py", "python"),
        LanguageInfo.of("Ruby", "Ruby", "ruby", "ruby"),
        LanguageInfo.of("Rust", "Rust", "rs", "rust"),
        LanguageInfo.of("Scala", "Scala", "scala", "scala"),
        LanguageInfo.of("TypeScript", "TypeScript", "ts", "typescript"),
        LanguageInfo.of("Visual Basic .NET", "Visual Basic .NET", "vb", "vbnet")
    );

    private LanguageUtil() {
        throw new NotImplementedException("No LanguageUtil for you!");
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
        switch (language) {
            case "C_sharp":
                return "C#";
            case "F_Sharp":
                return "F#";
            case "Visual_Basic_.NET":
                return "Visual Basic .NET";
            default:
                return language;
        }
    }
}
