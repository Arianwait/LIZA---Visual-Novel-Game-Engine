package kz.aws.game.mainscene;

import java.io.Serializable;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.ChapterConfigParser;
import kz.aws.game.utils.ChapterConfigParser.Chapter;
import kz.aws.game.utils.MainMenuConfigParser;
import kz.aws.game.utils.MainMenuConfigParser.MainMenuConfig;
import kz.aws.game.panel.Panel;
import kz.aws.game.utils.SceneSettingsParser;
import kz.aws.game.utils.SceneSettingsParser.SceneSettings;
import kz.aws.game.utils.UiConfigParser;
import kz.aws.game.utils.UiFactory;

/**
 * Панель выбора главы. Id в Panels.xml и {@link Panel}; показ через PanelRegistry.show("scene-selection", appSettings).
 */
@Panel("scene-selection")
public class SceneSelection extends VBox implements Serializable {
    private static final long serialVersionUID = -4187266373909661360L;

    public SceneSelection(AppSettings appSettings) {
        MainMenuConfig menuConfig = MainMenuConfigParser.getConfig();
        SceneSettings sceneSettings = SceneSettingsParser.getSettings();
        
        // Используем тот же подход, что и в MainMenu - VBox с отступами
        setSpacing(menuConfig.menuSpacing);
        
        // === ПАНЕЛЬ МЕНЮ (хайтек-стиль из CSS .hitech-panel) ===
        VBox menuPanel = new VBox(menuConfig.menuSpacing);
        menuPanel.getStyleClass().add("hitech-panel");
        menuPanel.setAlignment(Pos.CENTER);
        menuPanel.setPadding(new Insets(menuConfig.menuPadding));

        Label titleLabel = new Label("Выберите главу:");
        titleLabel.getStyleClass().add("hitech-panel-title");
        titleLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-size: ", appSettings.getStagePain().heightProperty().multiply(0.04).asString(), "px; "
        ));
        menuPanel.getChildren().add(titleLabel);

        // Список кнопок (внутри ScrollPane)
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.TOP_CENTER);
        buttonsBox.setPadding(new Insets(10, 25, 10, 10)); // Отступы внутри скролла
        
        // Привязываем ширину контейнера кнопок к ширине меню, чтобы кнопки растягивались
        buttonsBox.prefWidthProperty().bind(menuPanel.widthProperty().subtract(40)); // Учитываем паддинги

        List<Chapter> chapters = ChapterConfigParser.getChapters();
        for (Chapter chapter : chapters) {
            Button chapterButton = UiFactory.createButton(chapter.name, "chapter-btn-" + chapter.id, 
                event -> {
                    SceneInfo.clearChoices();
                    List<String> randomChoices = chapter.generateRandomChoices();
                    for (String choice : randomChoices) SceneInfo.addChoice(choice);
                    
                    LoadingScreen loadingScreen = new LoadingScreen(appSettings.getStagePain().getWidth(), appSettings.getStagePain().getHeight());
                    appSettings.getStagePain().getScene().setRoot(loadingScreen.getRoot());
                    appSettings.applyThemeToRoot();
                    loadingScreen.startLoading(chapter.sceneId, () -> {
                         new SceneBuilder(appSettings, null, chapter.sceneId);
                         appSettings.getStagePain().getScene().setRoot(appSettings.getRoot());
                    });
                }, appSettings);
            
            // Ручная настройка стилей для "ровности" и адаптивности
            chapterButton.getStyleClass().add("game-button"); // Базовый стиль
            
            // Размеры кнопки: Ширина почти во всю панель, Высота фиксированный % от экрана
            chapterButton.prefWidthProperty().bind(buttonsBox.widthProperty());
            chapterButton.prefHeightProperty().bind(appSettings.getStagePain().heightProperty().multiply(0.05)); // 5% от высоты экрана (было 8%)
            
            // Шрифт кнопки динамический (дублируем логику UiFactory на всякий случай или полагаемся на нее)
            // UiFactory уже делает биндинг, но для уверенности можно переопределить здесь, если нужен другой размер
            // Оставим как в UiFactory, но проверим, что он вызывается
            
            buttonsBox.getChildren().add(chapterButton);
        }

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(buttonsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        // Полностью прозрачный фон и скрытие рамки
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-control-inner-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Ограничиваем высоту ScrollPane
        if (appSettings.getStagePain() != null) {
             scrollPane.maxHeightProperty().bind(appSettings.getStagePain().heightProperty().multiply(sceneSettings.scrollPaneMaxHeightMultiplier)); 
        }
        
        menuPanel.getChildren().add(scrollPane);

        // Кнопки уровня submenu (Назад и др.) из Buttons.xml
        for (UiConfigParser.ButtonConfig btnCfg : UiConfigParser.getButtonsByContext("submenu")) {
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            menuPanel.getChildren().add(btn);
            appSettings.ButtonStyle(btn, menuPanel);
            btn.prefWidthProperty().bind(menuPanel.widthProperty().multiply(0.5));
            btn.prefHeightProperty().bind(appSettings.getStagePain().heightProperty().multiply(0.05));
        }

        // Устанавливаем отступы как в MainMenu (из конфигурации)
        SceneInfo.setHboxButton(menuPanel);
        VBox.setMargin(menuPanel, new Insets(0, appSettings.getStagePain().getWidth() * menuConfig.marginRight,  
                appSettings.getStagePain().getHeight() * menuConfig.marginTop, 
                appSettings.getStagePain().getWidth() * menuConfig.marginLeft));

        this.getChildren().add(menuPanel);
    }
}
