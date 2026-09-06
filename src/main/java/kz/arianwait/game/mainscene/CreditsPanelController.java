package kz.arianwait.game.mainscene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import kz.arianwait.game.animation.ButtonAnimation;
import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.mainscene.CreditsParser.Line;
import kz.arianwait.game.panel.Panel;
import kz.arianwait.game.panel.PanelRegistry;
import kz.arianwait.game.utils.ResourceLocator;
import kz.arianwait.game.utils.VirtualViewport;

/**
 * Окно «Кредиты»: авторы, музыка, благодарности из {@code lib/titries.txt}.
 * Открывается из настроек; ссылки в тексте кликабельны и открывают браузер.
 */
@Panel("credits")
public class CreditsPanelController extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(CreditsPanelController.class);

    private static final String FXML_PATH = "lib/fxml/credits-panel.fxml";
    private static final String CREDITS_PATH = "lib/titries.txt";

    private static final String COLOR_TITLE = "#e8d5c0";
    private static final String COLOR_HEADER = "#d2aa78";
    private static final String COLOR_TEXT = "#faf0e6";
    private static final String COLOR_LINK = "#ffcc00";

    @FXML private VBox menuPanel;
    @FXML private Label titleLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox contentBox;

    private final AppSettings appSettings;

    /**
     * Конструктор для PanelRegistry (вызывается через рефлексию по аннотации @Panel).
     *
     * @param appSettings настройки приложения
     */
    public CreditsPanelController(AppSettings appSettings) {
        this.appSettings = appSettings;
        loadFxml();
        initialize();
    }

    /**
     * Загружает FXML из файловой системы (fx:root pattern).
     */
    private void loadFxml() {
        try {
            File fxmlFile = ResourceLocator.file(FXML_PATH);
            URL fxmlUrl = fxmlFile.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credits-panel.fxml", e);
        }
    }

    /**
     * Наполняет панель: заголовок, строки титров, кнопка возврата.
     */
    private void initialize() {
        applyPanelLayout();
        titleLabel.setStyle(fontStyle(0.028) + "-fx-text-fill: " + COLOR_TITLE + ";");
        for (Line line : CreditsParser.parse(readCredits())) {
            contentBox.getChildren().add(createNode(line));
        }
        createBackButton();
    }

    /**
     * Читает файл титров.
     *
     * @return содержимое файла или пустая строка, если файла нет
     */
    private String readCredits() {
        try {
            return Files.readString(ResourceLocator.resolve(CREDITS_PATH), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Файл титров не прочитан (" + CREDITS_PATH + "): " + e.getMessage());
            return "";
        }
    }

    /**
     * Задаёт размеры панели и области прокрутки в дизайн-пикселях.
     */
    private void applyPanelLayout() {
        VBox.setMargin(menuPanel, new Insets(0,
                VirtualViewport.width(0.35),
                VirtualViewport.height(0.1),
                VirtualViewport.width(0.05)));
        scrollPane.setPrefHeight(VirtualViewport.height(0.55));
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentBox.setSpacing(VirtualViewport.height(0.004));
    }

    /**
     * Создаёт узел для одной строки титров.
     *
     * @param line строка титров
     * @return метка, ссылка или отступ
     */
    private Region createNode(Line line) {
        return switch (line.kind()) {
            case TITLE -> styledLabel(line.text(), 0.024, COLOR_TITLE, true);
            case HEADER -> styledLabel(line.text(), 0.02, COLOR_HEADER, true);
            case TEXT -> styledLabel(line.text(), 0.017, COLOR_TEXT, false);
            case LINK -> createLink(line.text());
            case BLANK -> blankSpacer();
        };
    }

    /**
     * Создаёт метку с адаптивным шрифтом.
     *
     * @param text       текст
     * @param sizeFactor доля высоты экрана для размера шрифта
     * @param color      цвет текста
     * @param bold       жирное начертание
     * @return настроенная метка
     */
    private Label styledLabel(String text, double sizeFactor, String color, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(fontStyle(sizeFactor) + "-fx-text-fill: " + color + ";"
                + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    /**
     * Создаёт кликабельную ссылку, открывающую адрес в браузере.
     *
     * @param url адрес
     * @return ссылка
     */
    private Hyperlink createLink(String url) {
        Hyperlink link = new Hyperlink(url);
        link.setStyle(fontStyle(0.016) + "-fx-text-fill: " + COLOR_LINK + ";");
        link.setOnAction(e -> openInBrowser(url));
        return link;
    }

    /**
     * Открывает адрес во внешнем браузере.
     *
     * @param url адрес
     */
    private void openInBrowser(String url) {
        if (appSettings.getGamedispetcher() == null) return;
        appSettings.getGamedispetcher().getHostServices().showDocument(url);
    }

    /**
     * Пустой отступ между блоками титров.
     *
     * @return регион фиксированной высоты
     */
    private Region blankSpacer() {
        Region spacer = new Region();
        spacer.setPrefHeight(VirtualViewport.height(0.012));
        return spacer;
    }

    /**
     * Создаёт кнопку «Назад» — возврат в настройки.
     */
    private void createBackButton() {
        Button back = new Button("Назад");
        back.getStyleClass().add("game-button");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setPrefHeight(VirtualViewport.height(0.05));
        back.setOnAction(e -> PanelRegistry.show("settings", appSettings));
        ButtonAnimation.addButtonHoverAnimation(back);
        menuPanel.getChildren().add(back);
    }

    /**
     * CSS-фрагмент размера шрифта в дизайн-пикселях.
     *
     * @param sizeFactor доля высоты экрана
     * @return строка вида {@code -fx-font-size: Npx;}
     */
    private static String fontStyle(double sizeFactor) {
        return String.format("-fx-font-size: %.0fpx;", VirtualViewport.height(sizeFactor));
    }
}
