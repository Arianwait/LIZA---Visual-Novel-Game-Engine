package kz.aws.game.engine.effect;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import kz.aws.game.engine.model.VisualEffectCommand;
import kz.aws.game.utils.VirtualViewport;

/**
 * Эффект засыпания — глаза моргают с нарастающим закрытием.
 *
 * <p>Три моргания, каждое следующее открывает глаза всё меньше:
 * <ol>
 *   <li>Закрытие → открытие на 75%</li>
 *   <li>Закрытие → открытие на 40%</li>
 *   <li>Закрытие → глаза остаются закрытыми (экран чёрный)</li>
 * </ol>
 *
 * <p>Веки остаются закрытыми до команды {@code stopEffect("closeEyes")},
 * после чего плавно открываются (пробуждение).
 */
@VisualEffect("closeEyes")
public class CloseEyesEffectPlayer implements VisualEffectPlayer {

    private static final int CLOSE_MS = 350;
    private static final int OPEN_MS = 450;
    private static final int PAUSE_MS = 150;
    private static final int GAP_MS = 600;
    private static final int EXIT_DURATION_MS = 500;

    private static final double OPEN_1 = 0.75;
    private static final double OPEN_2 = 0.40;

    private Rectangle topLid;
    private Rectangle bottomLid;
    private SequentialTransition animation;

    /**
     * Запускает анимацию засыпания: три моргания с нарастающим закрытием.
     *
     * @param root              корневой StackPane (сюда добавляются «веки»)
     * @param sceneContentLayer слой контента (не используется)
     * @param command           команда эффекта
     * @param onComplete        вызывается после полного закрытия глаз (может быть null)
     */
    @Override
    public void play(StackPane root, Pane sceneContentLayer,
                     VisualEffectCommand command, Runnable onComplete) {
        double width = VirtualViewport.DESIGN_WIDTH;
        double halfHeight = VirtualViewport.DESIGN_HEIGHT / 2;

        createLids(width, halfHeight);
        root.getChildren().addAll(topLid, bottomLid);
        topLid.toFront();
        bottomLid.toFront();

        animation = buildSleepAnimation(halfHeight, onComplete);
        animation.play();
    }

    /**
     * Плавно открывает глаза (пробуждение) и убирает веки.
     *
     * @param root              корневой StackPane
     * @param sceneContentLayer слой контента (не используется)
     */
    @Override
    public void stop(StackPane root, Pane sceneContentLayer) {
        if (animation != null) {
            animation.stop();
            animation = null;
        }

        if (topLid == null || bottomLid == null) return;

        double halfHeight = VirtualViewport.DESIGN_HEIGHT / 2;

        ParallelTransition exit = buildExitAnimation(halfHeight);
        exit.setOnFinished(e -> removeLids(root));
        exit.play();
    }

    /**
     * Создаёт два чёрных прямоугольника-века за пределами экрана.
     *
     * @param width      ширина экрана
     * @param halfHeight половина высоты экрана
     */
    private void createLids(double width, double halfHeight) {
        topLid = new Rectangle(width, halfHeight, Color.BLACK);
        topLid.setMouseTransparent(true);
        topLid.setTranslateY(-halfHeight);

        bottomLid = new Rectangle(width, halfHeight, Color.BLACK);
        bottomLid.setMouseTransparent(true);
        bottomLid.setTranslateY(halfHeight);
    }

    /**
     * Строит полную анимацию засыпания: три моргания с нарастающим закрытием.
     *
     * @param halfHeight половина высоты экрана
     * @param onComplete callback после финального закрытия
     * @return собранная анимация
     */
    private SequentialTransition buildSleepAnimation(double halfHeight, Runnable onComplete) {
        SequentialTransition seq = new SequentialTransition(
            buildBlinkCycle(halfHeight, OPEN_1),
            new PauseTransition(Duration.millis(GAP_MS)),
            buildBlinkCycle(halfHeight, OPEN_2),
            new PauseTransition(Duration.millis(GAP_MS)),
            buildCloseFinal(halfHeight)
        );

        seq.setOnFinished(e -> {
            animation = null;
            if (onComplete != null) onComplete.run();
        });
        return seq;
    }

