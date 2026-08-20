package kz.aws.game.scenedetails;

import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import kz.aws.game.engine.model.ClueData;

/**
 * Интерфейс панели диалога. Определяет контракт для отображения
 * текста, имени персонажа, управления видимостью и анимациями.
 * Реализация: {@link DialogPanelController} (FXML).
 */
public interface DialogPanel {

    /**
     * Устанавливает текст диалога (без улик).
     *
     * @param text    текст
     * @param animate true — с анимацией печати
     */
    void changeDialogText(String text, boolean animate);

    /**
     * Устанавливает текст с кликабельными уликами.
     *
     * @param text    полный текст
     * @param clues   список улик
     * @param animate true — с анимацией печати
     */
    void changeDialogTextWithClues(String text, List<ClueData> clues, boolean animate);

    /**
     * Устанавливает имя говорящего персонажа.
     *
     * @param text имя
     */
    void changeNameText(String text);

    /**
     * Устанавливает цвет имени персонажа.
     *
     * @param color цвет (null — сброс к CSS)
     */
    void changeColorNameText(Color color);

    /**
     * Сбрасывает цвет имени к значению из CSS.
     */
    void resetNameColor();

    /**
     * Обновляет CSS-стиль текста (при смене темы).
     *
     * @param styleName имя CSS-класса
     */
    void updateStyle(String styleName);

    /**
     * Добавляет панель на сцену с анимацией.
     *
     * @param root корневой StackPane
     */
    void addToScene(StackPane root);

    /**
     * Убирает панель со сцены с анимацией.
     *
     * @param root корневой StackPane
     */
    void removeFromScene(StackPane root);

    /**
     * Показывает или скрывает панель без удаления из сцены.
     *
     * @param visible true — показать
     */
    void setPanelVisible(boolean visible);

    /**
     * Включает или выключает кнопки навигации (вперёд/назад).
     * Используется на время активной мини-игры, чтобы нельзя было
     * листать диалог.
     *
     * @param enabled true — навигация доступна
     */
    void setNavigationEnabled(boolean enabled);
}
