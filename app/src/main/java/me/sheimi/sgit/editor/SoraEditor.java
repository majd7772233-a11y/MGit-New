package me.sheimi.sgit.editor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.tm4e.core.registry.IThemeSource;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import me.sheimi.android.utils.Profile;
import timber.log.Timber;

/**
 * Bootstraps sora-editor's TextMate engine: file provider, themes and grammars.
 * Theme loading is fast and may run on the UI thread; the heavier grammar
 * parsing runs once, on a background thread at app startup.
 */
public final class SoraEditor {

    public static final String LIGHT_THEME = "quietlight";
    public static final String DARK_THEME = "darcula";

    private static final String THEME_ASSET_PREFIX = "textmate/";
    private static final String GRAMMAR_REGISTRY_FILE = "textmate/languages.json";

    private static volatile boolean sThemesReady;
    private static volatile boolean sGrammarsReady;
    private static final List<Runnable> sPendingLanguageTasks = new CopyOnWriteArrayList<>();

    private SoraEditor() {
    }

    /**
     * Fully prepares the TextMate engine. Intended for the background thread
     * started at app startup.
     */
    public static void prepareInBackground(Context context) {
        ensureThemes(context);
        loadGrammars(context);
    }

    /**
     * Runs the given task once the TextMate grammars are available. If they are
     * already loaded the task runs immediately on the calling thread (expected to
     * be the UI thread); otherwise it is queued and posted to the main thread
     * when loading finishes. Used to (re)apply syntax highlighting to editors
     * opened before grammar loading completed.
     */
    public static void whenGrammarsReady(Runnable task) {
        if (sGrammarsReady) {
            task.run();
            return;
        }
        sPendingLanguageTasks.add(task);
    }

    /**
     * Builds a color scheme matching the current app theme (light or dark).
     * Safe to call from the UI thread.
     */
    public static TextMateColorScheme createColorScheme(Context context) {
        ensureThemes(context);
        ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
        themeRegistry.setTheme(Profile.getEditorTheme(context));
        return TextMateColorScheme.create(themeRegistry);
    }

    private static synchronized void ensureThemes(Context context) {
        if (sThemesReady) {
            return;
        }
        Context app = context.getApplicationContext();
        try {
            FileProviderRegistry.getInstance()
                    .addFileProvider(new AssetsFileResolver(app.getAssets()));
            ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
            themeRegistry.loadTheme(loadTheme(app, LIGHT_THEME));
            themeRegistry.loadTheme(loadTheme(app, DARK_THEME));
            sThemesReady = true;
        } catch (Exception e) {
            Timber.e(e, "Failed to load sora-editor TextMate themes");
        }
    }

    private static synchronized void loadGrammars(Context context) {
        if (sGrammarsReady) {
            return;
        }
        try {
            GrammarRegistry.getInstance().loadGrammars(GRAMMAR_REGISTRY_FILE);
            sGrammarsReady = true;
            flushPendingLanguageTasks();
        } catch (Exception e) {
            Timber.e(e, "Failed to load sora-editor TextMate grammars");
        }
    }

    private static void flushPendingLanguageTasks() {
        if (sPendingLanguageTasks.isEmpty()) {
            return;
        }
        Handler mainHandler = new Handler(Looper.getMainLooper());
        for (Runnable task : sPendingLanguageTasks) {
            mainHandler.post(task);
        }
        sPendingLanguageTasks.clear();
    }

    private static ThemeModel loadTheme(Context context, String name) throws IOException {
        String path = THEME_ASSET_PREFIX + name + ".json";
        try (InputStream is = context.getAssets().open(path)) {
            ThemeModel model = new ThemeModel(IThemeSource.fromInputStream(is, path, null), name);
            model.setDark(DARK_THEME.equals(name));
            return model;
        }
    }
}
