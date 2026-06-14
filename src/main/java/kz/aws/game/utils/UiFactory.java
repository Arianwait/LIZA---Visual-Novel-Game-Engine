package kz.aws.game.utils;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.buttonaction.ButtonActionRegistry;
import kz.aws.game.soundtrack.SoundManager;
import kz.aws.game.utils.UiConfigParser.ButtonConfig;

public class UiFactory {

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

    public static Button createButton(String defaultText, String cssId, EventHandler<ActionEvent> action, AppSettings appSettings) {
        Button button = new Button();
        
        // === АДАПТИВНЫЙ РАЗМЕР КНОПОК ===
        // Используем fontProperty binding - это самый надежный способ менять размер текста динамически
        if (appSettings != null && appSettings.getStagePain() != null) {
             // Уменьшили шрифт до 1.3% от высоты экрана, чтобы влезал в кнопки
             button.fontProperty().bind(Bindings.createObjectBinding(() -> 
                 Font.font("Verdana", FontWeight.BOLD, appSettings.getStagePain().getHeight() * 0.013), 
                 appSettings.getStagePain().heightProperty()));
        }
        
        // Load config
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
