package kz.aws.game.scenedetails;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import kz.aws.game.actionscenarios.SaveManadger;
import kz.aws.game.animation.AnimationUtils;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.GameEngine;
import kz.aws.game.fileutils.FileUtils;
import kz.aws.game.mainscene.LoadingScreen;
import kz.aws.game.scenelist.GameData;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Единый FXML-контроллер для панелей сохранения и загрузки.
 * Заменяет дублирующиеся SaveTable и LoudTable, объединяя общий UI-код
 * и разделяя логику по режиму (SAVE / LOAD).
 */
public class SaveLoadPanelController extends VBox {

    private static final String FXML_PATH = "lib/fxml/save-load-panel.fxml";
    private static final String SAVE_DIRECTORY = "save";
    private static final int SLOT_COUNT = 6;
    private static final int SLIDE_DURATION_MS = 500;

    @FXML private VBox menuPanel;
    @FXML private Text titleText;
    @FXML private VBox buttonsBox;
    @FXML private Button returnButton;

    private final AppSettings appSettings;
    private final Mode mode;
    private final boolean confirmOnLoad;
    private StackPane root;

    /**
     * Режим работы панели.
     */
    public enum Mode { SAVE, LOAD }

    /**
     * Создаёт панель сохранения/загрузки.
     *
     * @param appSettings   настройки приложения
     * @param mode          режим (SAVE или LOAD)
     * @param confirmOnLoad показывать ли подтверждение при загрузке (true — во время игры)
     */
    public SaveLoadPanelController(AppSettings appSettings, Mode mode,
                                    boolean confirmOnLoad) {
        this.appSettings = appSettings;
        this.mode = mode;
        this.confirmOnLoad = confirmOnLoad;
        loadFxml();
        initialize();
    }

    /**
     * Загружает FXML из файловой системы (fx:root pattern).
     */
    private void loadFxml() {
        try {
            File fxmlFile = new File(FXML_PATH);
            URL fxmlUrl = fxmlFile.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load save-load-panel.fxml", e);
        }
    }

    /**
     * Инициализация: заголовок, кнопки слотов, кнопка возврата и адаптивные биндинги.
     */
    private void initialize() {
        titleText.setText(mode == Mode.SAVE ? "Сохранения игры" : "Загрузка игры");
        bindTitleFontSize();
        createSlotButtons();
        setupReturnButton();
    }

    /**
     * Привязывает размер шрифта заголовка к высоте Stage.
     */
    private void bindTitleFontSize() {
        titleText.setFill(javafx.scene.paint.Color.WHITE);
        titleText.styleProperty().bind(Bindings.format(
                "-fx-font-size: %.0fpx; -fx-font-weight: bold;",
                appSettings.getStagePain().heightProperty().multiply(0.035)));
    }

    /**
     * Создаёт кнопки для каждого слота сохранения.
     */
    private void createSlotButtons() {
        List<Integer> existingSlots = FileUtils.extractNumbersFromDirectory(SAVE_DIRECTORY);
        for (int i = 0; i < SLOT_COUNT; i++) {
            Button btn = createSlotButton(i, existingSlots);
            styleSlotButton(btn);
            buttonsBox.getChildren().add(btn);
        }
    }

    /**
     * Создаёт кнопку для одного слота. Текст и действие зависят от режима и наличия сохранения.
     *
     * @param slotIndex     индекс слота
     * @param existingSlots список занятых слотов
     * @return настроенная кнопка
     */
    private Button createSlotButton(int slotIndex, List<Integer> existingSlots) {
        boolean slotOccupied = existingSlots.contains(slotIndex);
        String label = resolveSlotLabel(slotIndex, slotOccupied);
        Button button = new Button(label);

        if (mode == Mode.SAVE) {
            configureSaveSlot(button, slotIndex, slotOccupied);
        } else {
            configureLoadSlot(button, slotIndex, slotOccupied);
        }
        return button;
    }

    /**
     * Определяет текст кнопки слота.
     *
     * @param slotIndex    индекс слота
     * @param slotOccupied занят ли слот
     * @return текст кнопки
     */
    private String resolveSlotLabel(int slotIndex, boolean slotOccupied) {
        if (!slotOccupied) {
            return mode == Mode.SAVE ? "Сохранение" + (slotIndex + 1) : "Пусто";
        }
        String fileName = FileUtils.findFileByNumber(SAVE_DIRECTORY, slotIndex);
        String[] blocks = fileName.split("_");
        return blocks[blocks.length - 1].replace(".ser", "");
    }

    /**
     * Настраивает кнопку слота для режима сохранения.
     *
     * @param button       кнопка
     * @param slotIndex    индекс слота
     * @param slotOccupied занят ли слот
     */
    private void configureSaveSlot(Button button, int slotIndex, boolean slotOccupied) {
        if (slotOccupied) {
            button.setOnAction(e -> confirmOverwriteSave(slotIndex, button));
        } else {
            button.setOnAction(e -> performSave(slotIndex, button));
        }
    }

    /**
     * Настраивает кнопку слота для режима загрузки.
     *
     * @param button       кнопка
     * @param slotIndex    индекс слота
     * @param slotOccupied занят ли слот
     */
    private void configureLoadSlot(Button button, int slotIndex, boolean slotOccupied) {
        if (!slotOccupied) return;
        String fileName = FileUtils.findFileByNumber(SAVE_DIRECTORY, slotIndex);
        String[] blocks = fileName.split("_");
        int sceneId = Integer.parseInt(blocks[1]);
        button.setOnAction(e -> performLoad(fileName, sceneId));
    }

