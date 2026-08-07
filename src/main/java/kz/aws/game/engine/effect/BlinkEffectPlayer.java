package kz.aws.game.engine.effect;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import kz.aws.game.engine.model.VisualEffectCommand;
import kz.aws.game.utils.VirtualViewport;

/**
 * Эффект моргания (полузакрытые глаза) — устойчивый эффект.
 *
 * <p>Верхнее и нижнее веко имеют дугообразную форму (QuadCurveTo),
 * имитируя реальное моргание глаз. Clip-контейнер обрезает blur по бокам.
 *
 * <p>Фазы: закрытие → пауза → приоткрытие → удержание.
 * Убираются по {@code stopEffect("blink")} или кнопке «назад».
 */
@VisualEffect("blink")
public class BlinkEffectPlayer implements VisualEffectPlayer {

    private static final int ENTRY_DURATION_MS = 800;
    private static final double LID_HOLD_RATIO = 0.15;
    private static final double CLOSE_PHASE = 0.35;
    private static final double HOLD_PHASE = 0.10;
    private static final double OPEN_PHASE = 0.55;
    private static final double BLUR_RADIUS = 40;
    private static final double SIDE_OVERFLOW = 100;
    private static final double OVERFLOW_TOP = 80;
    private static final double CURVE_DEPTH = 0.6;
    private static final double DRIFT_AMPLITUDE = 0.03;
    private static final int DRIFT_CYCLE_MS = 2500;

    private Pane topContainer;
    private Pane bottomContainer;
    private final DoubleProperty topLidHeight = new SimpleDoubleProperty(0);
    private final DoubleProperty bottomLidHeight = new SimpleDoubleProperty(0);
    private double sceneWidth;
    private double clipH;
    private SequentialTransition entryAnimation;
    private Timeline driftAnimation;

    /**
     * Запускает анимацию входа: моргание → веки остаются.
     *
     * @param root              корневой StackPane
     * @param sceneContentLayer слой контента (не используется)
     * @param command           команда эффекта
     * @param onComplete        callback по завершении входа
     */
    @Override
    public void play(StackPane root, Pane sceneContentLayer,
                     VisualEffectCommand command, Runnable onComplete) {
        sceneWidth = VirtualViewport.DESIGN_WIDTH;
        double height = VirtualViewport.DESIGN_HEIGHT;
        double closeHeight = height / 2 + BLUR_RADIUS;
        double holdHeight = height * LID_HOLD_RATIO;
        clipH = height / 2 + BLUR_RADIUS;

        createLids(height);
        addLidsBelowText(root);

        entryAnimation = buildEntryAnimation(closeHeight, holdHeight, onComplete);
        entryAnimation.play();
    }

    /**
     * Мгновенно убирает веки с экрана.
     *
     * @param root              корневой StackPane
     * @param sceneContentLayer слой контента (не используется)
     */
    @Override
    public void stop(StackPane root, Pane sceneContentLayer) {
        if (entryAnimation != null) {
            entryAnimation.stop();
            entryAnimation = null;
        }
        if (driftAnimation != null) {
            driftAnimation.stop();
            driftAnimation = null;
        }
        removeLids(root);
    }

    @Override
    public void migrate(StackPane oldRoot, StackPane newRoot) {
        if (topContainer != null) oldRoot.getChildren().remove(topContainer);
        if (bottomContainer != null) oldRoot.getChildren().remove(bottomContainer);
        if (topContainer != null && bottomContainer != null) {
            addLidsBelowText(newRoot);
        }
    }

    /**
     * Создаёт верхнее и нижнее веко с дугообразной формой.
     *
     * @param sceneHeight высота экрана
     */
    private void createLids(double sceneHeight) {
        double clipW = sceneWidth + SIDE_OVERFLOW * 2;
        double topClipH = clipH + OVERFLOW_TOP;

        topContainer = createLidContainer(clipW, topClipH);
        topContainer.setLayoutX(-SIDE_OVERFLOW);
        topContainer.setLayoutY(-OVERFLOW_TOP);

        bottomContainer = createLidContainer(clipW, clipH + OVERFLOW_TOP);
        bottomContainer.setLayoutX(-SIDE_OVERFLOW);
        bottomContainer.setLayoutY(sceneHeight - clipH);

        topLidHeight.addListener((obs, o, n) -> rebuildTopLid());
        bottomLidHeight.addListener((obs, o, n) -> rebuildBottomLid());
    }

    /**
     * Создаёт clip-контейнер для века.
     *
     * @param width  ширина
     * @param height высота clip-области
     * @return контейнер
     */
    private Pane createLidContainer(double width, double height) {
        Pane container = new Pane();
        container.setMouseTransparent(true);
        container.setManaged(false);
        container.setPrefSize(width, height);
        container.setClip(new Rectangle(width, height));
        return container;
    }

