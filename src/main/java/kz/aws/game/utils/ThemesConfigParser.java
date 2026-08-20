package kz.aws.game.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.background.BackgroundDispatcher;

/**
 * Парсер конфигурации тем интерфейса из JSON.
 * Список тем задаётся в lib/config/ThemesConfig.json; стили по id — в style.css (.theme-{id}).
 */
public class ThemesConfigParser {

    private static final String CONFIG_PATH = "lib/config/ThemesConfig.json";

    /**
     * Запись одной темы из конфига.
     * background — путь к фону главного меню/настроек/выбора глав (PNG, GIF и т.д.); пусто — подставляется из MainMenu.
     */
    public static class ThemeEntry {
        private final String id;
        private final String name;
        private final String background;

        public ThemeEntry(String id, String name, String background) {
            this.id = id != null ? id.trim() : "";
            this.name = name != null ? name.trim() : id;
            this.background = background != null ? background.trim() : "";
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        /** Путь к фону (без префикса file:); пустая строка — использовать фон из MainMenu.xml. */
        public String getBackground() {
            return background;
        }
    }

    private static List<ThemeEntry> cachedThemes;
    private static boolean initialized = false;

    /**
     * Возвращает список тем из конфига. При ошибке чтения — список по умолчанию (hitech, classic, walk, dnd, fire).
     */
    @SuppressWarnings("deprecation")
	public static List<ThemeEntry> getThemes() {
        if (initialized && cachedThemes != null) {
            return new ArrayList<>(cachedThemes);
        }
        try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(
                ResourceLocator.resolve(CONFIG_PATH), java.nio.charset.StandardCharsets.UTF_8)) {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            JSONObject root = (JSONObject) obj;
            Object themesObj = root.get("themes");
            if (!(themesObj instanceof JSONArray)) {
                cachedThemes = getDefaultThemes();
                initialized = true;
                return new ArrayList<>(cachedThemes);
            }
            JSONArray arr = (JSONArray) themesObj;
            List<ThemeEntry> list = new ArrayList<>();
            for (Object item : arr) {
                if (!(item instanceof JSONObject)) continue;
                JSONObject o = (JSONObject) item;
                String id = o.get("id") != null ? o.get("id").toString() : "";
                String name = o.get("name") != null ? o.get("name").toString() : id;
                String background = o.get("background") != null ? o.get("background").toString() : "";
                if (!id.isEmpty()) {
                    list.add(new ThemeEntry(id, name, background));
                }
            }
            if (list.isEmpty()) {
                cachedThemes = getDefaultThemes();
            } else {
                cachedThemes = list;
            }
            initialized = true;
            return new ArrayList<>(cachedThemes);
        } catch (IOException | ParseException | ClassCastException e) {
            System.err.println("ThemesConfig: could not read " + CONFIG_PATH + ", using defaults: " + e.getMessage());
            cachedThemes = getDefaultThemes();
            initialized = true;
            return new ArrayList<>(cachedThemes);
        }
    }

    /**
     * Список тем по умолчанию (если конфиг отсутствует или пуст).
     */
    private static List<ThemeEntry> getDefaultThemes() {
        String defaultBg = "lib/MainMenuImg/Back (2).png";
        List<ThemeEntry> list = new ArrayList<>();
        list.add(new ThemeEntry("hitech", "Хайтек", defaultBg));
        list.add(new ThemeEntry("classic", "Классический", defaultBg));
        list.add(new ThemeEntry("walk", "Прогулка", defaultBg));
        list.add(new ThemeEntry("dnd", "DnD", defaultBg));
        list.add(new ThemeEntry("fire", "Огненный", defaultBg));
        return list;
    }

    /**
     * Возвращает тему по id или null.
     */
    public static ThemeEntry getThemeById(String themeId) {
        if (themeId == null || themeId.isEmpty()) return null;
        for (ThemeEntry t : getThemes()) {
            if (themeId.equals(t.getId())) return t;
        }
        return null;
    }

    /**
     * Устанавливает фон главного меню/настроек/выбора глав на корень сцены по текущей теме.
     * Путь к фону берётся из ThemesConfig (поле background); если пусто — из MainMenu.xml.
     * Поддерживаются PNG, JPG, GIF (для GIF используется первый кадр).
     */
    public static void applyMenuBackgroundToRoot(AppSettings appSettings) {
        if (appSettings == null) return;
        StackPane root = appSettings.getRoot();
        if (root == null) return;
        // Убираем инлайн-стиль с корня (чёрный фон от логотипа), иначе он перекрывает картинку фона
        root.setStyle("");
        String path = null;
        ThemeEntry theme = getThemeById(appSettings.getUiTheme());
        if (theme != null && theme.getBackground() != null && !theme.getBackground().isEmpty()) {
            path = theme.getBackground();
        }
        if (path == null || path.isEmpty()) {
            MainMenuConfigParser.MainMenuConfig config = MainMenuConfigParser.getConfig();
            String fallback = config != null ? config.backgroundImagePath : null;
            if (fallback != null && !fallback.isEmpty()) {
                path = fallback.startsWith("file:") ? fallback.substring(5) : fallback;
            }
        }
        if (path == null || path.isEmpty()) return;
        try {
            BackgroundDispatcher bgDispatch = new BackgroundDispatcher();
            // Сначала пробуем взять уже загруженное изображение из кеша — тогда фон появляется сразу
            String pathWithFile = path.startsWith("file:") ? path : "file:" + path;
            Image cached = MenuResourceCache.getImage(pathWithFile);
            if (cached == null) cached = MenuResourceCache.getImage(path);
            if (cached != null && cached.getProgress() >= 1.0 && !cached.isError()) {
                Background bg = bgDispatch.getBackgroundFromImage(cached);
                if (bg != null) {
                    root.setBackground(bg);
                    return;
                }
            }
            Background bg = bgDispatch.getBackground(path);
            if (bg != null) root.setBackground(bg);
        } catch (Exception e) {
            System.err.println("ThemesConfig: could not set menu background from " + path + ": " + e.getMessage());
        }
    }

    /**
     * Сбрасывает кеш (например, после смены конфига без перезапуска).
     */
    public static void clearCache() {
        initialized = false;
        cachedThemes = null;
    }
}
