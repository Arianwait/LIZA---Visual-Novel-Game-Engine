package kz.aws.game.gameaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Регистр игровых команд по имени действия. Команды из сценария вызываются через {@link #run(String, GameActionContext)}.
 * Новые команды: класс с {@link GameAction} и реализация {@link GameActionHandler} в пакете {@code kz.aws.game.gameaction} — регистрация автоматическая.
 */
public final class GameActionRegistry {

    private static final String ACTION_PACKAGE = "kz.aws.game.gameaction";
    private static final Map<String, GameActionHandler> HANDLERS = new HashMap<>();
    private static boolean discovered = false;

    public static void register(String actionId, GameActionHandler handler) {
        if (actionId != null && handler != null) {
            HANDLERS.put(actionId, handler);
        }
    }

    public static GameActionHandler get(String actionId) {
        ensureDiscovered();
        return HANDLERS.get(actionId);
    }

    /**
     * Выполняет игровую команду по имени. Если обработчика нет — ничего не делает.
     */
    public static void run(String actionId, GameActionContext ctx) {
        GameActionHandler h = get(actionId);
        if (h != null && ctx != null) {
            h.run(ctx);
        }
    }

    /**
     * Автоматически находит в пакете {@link #ACTION_PACKAGE} все классы с аннотацией {@link GameAction},
     * реализующие {@link GameActionHandler}, и регистрирует их по имени из аннотации.
     */
    public static void discover() {
        if (discovered) return;
        Reflections reflections = new Reflections(ACTION_PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(GameAction.class);
        for (Class<?> c : annotated) {
            if (c.isInterface() || !GameActionHandler.class.isAssignableFrom(c)) continue;
            GameAction ann = c.getAnnotation(GameAction.class);
            if (ann == null) continue;
            try {
                GameActionHandler handler = (GameActionHandler) c.getConstructor().newInstance();
                register(ann.value(), handler);
            } catch (Exception e) {
                System.err.println("GameActionRegistry: could not register " + c.getSimpleName() + ": " + e.getMessage());
            }
        }
        discovered = true;
    }

    private static void ensureDiscovered() {
        if (!discovered) discover();
    }
}
