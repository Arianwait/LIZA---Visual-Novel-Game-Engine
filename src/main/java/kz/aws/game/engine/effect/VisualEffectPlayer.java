package kz.aws.game.engine.effect;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import kz.aws.game.engine.model.VisualEffectCommand;

/**
 * Воспроизводит визуальный эффект на сцене.
 *
 * <p>Реализации помечаются {@link VisualEffect @VisualEffect}
 * и автоматически регистрируются в {@link VisualEffectRegistry}.
 *
 * <p>Эффект получает корневой {@code StackPane} сцены и слой контента
 * ({@code sceneContentLayer}) для применения трансформаций/фильтров
 * только к фону и персонажам, без затрагивания UI.
 */
public interface VisualEffectPlayer {

    /**
     * Запускает визуальный эффект.
     *
     * @param root              корневой StackPane сцены (для overlay-эффектов типа blink)
     * @param sceneContentLayer слой фона и персонажей (для zoom, цветовых фильтров)
     * @param command           параметры эффекта (длительность, масштаб и др.)
     * @param onComplete        вызывается по окончании анимации (может быть null)
     */
    void play(StackPane root, Pane sceneContentLayer, VisualEffectCommand command, Runnable onComplete);

    /**
     * Немедленно останавливает эффект и убирает все визуальные артефакты.
     *
     * @param root              корневой StackPane сцены
     * @param sceneContentLayer слой фона и персонажей
     */
    void stop(StackPane root, Pane sceneContentLayer);

    /**
     * Переносит визуальные артефакты эффекта из старого root в новый.
     * По умолчанию ничего не делает — эффекты на sceneContentLayer
     * переносятся автоматически вместе со слоем.
     *
     * @param oldRoot старый корневой StackPane
     * @param newRoot новый корневой StackPane
     */
    default void migrate(StackPane oldRoot, StackPane newRoot) {}
}
