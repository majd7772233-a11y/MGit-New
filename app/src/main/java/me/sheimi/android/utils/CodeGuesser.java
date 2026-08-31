package me.sheimi.android.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps file extensions to sora-editor TextMate language scopes.
 * Only languages whose TextMate grammars are bundled in the app are listed.
 * Created by sheimi on 8/23/13.
 */
public class CodeGuesser {

    private final static String[][] FILENAME_EXTENSION_ARRAY = {
            { "C", "source.c", "c" },
            { "C++", "source.cpp", "cc", "cpp", "cxx", "h", "hh", "hpp", "hxx" },
            { "CSS", "source.css", "css", "scss", "sass", "less" },
            { "Go", "source.go", "go" },
            { "HTML", "text.html.basic", "html", "htm", "xhtml" },
            { "Java", "source.java", "java" },
            { "JavaScript", "source.js", "js", "mjs", "cjs", "javascript" },
            { "JSON", "source.json", "json", "jsonc", "json5" },
            { "Kotlin", "source.kotlin", "kt", "kts" },
            { "Lua", "source.lua", "lua" },
            { "Markdown", "text.html.markdown", "md", "markdown", "mdx" },
            { "PHP", "source.php", "php" },
            { "Python", "source.python", "py", "pyw" },
            { "Ruby", "source.ruby", "rb", "rake" },
            { "Rust", "source.rust", "rs" },
            { "Shell", "source.shell", "sh", "bash", "zsh", "csh", "ksh" },
            { "SQL", "source.sql", "sql" },
            { "TypeScript", "source.ts", "ts", "tsx", "mts", "cts" },
            { "XML", "text.xml", "xml" },
            { "YAML", "source.yaml", "yml", "yaml" }, };

    private static Map<String, String> mFilenameExtensionMap = new HashMap<String, String>();
    private static List<String> mSupportLanguageList = new ArrayList<String>();

    private static Map<String, String> mDisplayTagMap = new HashMap<String, String>();

    static {
        for (int i = 0; i < FILENAME_EXTENSION_ARRAY.length; ++i) {
            String[] extensions = FILENAME_EXTENSION_ARRAY[i];
            String display = extensions[0];
            String tag = extensions[1];
            mDisplayTagMap.put(display, tag);
            for (int j = 2; j < extensions.length; ++j) {
                mFilenameExtensionMap.put(extensions[j], tag);
            }
        }
        mSupportLanguageList.addAll(mDisplayTagMap.keySet());
        Collections.sort(mSupportLanguageList);
    }

    public static String guessCodeType(String filename) {
        String[] filesplit = filename.split("\\.");
        if (filesplit.length <= 1)
            return null;
        String extension = filesplit[filesplit.length - 1];
        return mFilenameExtensionMap.get(extension);
    }

    public static List<String> getLanguageList() {
        return mSupportLanguageList;
    }

    public static String getLanguageTag(String language) {
        return mDisplayTagMap.get(language);
    }

    public static String wrapUrlScript(String script) {
        return String.format(URL_SCRIPT_WRAPPER, script);
    }

    public final static String URL_SCRIPT_WRAPPER = "javascript:(function(){%s;})()";

}