    /**
     * Один цикл моргания: закрытие → пауза → открытие до указанной доли.
     *
     * @param halfHeight половина высоты экрана
     * @param openFraction доля открытия (1.0 = полностью открыты, 0.0 = закрыты)
     * @return анимация одного моргания
     */
    private SequentialTransition buildBlinkCycle(double halfHeight, double openFraction) {
        ParallelTransition close = buildMoveLids(CLOSE_MS, halfHeight, 0, Interpolator.EASE_IN);
        PauseTransition hold = new PauseTransition(Duration.millis(PAUSE_MS));
        double openY = halfHeight * openFraction;
        ParallelTransition open = buildMoveLids(OPEN_MS, 0, openY, Interpolator.EASE_OUT);

        return new SequentialTransition(close, hold, open);
    }

    /**
     * Финальное закрытие глаз — веки сходятся к центру и остаются.
     *
     * @param halfHeight половина высоты экрана
     * @return анимация закрытия
     */
    private SequentialTransition buildCloseFinal(double halfHeight) {
        ParallelTransition close = buildMoveLids(CLOSE_MS, halfHeight, 0, Interpolator.EASE_IN);
        return new SequentialTransition(close);
    }

    /**
     * Перемещает оба века от текущей позиции к целевому отступу от центра.
     *
     * @param durationMs   длительность перемещения
     * @param fromOffset   начальный отступ от центра (halfHeight = скрыты за краем)
     * @param toOffset     конечный отступ от центра (0 = полностью закрыты)
     * @param interpolator интерполятор анимации
     * @return параллельная анимация обоих век
     */
    private ParallelTransition buildMoveLids(int durationMs, double fromOffset,
                                              double toOffset, Interpolator interpolator) {
        TranslateTransition topMove = new TranslateTransition(
                Duration.millis(durationMs), topLid);
        topMove.setFromY(-fromOffset);
        topMove.setToY(-toOffset);
        topMove.setInterpolator(interpolator);

        TranslateTransition bottomMove = new TranslateTransition(
                Duration.millis(durationMs), bottomLid);
        bottomMove.setFromY(fromOffset);
        bottomMove.setToY(toOffset);
        bottomMove.setInterpolator(interpolator);

        return new ParallelTransition(topMove, bottomMove);
    }

    /**
     * Анимация выхода: веки расходятся к краям экрана (пробуждение).
     *
     * @param halfHeight половина высоты экрана
     * @return параллельная анимация выхода
     */
    private ParallelTransition buildExitAnimation(double halfHeight) {
        TranslateTransition topExit = new TranslateTransition(
                Duration.millis(EXIT_DURATION_MS), topLid);
        topExit.setToY(-halfHeight);
        topExit.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition bottomExit = new TranslateTransition(
                Duration.millis(EXIT_DURATION_MS), bottomLid);
        bottomExit.setToY(halfHeight);
        bottomExit.setInterpolator(Interpolator.EASE_OUT);

        return new ParallelTransition(topExit, bottomExit);
    }

    @Override
    public void migrate(StackPane oldRoot, StackPane newRoot) {
        if (topLid != null) oldRoot.getChildren().remove(topLid);
        if (bottomLid != null) oldRoot.getChildren().remove(bottomLid);
        if (topLid != null) newRoot.getChildren().add(topLid);
        if (bottomLid != null) newRoot.getChildren().add(bottomLid);
    }

    /**
     * Удаляет веки из контейнера.
     *
     * @param parent родительский StackPane
     */
    private void removeLids(StackPane parent) {
        if (parent == null) return;
        if (topLid != null) parent.getChildren().remove(topLid);
        if (bottomLid != null) parent.getChildren().remove(bottomLid);
        topLid = null;
        bottomLid = null;
    }
}