    /**
     * Применяет адаптивный стиль и анимацию к кнопке слота.
     *
     * @param button кнопка
     */
    private void styleSlotButton(Button button) {
        button.getStyleClass().add("game-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER);
        VBox.setMargin(button, new Insets(2, 0, 2, 0));
        ButtonAnimation.addButtonHoverAnimation(button);
    }

    /**
     * Настраивает кнопку возврата с адаптивным стилем.
     */
    private void setupReturnButton() {
        returnButton.setOnAction(e -> closePanel());
        returnButton.getStyleClass().add("game-button");
        returnButton.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(returnButton, new Insets(10, 0, 0, 0));
        ButtonAnimation.addButtonHoverAnimation(returnButton);
    }

    /**
     * Показывает панель с анимацией slide-in на указанном root.
     *
     * @param root корневой StackPane сцены
     */
    public void showPanel(StackPane root) {
        this.root = root;
        root.getChildren().add(menuPanel);
        AnimationUtils.slideInFromSide(menuPanel,
                appSettings.getScene().getWidth(), 0, SLIDE_DURATION_MS);
        bindPanelMargins(root);
        SceneInfo.disableEventHandler(root);
    }

    /**
     * Привязывает внешние отступы панели к размерам сцены через listener.
     * Панель располагается внизу экрана с горизонтальными отступами.
     *
     * @param root корневой StackPane
     */
    private void bindPanelMargins(StackPane root) {
        javafx.scene.Scene scene = appSettings.getScene();
        StackPane.setAlignment(menuPanel, Pos.CENTER);
        menuPanel.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        menuPanel.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        menuPanel.prefWidthProperty().bind(scene.widthProperty().multiply(0.45));
        menuPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.65));
        menuPanel.spacingProperty().bind(scene.heightProperty().multiply(0.012));
        menuPanel.paddingProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(
                () -> new Insets(
                        scene.getHeight() * 0.04,
                        scene.getWidth() * 0.04,
                        scene.getHeight() * 0.04,
                        scene.getWidth() * 0.04),
                scene.widthProperty(), scene.heightProperty()));
        buttonsBox.spacingProperty().bind(scene.heightProperty().multiply(0.005));
    }

    /**
     * Закрывает панель с анимацией slide-out.
     */
    public void closePanel() {
        AnimationUtils.slideOutToSide(menuPanel, 0,
                appSettings.getScene().getWidth(), SLIDE_DURATION_MS,
                () -> {
                    root.getChildren().remove(menuPanel);
                    SceneInfo.enableEventHandler(root);
                });
    }

    /**
     * Показывает диалог подтверждения перезаписи сохранения.
     *
     * @param slotIndex индекс слота
     * @param button    кнопка слота (для обновления текста)
     */
    private void confirmOverwriteSave(int slotIndex, Button button) {
        ConfirmDialogController dialog = new ConfirmDialogController(appSettings);
        dialog.show(
                "Вы уверены, что хотите поменять сохранения?",
                null,
                () -> {
                    deleteFile("save/" + FileUtils.findFileByNumber(SAVE_DIRECTORY, slotIndex));
                    performSave(slotIndex, button);
                },
                () -> System.out.println("Отмена смены сохранения"),
                buttonsBox);
    }

    /**
     * Выполняет сохранение в указанный слот.
     *
     * @param slotIndex индекс слота
     * @param button    кнопка слота (для обновления текста)
     */
    private void performSave(int slotIndex, Button button) {
        GameEngine engine = SceneInfo.getGameEngine();
        if (engine == null) {
            System.err.println("Cannot save: GameEngine is null.");
            return;
        }
        GameData gameData = engine.getSaveData();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String saveFileName = slotIndex + "_" + gameData.getCurrentSceneId() + "_" + dateTime + ".ser";
        SaveManadger.serializeClicker(gameData, SAVE_DIRECTORY + "/" + saveFileName);
        button.setText(dateTime);
        closePanel();
    }

    /**
     * Выполняет загрузку сохранения (с подтверждением, если confirmOnLoad).
     *
     * @param fileName имя файла сохранения
     * @param sceneId  id сцены из сохранения
     */
    private void performLoad(String fileName, int sceneId) {
        if (confirmOnLoad) {
            confirmAndLoad(fileName, sceneId);
        } else {
            executeLoad(fileName, sceneId);
        }
    }

    /**
     * Показывает диалог подтверждения загрузки.
     *
     * @param fileName имя файла
     * @param sceneId  id сцены
     */
    private void confirmAndLoad(String fileName, int sceneId) {
        ConfirmDialogController dialog = new ConfirmDialogController(appSettings);
        dialog.show(
                "Вы уверены, загрузить сохранения, все не сохраненые данные будут удалены?",
                null,
                () -> {
                    appSettings.getRoot().getChildren().remove(menuPanel);
                    appSettings.getRoot().getChildren().clear();
                    executeLoad(fileName, sceneId);
                },
                () -> System.out.println("Отмена смены сохранения"),
                buttonsBox);
    }

    /**
     * Загружает сохранение через LoadingScreen и SceneBuilder.
     *
     * @param fileName имя файла сохранения
     * @param sceneId  id сцены
     */
    private void executeLoad(String fileName, int sceneId) {
        appSettings.getRoot().getChildren().clear();
        LoadingScreen loadingScreen = new LoadingScreen(
                appSettings.getStagePain().getWidth(),
                appSettings.getStagePain().getHeight());
        appSettings.getStagePain().getScene().setRoot(loadingScreen.getRoot());
        appSettings.applyThemeToRoot();
        loadingScreen.startLoading(1, () -> {
            new SceneBuilder(appSettings, fileName, sceneId);
            appSettings.getStagePain().getScene().setRoot(appSettings.getRoot());
        });
    }

    /**
     * Удаляет файл по указанному пути.
     *
     * @param filePath путь к файлу
     */
    private void deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.err.println("Ошибка при удалении файла: " + filePath);
        }
    }
}
