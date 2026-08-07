package kz.aws.game.utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.buttonaction.ButtonActionRegistry;
import kz.aws.game.soundtrack.SoundManager;
import kz.aws.game.utils.UiConfigParser.ButtonConfig;

/**
 * Фабрика кнопок интерфейса: текст и действие из Buttons.xml, звуки
 * наведения/клика и единый шрифт в дизайн-пикселях {@link VirtualViewport}.
 */
public class UiFactory {

    /** Размер шрифта кнопок в дизайн-пикселях (1.3% высоты дизайн-разрешения). */
    private static final double BUTTON_FONT_SIZE = VirtualViewport.height(0.013);

    /**
     * Создаёт кнопку по конфигу из Buttons.xml; действие выполняется через {@link ButtonActionRegistry} по id.
     */
    public static Button createButtonFromConfig(ButtonConfig config, AppSettings appSettings) {
        if (config == null) return new Button();
        return createButton(
            config.text != null && !config.text.isEmpty() ? config.text : config.id,
            config.id,
            e -> ButtonActionRegistry.run(config.id, appSettings),
            appSettings
        );
    }

    /**
     * Создаёт кнопку: текст и стиль из конфига, звуки и обработчик действия.
     *
     * @param defaultText текст по умолчанию, если в конфиге не задан
     * @param cssId       id кнопки (для CSS и поиска конфига)
     * @param action      обработчик нажатия
     * @param appSettings настройки приложения
     * @return настроенная кнопка
     */
    public static Button createButton(String defaultText, String cssId, EventHandler<ActionEvent> action, AppSettings appSettings) {
        Button button = new Button();
        button.setFont(Font.font("Verdana", FontWeight.BOLD, BUTTON_FONT_SIZE));
        ButtonConfig config = UiConfigParser.getButtonConfig(cssId);
        
        if (config != null) {
            button.setText(config.text != null && !config.text.isEmpty() ? config.text : defaultText);
            if (config.styleClass != null && !config.styleClass.isEmpty()) {
                button.getStyleClass().add(config.styleClass);
            }
        } else {
            button.setText(defaultText);
            button.getStyleClass().add("game-button"); // Default fallback
        }
        
        if (cssId != null && !cssId.isEmpty()) {
            button.setId(cssId);
        }
        
        // Sound Effects
        button.setOnMouseEntered(e -> {
            String sound = (config != null && config.hoverSound != null) ? config.hoverSound : "hover";
            SoundManager.playSound(sound);
        });
        
        button.setOnAction(e -> {
            String sound = (config != null && config.clickSound != null) ? config.clickSound : "click";
            SoundManager.playSound(sound);
            if (action != null) {
                action.handle(e);
            }
        });
        
        return button;
    }
}
