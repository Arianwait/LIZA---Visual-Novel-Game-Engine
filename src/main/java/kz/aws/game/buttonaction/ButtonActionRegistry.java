package kz.aws.game.buttonaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import kz.aws.game.appsettings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Регистр действий кнопок по id. Кнопки из Buttons.xml вызывают действие по id через {@link #run(String, AppSettings)}.
 * Новые действия: создай класс с {@link ButtonAction} и реализуй {@link ButtonActionHandler} в пакете {@code kz.aws.game.buttonaction} — регистрация автоматическая.
 */
public final class ButtonActionRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ButtonActionRegistry.class);

    private static final String ACTION_PACKAGE = "kz.aws.game.buttonaction";
    private static final Map<String, ButtonActionHandler> HANDLERS = new HashMap<>();
    private static boolean discovered = false;

    public static void register(String id, ButtonActionHandler handler) {
        if (id != null && handler != null) {
            HANDLERS.put(id, handler);
        }
    }

    public static ButtonActionHandler get(String id) {
        ensureDiscovered();
        return HANDLERS.get(id);
    }

    /**
     * Выполняет действие по id кнопки. Если обработчика нет — ничего не делает.
     */
    public static void run(String id, AppSettings appSettings) {
        ButtonActionHandler h = get(id);
        if (h != null && appSettings != null) {
            h.run(appSettings);
        }
    }

    /**
     * Автоматически находит в пакете {@link #ACTION_PACKAGE} все классы с аннотацией {@link ButtonAction},
     * реализующие {@link ButtonActionHandler}, и регистрирует их по id из аннотации.
     */
    public static void discover() {
        if (discovered) return;
        Reflections reflections = new Reflections(ACTION_PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ButtonAction.class);
        for (Class<?> c : annotated) {
            if (c.isInterface() || !ButtonActionHandler.class.isAssignableFrom(c)) continue;
            ButtonAction ann = c.getAnnotation(ButtonAction.class);
            if (ann == null) continue;
            try {
                ButtonActionHandler handler = (ButtonActionHandler) c.getConstructor().newInstance();
                register(ann.value(), handler);
            } catch (Exception e) {
                LOG.error("ButtonActionRegistry: could not register " + c.getSimpleName() + ": " + e.getMessage());
            }
        }
        discovered = true;
    }

    private static void ensureDiscovered() {
        if (!discovered) discover();
    }
}
