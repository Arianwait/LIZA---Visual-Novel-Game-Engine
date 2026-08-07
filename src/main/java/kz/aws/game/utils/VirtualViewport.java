package kz.aws.game.utils;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Виртуальный вьюпорт игры. Весь интерфейс верстается один раз в фиксированном
 * дизайн-разрешении {@value #DESIGN_WIDTH}x{@value #DESIGN_HEIGHT}, а при любом
 * реальном размере окна содержимое масштабируется единым Scale-трансформом
 * с сохранением пропорций (letterbox: чёрные поля по краям при несовпадении
 * соотношения сторон). Благодаря этому все размеры в коде — обычные пиксели
 * дизайн-разрешения, без биндингов к размеру окна.
 */
public final class VirtualViewport {

    /** Ширина дизайн-разрешения в пикселях. */
    public static final double DESIGN_WIDTH = 1920;
    /** Высота дизайн-разрешения в пикселях. */
    public static final double DESIGN_HEIGHT = 1080;

    private final StackPane screenRoot = new StackPane();
    private final StackPane contentRoot = new StackPane();

    /**
     * Создаёт вьюпорт: контейнер контента фиксированного размера внутри
     * центрирующего чёрного экрана-подложки.
     */
    public VirtualViewport() {
        contentRoot.setMinSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        contentRoot.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        contentRoot.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        contentRoot.setAlignment(Pos.CENTER);
        contentRoot.setClip(new Rectangle(DESIGN_WIDTH, DESIGN_HEIGHT));

        screenRoot.setStyle("-fx-background-color: black;");
        screenRoot.getChildren().add(new Group(contentRoot));
    }

    /**
     * Привязывает масштаб контента к размеру сцены: при каждом изменении
     * размера окна пересчитывается общий коэффициент масштабирования.
     *
     * @param scene сцена приложения, к размерам которой привязывается масштаб
     */
    public void bindTo(Scene scene) {
        scene.widthProperty().addListener((obs, oldV, newV) -> rescale(scene));
        scene.heightProperty().addListener((obs, oldV, newV) -> rescale(scene));
        rescale(scene);
    }

    /**
     * Пересчитывает коэффициент масштабирования по текущему размеру сцены.
     *
     * @param scene сцена приложения
     */
    private void rescale(Scene scene) {
        double scale = Math.min(scene.getWidth() / DESIGN_WIDTH,
                scene.getHeight() / DESIGN_HEIGHT);
        if (scale <= 0 || Double.isNaN(scale)) {
            scale = 1;
        }
        contentRoot.setScaleX(scale);
        contentRoot.setScaleY(scale);
    }

    /**
     * Возвращает долю ширины дизайн-разрешения в пикселях.
     *
     * @param fraction доля от 0.0 до 1.0
     * @return ширина в дизайн-пикселях
     */
    public static double width(double fraction) {
        return DESIGN_WIDTH * fraction;
    }

    /**
     * Возвращает долю высоты дизайн-разрешения в пикселях.
     *
     * @param fraction доля от 0.0 до 1.0
     * @return высота в дизайн-пикселях
     */
    public static double height(double fraction) {
        return DESIGN_HEIGHT * fraction;
    }

    /**
     * Возвращает корень для установки в Scene (чёрная подложка летербокса).
     *
     * @return внешний корневой контейнер
     */
    public StackPane getScreenRoot() {
        return screenRoot;
    }

    /**
     * Возвращает контейнер контента фиксированного дизайн-размера —
     * то, что раньше было root приложения. Сюда добавляются все слои игры.
     *
     * @return контейнер контента 1920x1080
     */
    public StackPane getContentRoot() {
        return contentRoot;
    }
}
