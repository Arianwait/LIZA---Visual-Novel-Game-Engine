package kz.aws.game.mainscene;

import java.io.Serializable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.PanelRegistry;
import kz.aws.game.appsettings.JsonConfigWriter;
import kz.aws.game.utils.UiConfigParser;
import kz.aws.game.utils.UiFactory;

/**
 * Устаревшая панель настроек (заменена на {@link SettingsMenuController} с FXML).
 *
 * @deprecated используйте {@link SettingsMenuController}
 */
@Deprecated
public class SettingsMenu extends VBox implements Serializable {

    private static final long serialVersionUID = 3913186027516459673L;

    
    public SettingsMenu(AppSettings appSettings) {
        
        CheckBox fullscreenCheckBox = new CheckBox("Полный экран");
        fullscreenCheckBox.setSelected(appSettings.isFullscreen());

        
        ChoiceBox<String> resolutionChoiceBox = new ChoiceBox<>();
        resolutionChoiceBox.getItems().addAll("1024x768", "1280x720", "1920x1080");
        resolutionChoiceBox.setValue(appSettings.getWindowWidth() + "x" + appSettings.getWindowHeight());

        // Добавляем слушатель изменения состояния CheckBox
        fullscreenCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // При изменении состояния CheckBox, устанавливаем доступность ChoiceBox
                resolutionChoiceBox.setDisable(newValue);
            }
        });

        // При запуске приложения проверяем начальное состояние CheckBox и устанавливаем ChoiceBox соответственно
        resolutionChoiceBox.setDisable(fullscreenCheckBox.isSelected());
        
        DoubleProperty controlWidth = new SimpleDoubleProperty(150.0);
        DoubleProperty controlHeight = new SimpleDoubleProperty(40.0);

        Slider volumeSlider = new Slider(0, 1, appSettings.getVolumeValue());
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.prefWidthProperty().bind(controlWidth);
        volumeSlider.prefHeightProperty().bind(controlHeight);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            appSettings.getMediaPlayer().setVolume(newValue.doubleValue());
        });

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> saveSettingsAndReturnToMainMenu(appSettings, fullscreenCheckBox, resolutionChoiceBox, volumeSlider.getValue()));
        

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> PanelRegistry.show("main-menu", appSettings));

        fullscreenCheckBox.getStyleClass().add("settings-checkbox");
        resolutionChoiceBox.getStyleClass().add("settings-combobox");
        volumeSlider.getStyleClass().add("settings-slider");
        fullscreenCheckBox.styleProperty().bind(javafx.beans.binding.Bindings.format("-fx-font-size: %.0fpx;",
                appSettings.getStagePain().heightProperty().multiply(0.018)));
        resolutionChoiceBox.styleProperty().bind(javafx.beans.binding.Bindings.format("-fx-font-size: %.0fpx;",
                appSettings.getStagePain().heightProperty().multiply(0.018)));

        VBox menu = new VBox(10);
        menu.getStyleClass().add("hitech-panel");
        VBox.setMargin(menu, new Insets(0, appSettings.getStagePain().getWidth() * 0.7,
                appSettings.getStagePain().getHeight() * 0.2, appSettings.getStagePain().getWidth() * 0.05));
        menu.setPadding(new Insets(50));
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().addAll(
                fullscreenCheckBox,
                resolutionChoiceBox,
                volumeSlider,
                saveButton, cancelButton
        );
        for (UiConfigParser.ButtonConfig btnCfg : UiConfigParser.getButtonsByContext("submenu")) {
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            menu.getChildren().add(btn);
            appSettings.ButtonStyle(btn, menu);
        }
        getChildren().addAll(menu);

        appSettings.ButtonStyle(saveButton, menu);
        appSettings.ButtonStyle(cancelButton, menu);

        setSpacing(10);
    }

    private void saveSettingsAndReturnToMainMenu(AppSettings appSettings, CheckBox fullscreenCheckBox,
            ChoiceBox<String> resolutionChoiceBox, double volumeValue) {
        try {
            appSettings.setFullscreen(fullscreenCheckBox.isSelected());

            String resolutionValue = resolutionChoiceBox.getValue();
            String[] resolutionParts = resolutionValue.split("x");
            appSettings.setWindowWidth(Integer.parseInt(resolutionParts[0]));
            appSettings.setWindowHeight(Integer.parseInt(resolutionParts[1]));
            appSettings.setVolumeValue(volumeValue);

            JsonConfigWriter.writeConfig(appSettings);
            

            
            if(fullscreenCheckBox.isSelected()) {
            	appSettings.getStagePain().setFullScreen(appSettings.isFullscreen());
            } else {
                appSettings.getStagePain().setFullScreen(appSettings.isFullscreen());
                appSettings.getStagePain().setWidth(appSettings.getWindowWidth());
                appSettings.getStagePain().setHeight(appSettings.getWindowHeight());
            }

            
        } catch (NumberFormatException ex) {
            // Обработка ошибки, если пользователь ввел нечисловое значение
        }

        PanelRegistry.show("main-menu", appSettings);
    }
}


