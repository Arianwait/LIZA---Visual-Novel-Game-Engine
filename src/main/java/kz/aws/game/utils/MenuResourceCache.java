package kz.aws.game.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;

/**
 * Кеш ресурсов МЕНЮ для предотвращения повторной загрузки.
 * Ресурсы меню загружаются один раз при старте (во время логотипа) и остаются в памяти.
 */
public class MenuResourceCache {
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static volatile boolean menuResourcesPreloaded = false;
    private static volatile boolean isLoading = false;

    /**
     * Предзагрузка ресурсов меню без учёта темы (fallback из MainMenu.xml).
     */
    public static void preloadMenuResources() {
        preloadMenuResources((String) null);
    }

    /**
     * Предзагрузка ресурсов меню в фоновом потоке.
     * themeBackgroundPathOptional — путь к фону текущей темы (с file: или без); если не null, загружается первым — при первом показе меню фон появляется сразу.
     */
    public static void preloadMenuResources(String themeBackgroundPathOptional) {
        if (menuResourcesPreloaded || isLoading) {
            return;
        }

        MainMenuConfigParser.MainMenuConfig config = MainMenuConfigParser.getConfig();
        final String pathToPreload = themeBackgroundPathOptional != null && !themeBackgroundPathOptional.isEmpty()
                ? (themeBackgroundPathOptional.startsWith("file:") ? themeBackgroundPathOptional : "file:" + themeBackgroundPathOptional)
                : null;
        isLoading = true;
        System.out.println("Starting background preload of menu resources...");

        Thread preloaderThread = new Thread(() -> {
            try {
                if (pathToPreload != null) {
                    loadImage(pathToPreload);
                }
                loadImage(config.backgroundImagePath);
                loadImage(config.tableImagePath);
                loadImage("file:lib/Logo/logo.png");

                System.out.println("Menu resources preloaded successfully (Background Thread)");
            } catch (Exception e) {
                System.err.println("Error in menu resources preloader: " + e.getMessage());
                e.printStackTrace();
            } finally {
                menuResourcesPreloaded = true;
                isLoading = false;
            }
        });
        preloaderThread.setDaemon(true);
        preloaderThread.start();
    }
    
    /**
     * Загрузить изображение в кеш (синхронно, для фонового потока)
     */
    private static void loadImage(String path) {
        if (path == null || path.isEmpty()) return;
        
        if (imageCache.containsKey(path)) {
            return; // Уже загружено
        }
        
        try {
            // backgroundLoading = false потому что мы уже в фоне и хотим дождаться загрузки
            Image image = new Image(path, 0, 0, true, true, false);
            
            // Ждем загрузки (в фоновом потоке это безопасно)
            long startTime = System.currentTimeMillis();
            long timeout = 10000; // 10 секунд таймаут
            
            while (image.getProgress() < 1.0 && !image.isError() 
                    && (System.currentTimeMillis() - startTime) < timeout) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (image.isError()) {
                System.err.println("Error loading image: " + path + " - " + image.getException());
            } else {
                imageCache.put(path, image);
                System.out.println("Loaded: " + path + " (" + (image.getProgress() * 100) + "%)");
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
        }
    }
    
    /**
     * Получить изображение из кеша или загрузить, если его нет
     */
    public static Image getImage(String path) {
        if (path == null || path.isEmpty()) return null;
        
        Image cached = imageCache.get(path);
        if (cached != null) {
            return cached;
        }
        
        // Если нет в кеше, загружаем асинхронно (fallback)
        System.out.println("Warning: Image not in cache, loading async: " + path);
        try {
            Image image = new Image(path, 0, 0, true, true, true);
            imageCache.put(path, image);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
    }
    
    public static boolean isPreloaded() {
        return menuResourcesPreloaded;
    }
    
    public static void clearCache() {
        imageCache.clear();
        menuResourcesPreloaded = false;
        isLoading = false;
    }
    
    public static int getCacheSize() {
        return imageCache.size();
    }
}
