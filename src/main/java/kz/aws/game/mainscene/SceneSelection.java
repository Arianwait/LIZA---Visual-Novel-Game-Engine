package kz.aws.game.mainscene;

import java.io.Serializable;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.Panel;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.ChapterConfigParser;
import kz.aws.game.utils.ChapterConfigParser.Chapter;
import kz.aws.game.utils.MainMenuConfigParser;
import kz.aws.game.utils.MainMenuConfigParser.MainMenuConfig;
import kz.aws.game.utils.SceneSettingsParser;
import kz.aws.game.utils.SceneSettingsParser.SceneSettings;
import kz.aws.game.utils.UiConfigParser;
import kz.aws.game.utils.UiFactory;
import kz.aws.game.utils.VirtualViewport;

/**
 * Панель выбора главы. Id в Panels.xml и {@link Panel}; показ через
 * PanelRegistry.show("scene-selection", appSettings). Все размеры — в пикселях
 * дизайн-разрешения {@link VirtualViewport}.
 */
@Panel("scene-selection")
public class SceneSelection extends VBox implements Serializable {
    private static final long serialVersionUID = -4187266373909661360L;

    private final transient AppSettings appSettings;
    private final transient MainMenuConfig menuConfig;
    private final transient SceneSettings sceneSettings;

    /**
     * Собирает панель выбора главы.
     *
     * @param appSettings настройки приложения
     */
    public SceneSelection(AppSettings appSettings) {
        this.appSettings = appSettings;
        this.menuConfig = MainMenuConfigParser.getConfig();
        this.sceneSettings = SceneSettingsParser.getSettings();

        setSpacing(menuConfig.menuSpacing);

        VBox menuPanel = buildMenuPanel();
        SceneInfo.setHboxButton(menuPanel);
        applyPanelMargins(menuPanel);
        getChildren().add(menuPanel);
    }

    /**
     * Строит панель меню: заголовок, список глав в скролле и кнопки подменю.
     *
     * @return собранная панель
     */
    private VBox buildMenuPanel() {
        VBox menuPanel = new VBox(menuConfig.menuSpacing);
        menuPanel.getStyleClass().add("hitech-panel");
        menuPanel.setAlignment(Pos.CENTER);
        menuPanel.setPadding(new Insets(menuConfig.menuPadding));

        menuPanel.getChildren().add(buildTitle());
        menuPanel.getChildren().add(buildChapterScrollPane(menuPanel));
        addSubmenuButtons(menuPanel);
        return menuPanel;
    }

    /**
     * Создаёт заголовок панели.
     *
     * @return Label заголовка
     */
    private Label buildTitle() {
        Label titleLabel = new Label("Выберите главу:");
        titleLabel.getStyleClass().add("hitech-panel-title");
        titleLabel.setStyle(String.format("-fx-font-size: %.0fpx;",
                VirtualViewport.height(0.04)));
        return titleLabel;
    }

    /**
     * Создаёт прокручиваемый список кнопок глав.
     *
     * @param menuPanel родительская панель (для привязки ширины кнопок)
     * @return настроенный ScrollPane
     */
    private ScrollPane buildChapterScrollPane(VBox menuPanel) {
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.TOP_CENTER);
        buttonsBox.setPadding(new Insets(10, 25, 10, 10));
        buttonsBox.prefWidthProperty().bind(menuPanel.widthProperty().subtract(40));

        for (Chapter chapter : ChapterConfigParser.getChapters()) {
            buttonsBox.getChildren().add(buildChapterButton(chapter, buttonsBox));
        }

        ScrollPane scrollPane = new ScrollPane(buttonsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;"
                + " -fx-control-inner-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxHeight(VirtualViewport.height(sceneSettings.scrollPaneMaxHeightMultiplier));
        return scrollPane;
    }

    /**
     * Создаёт кнопку главы с переходом через экран загрузки.
     *
     * @param chapter    глава из конфигурации
     * @param buttonsBox контейнер кнопок (для привязки ширины)
     * @return настроенная кнопка
     */
    private Button buildChapterButton(Chapter chapter, VBox buttonsBox) {
        Button chapterButton = UiFactory.createButton(chapter.name,
                "chapter-btn-" + chapter.id, event -> startChapter(chapter), appSettings);
        chapterButton.getStyleClass().add("game-button");
        chapterButton.prefWidthProperty().bind(buttonsBox.widthProperty());
        chapterButton.setPrefHeight(VirtualViewport.height(0.05));
        return chapterButton;
    }

    /**
     * Запускает главу: случайные выборы, экран загрузки и построение сцены.
     *
     * @param chapter выбранная глава
     */
    private void startChapter(Chapter chapter) {
        SceneInfo.clearChoices();
        for (String choice : chapter.generateRandomChoices()) {
            SceneInfo.addChoice(choice);
        }

        StackPane root = appSettings.getRoot();
        LoadingScreen loadingScreen = new LoadingScreen();
        root.getChildren().setAll(loadingScreen.getRoot());
        appSettings.applyThemeToRoot();
        loadingScreen.startLoading(chapter.sceneId,
                () -> new SceneBuilder(appSettings, null, chapter.sceneId));
    }

    /**
     * Добавляет кнопки уровня submenu (Назад и др.) из Buttons.xml.
     *
     * @param menuPanel панель для добавления кнопок
     */
    private void addSubmenuButtons(VBox menuPanel) {
        for (UiConfigParser.ButtonConfig btnCfg : UiConfigParser.getButtonsByContext("submenu")) {
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            menuPanel.getChildren().add(btn);
            appSettings.ButtonStyle(btn, menuPanel);
            btn.prefWidthProperty().bind(menuPanel.widthProperty().multiply(0.5));
            btn.setPrefHeight(VirtualViewport.height(0.05));
        }
    }

    /**
     * Устанавливает внешние отступы панели из конфигурации (в дизайн-пикселях).
     *
     * @param menuPanel панель меню
     */
    private void applyPanelMargins(VBox menuPanel) {
        VBox.setMargin(menuPanel, new Insets(0,
                VirtualViewport.width(menuConfig.marginRight),
                VirtualViewport.height(menuConfig.marginTop),
                VirtualViewport.width(menuConfig.marginLeft)));
    }
}