    /**
     * Перестраивает форму верхнего века при изменении высоты.
     * Дуга вогнутая (внутрь) — как настоящее верхнее веко.
     */
    private void rebuildTopLid() {
        double h = topLidHeight.get();
        double w = sceneWidth + SIDE_OVERFLOW * 2;
        topContainer.getChildren().clear();
        if (h <= 0) return;

        double curveY = h * CURVE_DEPTH;
        double baseY = h + OVERFLOW_TOP;
        Path path = new Path();
        path.getElements().addAll(
                new MoveTo(0, 0),
                new LineTo(w, 0),
                new LineTo(w, baseY),
                new QuadCurveTo(w / 2, baseY - curveY, 0, baseY),
                new ClosePath()
        );
        path.setFill(Color.BLACK);
        path.setEffect(new GaussianBlur(BLUR_RADIUS));
        topContainer.getChildren().add(path);
    }

    /**
     * Перестраивает форму нижнего века при изменении высоты.
     * Дуга выпуклая (наружу) — как настоящее нижнее веко.
     */
    private void rebuildBottomLid() {
        double h = bottomLidHeight.get();
        double w = sceneWidth + SIDE_OVERFLOW * 2;
        bottomContainer.getChildren().clear();
        if (h <= 0) return;

        double startY = clipH - h;
        double curveY = h * CURVE_DEPTH;
        double bottomEdge = clipH + OVERFLOW_TOP;
        Path path = new Path();
        path.getElements().addAll(
                new MoveTo(0, startY),
                new QuadCurveTo(w / 2, startY + curveY, w, startY),
                new LineTo(w, bottomEdge),
                new LineTo(0, bottomEdge),
                new ClosePath()
        );
        path.setFill(Color.BLACK);
        path.setEffect(new GaussianBlur(BLUR_RADIUS));
        bottomContainer.getChildren().add(path);
    }

    /**
     * Добавляет контейнеры век в root перед текстовой панелью.
     *
     * @param root корневой StackPane
     */
    private void addLidsBelowText(StackPane root) {
        int size = root.getChildren().size();
        int insertIndex = Math.max(size - 1, 0);
        root.getChildren().add(insertIndex, topContainer);
        root.getChildren().add(insertIndex + 1, bottomContainer);
    }

    /**
     * Анимация входа: закрытие → пауза → приоткрытие.
     *
     * @param closeHeight высота при полном закрытии
     * @param holdHeight  высота удержания
     * @param onComplete  callback
     * @return анимация
     */
    private SequentialTransition buildEntryAnimation(double closeHeight,
                                                     double holdHeight, Runnable onComplete) {
        Timeline close = buildHeightTimeline(
                ENTRY_DURATION_MS * CLOSE_PHASE, closeHeight, Interpolator.EASE_IN);
        PauseTransition hold = new PauseTransition(
                Duration.millis(ENTRY_DURATION_MS * HOLD_PHASE));
        Timeline open = buildHeightTimeline(
                ENTRY_DURATION_MS * OPEN_PHASE, holdHeight, Interpolator.EASE_OUT);

        SequentialTransition seq = new SequentialTransition(close, hold, open);
        seq.setOnFinished(e -> {
            entryAnimation = null;
            startDriftAnimation(holdHeight);
            if (onComplete != null) onComplete.run();
        });
        return seq;
    }

    /**
     * Запускает бесконечное медленное покачивание век вверх-вниз.
     *
     * @param baseHeight базовая высота век (от которой колеблется)
     */
    private void startDriftAnimation(double baseHeight) {
        double amplitude = baseHeight * DRIFT_AMPLITUDE;
        double low = baseHeight - amplitude;
        double high = baseHeight + amplitude;

        driftAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(topLidHeight, low, Interpolator.EASE_BOTH),
                        new KeyValue(bottomLidHeight, low, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(DRIFT_CYCLE_MS),
                        new KeyValue(topLidHeight, high, Interpolator.EASE_BOTH),
                        new KeyValue(bottomLidHeight, high, Interpolator.EASE_BOTH))
        );
        driftAnimation.setAutoReverse(true);
        driftAnimation.setCycleCount(Timeline.INDEFINITE);
        driftAnimation.play();
    }

    /**
     * Анимирует высоту обоих век одновременно.
     *
     * @param durationMs   длительность в мс
     * @param targetHeight целевая высота
     * @param interpolator интерполятор
     * @return Timeline
     */
    private Timeline buildHeightTimeline(double durationMs, double targetHeight,
                                          Interpolator interpolator) {
        return new Timeline(
                new KeyFrame(Duration.millis(durationMs),
                        new KeyValue(topLidHeight, targetHeight, interpolator),
                        new KeyValue(bottomLidHeight, targetHeight, interpolator)
                )
        );
    }

    /**
     * Удаляет контейнеры век из root мгновенно.
     *
     * @param parent корневой StackPane
     */
    private void removeLids(StackPane parent) {
        if (parent == null) return;
        if (topContainer != null) parent.getChildren().remove(topContainer);
        if (bottomContainer != null) parent.getChildren().remove(bottomContainer);
        topContainer = null;
        bottomContainer = null;
    }
}
