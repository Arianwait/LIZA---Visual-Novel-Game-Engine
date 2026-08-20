package kz.aws.game.panel;

import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import kz.aws.game.appsettings.AppSettings;

/**
 * Базовый класс для всех игровых панелей.
 *
 * <p>Панель — это любой интерактивный оверлей поверх сцены:
 * доска улик, осмотр предмета, загадка, мини-игра, карта и т.д.
 *
 * <p>Порядок использования:
 * <ol>
 *   <li>Создать класс-наследник, пометить {@link GamePanel @GamePanel}.</li>
 *   <li>Реализовать {@link #initialize()} — построить UI панели.</li>
 *   <li>По завершению вызвать {@link #complete(PanelResult)}.</li>
 * </ol>
 *
 * <p>Размер задаётся в аннотации ({@code widthPercent} / {@code heightPercent})
 * и применяется автоматически при вызове {@link #init(PanelContext)}.
 *
 * <p>Пример:
 * <pre>
 * {@literal @}GamePanel(id = "knife_inspect", title = "Осмотр улики",
 *            widthPercent = 55, heightPercent = 60)
 * public class KnifeInspectPanel extends BaseGamePanel {
 *     {@literal @}Override
 *     protected void initialize() {
 *         ImageView img = new ImageView(new Image("file:lib/img/knife.png"));
 *         Button closeBtn = new Button("Закрыть");
 *         closeBtn.setOnAction(e -&gt; complete(PanelResult.close()));
 *         getChildren().addAll(img, closeBtn);
 *     }
 * }
 * </pre>
 */
public abstract class BaseGamePanel extends StackPane {

    private Consumer<PanelResult> onComplete;
    private PanelContext context;

    /**
     * Инициализирует панель: задаёт размеры из аннотации, вызывает
     * {@link #initialize()} и добавляет кнопку «Пропустить» (если панель
     * помечена как {@code skippable}). Вызывается системой автоматически
     * перед показом.
     */
    public final void init(PanelContext context) {
        this.context = context;
        GamePanel annotation = getClass().getAnnotation(GamePanel.class);
        if (annotation != null) {
            double w = context.getWindowWidth()  * annotation.widthPercent()  / 100.0;
            double h = context.getWindowHeight() * annotation.heightPercent() / 100.0;
            setPrefSize(w, h);
            setMaxSize(w, h);
        }
        initialize();
        addSkipButton(annotation);
    }

    /**
     * Добавляет кнопку «Пропустить» в правый верхний угол панели.
     * Нажатие закрывает мини-игру с результатом «провал» — так же,
     * как выход по Esc.
     *
     * @param annotation аннотация панели (null — кнопка не добавляется)
     */
    private void addSkipButton(GamePanel annotation) {
        if (annotation == null || !annotation.skippable()) return;
        Button skipButton = new Button("Пропустить");
        skipButton.getStyleClass().add("game-button");
        skipButton.setOnAction(e -> forceFailure());
        StackPane.setAlignment(skipButton, Pos.TOP_RIGHT);
        StackPane.setMargin(skipButton, new Insets(10));
        getChildren().add(skipButton);
    }

    /**
     * Строим UI панели здесь.
     * Вызывается один раз после установки размеров.
     */
    protected abstract void initialize();

    /**
     * Завершает панель и передаёт результат движку.
     * <ul>
     *   <li>{@link PanelResult#success()} / {@link PanelResult#failure()} — записывают флаг, могут перейти на сцену</li>
     *   <li>{@link PanelResult#withData(boolean, String)} — то же + строковые данные в {@code playerChoices}</li>
     *   <li>{@link PanelResult#close()} — просто закрыть панель, ничего не записывать</li>
     * </ul>
     */
    protected final void complete(PanelResult result) {
        if (onComplete != null) onComplete.accept(result);
    }

    /**
     * Досрочно завершает панель с провалом. Вызывается извне (например по Esc).
     */
    public void forceFailure() {
        complete(PanelResult.failure());
    }

    /** Устанавливается движком. */
    public void setOnComplete(Consumer<PanelResult> callback) {
        this.onComplete = callback;
    }

    protected PanelContext  getContext()          { return context; }
    protected AppSettings   getAppSettings()      { return context != null ? context.getAppSettings() : null; }
    protected GamePanel     getPanelAnnotation()  { return getClass().getAnnotation(GamePanel.class); }
}
