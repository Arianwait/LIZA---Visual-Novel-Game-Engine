package kz.aws.game.panel;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Реестр игровых панелей. Аналог {@code GameActionRegistry}.
 *
 * <p>Автоматически находит все классы в пакете {@code kz.aws.game.panel},
 * помеченные {@link GamePanel @GamePanel} и наследующие {@link BaseGamePanel}.
 * Регистрация происходит при первом вызове {@link #create(String)}.
 *
 * <p>Добавить новую панель: создать класс (в любом подпакете {@code kz.aws.game.panel}),
 * пометить {@link GamePanel @GamePanel} — регистрация автоматическая.
 */
public final class PuzzleRegistry {

    private static final String PANEL_PACKAGE = "kz.aws.game.panel";
    private static final Map<String, Class<? extends BaseGamePanel>> PANELS = new HashMap<>();
    private static boolean discovered = false;

    private PuzzleRegistry() {}

    /**
     * Создаёт новый экземпляр панели по её id.
     *
     * @param panelId значение {@link GamePanel#id()}
     * @return новый экземпляр или {@code null} если id не найден
     */
    public static BaseGamePanel create(String panelId) {
        ensureDiscovered();
        Class<? extends BaseGamePanel> cls = PANELS.get(panelId);
        if (cls == null) {
            System.err.println("PuzzleRegistry: панель не найдена: " + panelId);
            return null;
        }
        try {
            return cls.getConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("PuzzleRegistry: ошибка создания " + cls.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Сканирует пакет {@value #PANEL_PACKAGE} и регистрирует все найденные панели.
     * Вызывается автоматически при первом обращении к {@link #create(String)}.
     */
    public static void discover() {
        if (discovered) return;
        Reflections reflections = new Reflections(PANEL_PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(GamePanel.class);
        for (Class<?> c : annotated) {
            if (c.isInterface() || Modifier.isAbstract(c.getModifiers())) continue;
            if (!BaseGamePanel.class.isAssignableFrom(c)) continue;
            GamePanel ann = c.getAnnotation(GamePanel.class);
            if (ann == null || ann.id().isEmpty()) continue;
            @SuppressWarnings("unchecked")
            Class<? extends BaseGamePanel> panelClass = (Class<? extends BaseGamePanel>) c;
            PANELS.put(ann.id(), panelClass);
        }
        discovered = true;
    }

    private static void ensureDiscovered() {
        if (!discovered) discover();
    }

    /**
     * Неизменяемый снимок зарегистрированных панелей.
     * Полезно для отладки: {@code PuzzleRegistry.getRegistered().keySet()}.
     */
    public static Map<String, Class<? extends BaseGamePanel>> getRegistered() {
        ensureDiscovered();
        return Collections.unmodifiableMap(PANELS);
    }
}
