package kz.aws.game.engine.effect;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реестр визуальных эффектов. Автоматически находит и регистрирует
 * все классы с аннотацией {@link VisualEffect @VisualEffect}
 * в пакете {@value #EFFECT_PACKAGE}.
 *
 * <p>Добавить новый эффект: создать класс, реализующий {@link VisualEffectPlayer},
 * пометить {@link VisualEffect @VisualEffect("myEffect")} — регистрация автоматическая.
 */
public final class VisualEffectRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(VisualEffectRegistry.class);

    private static final String EFFECT_PACKAGE = "kz.aws.game.engine.effect";
    private static final Map<String, VisualEffectPlayer> PLAYERS = new HashMap<>();
    private static boolean discovered = false;

    private VisualEffectRegistry() {}

    /**
     * Возвращает плеер для указанного типа эффекта.
     *
     * @param effectType идентификатор эффекта (blink, zoom, colorFilter и др.)
     * @return экземпляр {@link VisualEffectPlayer} или {@code null} если не найден
     */
    public static VisualEffectPlayer get(String effectType) {
        ensureDiscovered();
        return PLAYERS.get(effectType);
    }

    /**
     * Сканирует пакет {@value #EFFECT_PACKAGE} и регистрирует все найденные эффекты.
     * Вызывается автоматически при первом обращении к {@link #get(String)}.
     */
    public static void discover() {
        if (discovered) return;
        Reflections reflections = new Reflections(EFFECT_PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(VisualEffect.class);
        for (Class<?> c : annotated) {
            registerClass(c);
        }
        discovered = true;
    }

    /**
     * Регистрирует один класс эффекта, если он валиден.
     *
     * @param c класс-кандидат с аннотацией {@link VisualEffect}
     */
    private static void registerClass(Class<?> c) {
        if (c.isInterface() || Modifier.isAbstract(c.getModifiers())) return;
        if (!VisualEffectPlayer.class.isAssignableFrom(c)) return;
        VisualEffect ann = c.getAnnotation(VisualEffect.class);
        if (ann == null || ann.value().isEmpty()) return;
        try {
            VisualEffectPlayer player = (VisualEffectPlayer) c.getConstructor().newInstance();
            PLAYERS.put(ann.value(), player);
        } catch (Exception e) {
            LOG.error("VisualEffectRegistry: ошибка создания " + c.getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void ensureDiscovered() {
        if (!discovered) discover();
    }
}
