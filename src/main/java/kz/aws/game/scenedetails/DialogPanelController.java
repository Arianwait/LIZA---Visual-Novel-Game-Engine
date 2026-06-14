package kz.aws.game.scenedetails;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.model.ClueData;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.utils.SceneSettingsParser.SceneSettings;
import kz.aws.game.buttonaction.ButtonActionRegistry;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.UiConfigParser;
import kz.aws.game.utils.UiFactory;

/**
 * Контроллер панели диалога (FXML).
 * Заменяет старый TableDatail — управляет текстом, именем персонажа,
 * кнопками и анимацией печати. Все размеры привязаны к размерам сцены
 * через property binding, что обеспечивает корректное масштабирование.
 */
public class DialogPanelController implements DialogPanel {

    private static final double TYPING_SPEED_MS = 10;
    private static final double SLIDE_IN_MS = 500;
    private static final double SLIDE_OUT_MS = 600;
    private static final double BUTTON_HOVER_SCALE = 1.2;
    private static final double BUTTON_HOVER_DURATION_MS = 200;

    @FXML private StackPane rootPane;
    @FXML private StackPane container;
    @FXML private StackPane tablePane;
    @FXML private ImageView backgroundImageView;
    @FXML private VBox contentStack;
    @FXML private VBox textContainer;
    @FXML private Text nameText;
    @FXML private Text dialogueText;
    @FXML private TextFlow dialogueTextFlow;
    @FXML private HBox bottomBar;
    @FXML private VBox hotkeyHints;
    @FXML private HBox navButtons;

    /** Цвет имени персонажа, вставляется в binding стиля nameText. */
    private final StringProperty nameColorStyle = new SimpleStringProperty("");
    private boolean usingTextFlow = false;
    private String fullText;
    private int currentIndex;
    private Timeline typingTimeline;
    private Scene sceneRef;
    private SceneSettings settingsRef;

    /**
     * Инициализирует панель после загрузки FXML. Привязывает размеры
     * элементов к размерам сцены и настраивает визуальный режим.
     *
     * @param appSettings настройки приложения
     * @param scene       текущая сцена (для привязки размеров)
     * @param settings    настройки внешнего вида панели
     */
    public void initialize(AppSettings appSettings, Scene scene, SceneSettings settings) {
        this.sceneRef = scene;
        this.settingsRef = settings;

        setupTextEffects();
        setupButtons(appSettings, settings);
        setupHotkeyHints(scene, settings);
        bindFontSizes(scene, settings);

        if (settings.useGradientPanel) {
            setupGradientMode(scene, settings);
        } else {
            setupImageMode(scene, settings);
        }

        bindButtonSizes(scene, settings);
        bindContainerSize(scene);
        setupHotkeys(appSettings, scene);
    }

    // ── Инициализация режимов ──────────────────────────────────────

    /**
     * Настраивает тени для текста диалога и имени.
     */
    private void setupTextEffects() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(2.0);
        shadow.setOffsetX(2.0);
        shadow.setOffsetY(0.5);
        shadow.setColor(Color.WHITE);
        dialogueText.setEffect(shadow);
        nameText.setEffect(shadow);
    }

    /**
     * Создаёт подсказки горячих клавиш внизу слева.
     *
     * @param scene    сцена
     * @param settings настройки
     */
    private void setupHotkeyHints(Scene scene, SceneSettings settings) {
        hotkeyHints.spacingProperty().bind(scene.heightProperty().multiply(0.003));
        hotkeyHints.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(0, 0, scene.getHeight() * 0.01, scene.getWidth() * 0.01),
                scene.widthProperty(), scene.heightProperty()));
        navButtons.spacingProperty().bind(scene.widthProperty().multiply(0.006));
        navButtons.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(0, scene.getWidth() * 0.01, scene.getHeight() * 0.01, 0),
                scene.widthProperty(), scene.heightProperty()));

        String[][] hints = {{"Esc", "Меню"}, {"Tab", "Журнал"}, {"Ctrl", "Скрыть интерфейс"}, {"F", "Авто"}};
        for (String[] hint : hints) {
            Text keyText = new Text(hint[0]);
            keyText.getStyleClass().add("hotkey-key");
            Text labelText = new Text("  " + hint[1]);
            labelText.getStyleClass().add("hotkey-label");
            keyText.styleProperty().bind(Bindings.concat(
                    "-fx-font-size: ", scene.heightProperty().multiply(0.014).asString(), "px;"));
            labelText.styleProperty().bind(Bindings.concat(
                    "-fx-font-size: ", scene.heightProperty().multiply(0.013).asString(), "px;"));
            javafx.scene.text.TextFlow hintFlow = new javafx.scene.text.TextFlow(keyText, labelText);
            hotkeyHints.getChildren().add(hintFlow);
        }
    }

    /**
     * Привязывает горячие клавиши к сцене.
     *
     * @param appSettings настройки приложения
     * @param scene       сцена
     */
    private void setupHotkeys(AppSettings appSettings, Scene scene) {
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (SceneInfo.isPuzzleActive()) {
                handlePuzzleHotkey(event);
                return;
            }
            if (hasOverlayOpen(appSettings)) {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE && GamePauseMenuController.isOpen()) {
                    GamePauseMenuController.closeCurrent();
                    event.consume();
                }
                return;
            }
            switch (event.getCode()) {
                case ESCAPE -> {
                    GamePauseMenuController.open(appSettings);
                    event.consume();
                }
                case TAB -> {
                    ButtonActionRegistry.run("game-btn-history", appSettings);
                    event.consume();
                }
                default -> { }
            }
        });
    }

    /**
     * Обрабатывает хоткеи во время активного паззла.
     * Esc — досрочный провал с подтверждением.
     *
     * @param event событие клавиши
     */
    private void handlePuzzleHotkey(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            kz.aws.game.panel.BaseGamePanel puzzle = SceneInfo.getActivePuzzle();
            if (puzzle != null) {
                puzzle.forceFailure();
            }
            event.consume();
        }
    }

    /**
     * Проверяет, открыта ли какая-либо overlay-панель поверх игры.
     *
     * @param appSettings настройки приложения
     * @return true если есть открытый overlay
     */
    private boolean hasOverlayOpen(AppSettings appSettings) {
        return appSettings.getRoot().getChildren().size() > 2;
    }

    /**
     * Создаёт навигационные кнопки из конфигурации Buttons.xml.
     *
     * @param appSettings настройки приложения
     * @param settings    настройки сцены
     */
    private static final java.util.Set<String> NAV_IDS = java.util.Set.of(
            "game-btn-back", "game-btn-next");
    private static final java.util.Map<String, String> NAV_ICONS = java.util.Map.of(
            "game-btn-back", "◀",
            "game-btn-next", "▶");

    private void setupButtons(AppSettings appSettings, SceneSettings settings) {
        for (UiConfigParser.ButtonConfig btnCfg : UiConfigParser.getButtonsByContext("game-panel")) {
            if (!NAV_IDS.contains(btnCfg.id)) continue;
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            btn.setText(NAV_ICONS.getOrDefault(btnCfg.id, btn.getText()));
            btn.getStyleClass().add("nav-arrow-button");
            navButtons.getChildren().add(btn);
        }
    }

    /**
     * Привязывает размер шрифтов диалога и имени к высоте сцены.
     *
     * @param scene    сцена для привязки
     * @param settings настройки множителей
     */
    private void bindFontSizes(Scene scene, SceneSettings settings) {
        textContainer.spacingProperty().bind(scene.heightProperty().multiply(0.006));
        dialogueText.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                scene.heightProperty().multiply(settings.dialogFontSizeMultiplier).asString(),
                "px;"));

        nameText.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                scene.heightProperty().multiply(settings.nameFontSizeMultiplier).asString(),
                "px; ", nameColorStyle));

        dialogueText.wrappingWidthProperty().bind(
                scene.widthProperty().multiply(settings.textContentWidth));
        dialogueTextFlow.prefWidthProperty().bind(
                scene.widthProperty().multiply(settings.textContentWidth));
    }

    /**
     * Настраивает режим градиентной панели (полупрозрачный фон снизу).
     *
     * @param scene    сцена для привязки размеров
     * @param settings настройки градиента
     */
    private void setupGradientMode(Scene scene, SceneSettings settings) {
        backgroundImageView.setVisible(false);
        backgroundImageView.setManaged(false);

        tablePane.prefWidthProperty().bind(scene.widthProperty());
        tablePane.prefHeightProperty().bind(
                scene.heightProperty().multiply(settings.gradientPanelHeightMultiplier));
        tablePane.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        tablePane.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s);",
                settings.gradientStartColor, settings.gradientEndColor));
        StackPane.setAlignment(tablePane, Pos.BOTTOM_CENTER);

        bindTextMargins(scene, settings);
        bindMenuMarginsGradient(scene, settings);
        setupGradientButtonStyles(scene, settings);

        VBox.setVgrow(textContainer, javafx.scene.layout.Priority.ALWAYS);
    }

    /**
     * Настраивает режим панели с фоновой картинкой.
     *
     * @param scene    сцена для привязки размеров
     * @param settings настройки картинки
     */
    private void setupImageMode(Scene scene, SceneSettings settings) {
        backgroundImageView.setVisible(true);
        backgroundImageView.setManaged(true);
        backgroundImageView.setImage(new Image(settings.tableImagePath));
        backgroundImageView.setSmooth(true);
        backgroundImageView.fitWidthProperty().bind(
                scene.heightProperty().multiply(settings.tableWidthMultiplier));
        backgroundImageView.fitHeightProperty().bind(
                scene.widthProperty().multiply(settings.tableHeightMultiplier));

        tablePane.maxWidthProperty().bind(backgroundImageView.fitWidthProperty());
        tablePane.maxHeightProperty().bind(backgroundImageView.fitHeightProperty());
        StackPane.setAlignment(tablePane, Pos.BOTTOM_CENTER);

        bindTextMargins(scene, settings);
    }

    // ── Привязки размеров (responsive) ─────────────────────────────

    /**
     * Привязывает отступы текста к размерам сцены (обновляются при ресайзе).
     *
     * @param scene    сцена
     * @param settings настройки отступов
     */
    private void bindTextMargins(Scene scene, SceneSettings settings) {
        textContainer.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(
                        scene.getHeight() * settings.textPaddingTop,
                        0, 0,
                        scene.getWidth() * settings.textPaddingLeft),
                scene.widthProperty(), scene.heightProperty()));
    }

    /**
     * Привязывает отступ кнопок снизу в режиме градиента.
     *
     * @param scene    сцена
     * @param settings настройки
     */
    private void bindMenuMarginsGradient(Scene scene, SceneSettings settings) {
        navButtons.spacingProperty().bind(
                scene.widthProperty().multiply(settings.buttonSpacingMultiplier));
        navButtons.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(0, 0, scene.getHeight() * 0.01, 0),
                scene.heightProperty()));
    }

    /**
     * Назначает CSS-класс и hover-анимацию навигационным кнопкам в режиме градиента.
     *
     * @param scene    сцена
     * @param settings настройки кнопок
     */
    private void setupGradientButtonStyles(Scene scene, SceneSettings settings) {
        for (javafx.scene.Node node : navButtons.getChildren()) {
            if (!(node instanceof Button btn)) continue;

            btn.prefWidthProperty().bind(
                    scene.heightProperty().multiply(settings.buttonHeightMultiplier));
            btn.prefHeightProperty().bind(
                    scene.heightProperty().multiply(settings.buttonHeightMultiplier));
            btn.styleProperty().bind(Bindings.concat(
                    "-fx-font-size: ",
                    scene.heightProperty().multiply(settings.buttonFontSizeMultiplier * 1.2).asString(),
                    "px;"));

            addButtonHoverAnimation(btn);
        }
    }

    /**
     * Привязывает размеры всех кнопок к сцене (общее для обоих режимов).
     *
     * @param scene    сцена
     * @param settings настройки
     */
    private void bindButtonSizes(Scene scene, SceneSettings settings) {
        for (javafx.scene.Node node : navButtons.getChildren()) {
            if (!(node instanceof Button btn)) continue;
            if (settingsRef.useGradientPanel) continue;

            btn.prefWidthProperty().unbind();
            btn.prefHeightProperty().unbind();
            btn.prefWidthProperty().bind(
                    scene.heightProperty().multiply(settings.buttonHeightMultiplier));
            btn.prefHeightProperty().bind(
                    scene.heightProperty().multiply(settings.buttonHeightMultiplier));
            btn.styleProperty().bind(Bindings.concat(
                    "-fx-font-size: ",
                    scene.heightProperty().multiply(settings.buttonFontSizeMultiplier * 1.2).asString(),
                    "px;"));
        }
    }

    /**
     * Привязывает размер корневого контейнера к сцене.
     *
     * @param scene сцена
     */
    private void bindContainerSize(Scene scene) {
        rootPane.prefWidthProperty().bind(scene.widthProperty());
        rootPane.prefHeightProperty().bind(scene.heightProperty());
    }

    /**
     * Добавляет анимацию увеличения при наведении на кнопку.
     *
     * @param btn кнопка
     */
    private void addButtonHoverAnimation(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(
                    Duration.millis(BUTTON_HOVER_DURATION_MS), btn);
            st.setToX(BUTTON_HOVER_SCALE);
            st.setToY(BUTTON_HOVER_SCALE);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(
                    Duration.millis(BUTTON_HOVER_DURATION_MS), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ── Добавление/удаление из сцены с анимацией ───────────────────

    /**
     * Добавляет панель в root с анимацией slide-up.
     *
     * @param root корневой StackPane
     */
    public void addToScene(StackPane root) {
        container.setTranslateY(root.getHeight());
        root.getChildren().add(rootPane);
        rootPane.toFront();

        TranslateTransition slideUp = new TranslateTransition(
                Duration.millis(SLIDE_IN_MS), container);
        slideUp.setFromY(root.getHeight());
        slideUp.setToY(0);
        slideUp.play();
    }

    /**
     * Убирает панель из root с анимацией slide-down.
     *
     * @param root корневой StackPane
     */
    public void removeFromScene(StackPane root) {
        TranslateTransition slideDown = new TranslateTransition(
                Duration.millis(SLIDE_OUT_MS), container);
        slideDown.setFromY(0);
        slideDown.setToY(root.getHeight());
        slideDown.setOnFinished(e -> root.getChildren().remove(rootPane));
        slideDown.play();
    }

    /**
     * Скрывает или показывает панель без удаления из сцены.
     *
     * @param visible true — показать, false — скрыть
     */
    public void setPanelVisible(boolean visible) {
        if (container != null) {
            container.setVisible(visible);
        }
    }

    // ── Текст диалога ──────────────────────────────────────────────

    /**
     * Устанавливает текст диалога (обычный режим, без улик).
     *
     * @param text    текст для отображения
     * @param animate true — анимация печати
     */
    public void changeDialogText(String text, boolean animate) {
        stopTyping();
        switchToPlainText();
        fullText = text;

        if (!animate) {
            dialogueText.setText(fullText);
            return;
        }
        startTypingAnimation(fullText);
    }

    /**
     * Устанавливает текст с кликабельными уликами через TextFlow.
     *
     * @param text    полный текст
     * @param clues   список улик
     * @param animate true — анимация печати
     */
    public void changeDialogTextWithClues(String text, List<ClueData> clues, boolean animate) {
        stopTyping();
        switchToTextFlow();
        fullText = text;

        List<DialogSegment> segments = buildSegments(text, clues);
        dialogueTextFlow.getChildren().clear();
        List<Text> allNodes = new ArrayList<>();

        for (DialogSegment seg : segments) {
            Text node = createSegmentNode(seg);
            allNodes.add(node);
            dialogueTextFlow.getChildren().add(node);
        }

        if (!animate) {
            setAllSegmentTexts(segments, allNodes);
            return;
        }
        startSegmentTypingAnimation(segments, allNodes);
    }

    /**
     * Устанавливает имя персонажа.
     *
     * @param text имя
     */
    public void changeNameText(String text) {
        nameText.setText(text);
    }

    /**
     * Устанавливает цвет имени персонажа через inline style.
     *
     * @param color цвет (null — сброс к CSS)
     */
    public void changeColorNameText(Color color) {
        if (color == null) {
            resetNameColor();
            return;
        }
        nameColorStyle.set("-fx-fill: " + colorToCssHex(color) + ";");
    }

    /**
     * Сбрасывает цвет имени к значению из CSS.
     */
    public void resetNameColor() {
        nameColorStyle.set("");
    }

    /**
     * Обновляет CSS-стиль текста (при смене темы).
     *
     * @param styleName имя CSS-класса темы
     */
    public void updateStyle(String styleName) {
        updateNodeStyle(dialogueText, "dialog-text", styleName);
        updateNodeStyle(nameText, "speaker-name", styleName);
        updateTextFlowChildStyles(styleName);
    }

    // ── Внутренние хелперы ─────────────────────────────────────────

    /**
     * Переключается на обычный Text (скрывает TextFlow).
     */
    private void switchToPlainText() {
        if (!usingTextFlow) return;
        dialogueTextFlow.setVisible(false);
        dialogueTextFlow.setManaged(false);
        dialogueTextFlow.getChildren().clear();
        dialogueText.setVisible(true);
        dialogueText.setManaged(true);
        usingTextFlow = false;
    }

    /**
     * Переключается на TextFlow (скрывает обычный Text).
     */
    private void switchToTextFlow() {
        dialogueText.setVisible(false);
        dialogueText.setManaged(false);
        dialogueTextFlow.setVisible(true);
        dialogueTextFlow.setManaged(true);
        usingTextFlow = true;
    }

    /**
     * Останавливает текущую анимацию печати.
     */
    private void stopTyping() {
        if (typingTimeline != null) {
            typingTimeline.stop();
        }
    }

    /**
     * Запускает анимацию печати для одного текстового узла.
     *
     * @param text полный текст
     */
    private void startTypingAnimation(String text) {
        currentIndex = 0;
        typingTimeline = new Timeline();
        typingTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(TYPING_SPEED_MS), event -> {
                    if (currentIndex <= text.length()) {
                        dialogueText.setText(text.substring(0, currentIndex));
                        currentIndex++;
                    } else {
                        typingTimeline.stop();
                    }
                }));
        typingTimeline.setCycleCount(text.length() + 1);
        typingTimeline.play();
    }

    /**
     * Запускает анимацию печати через несколько сегментов TextFlow.
     *
     * @param segments список сегментов
     * @param nodes    список Text-узлов
     */
    private void startSegmentTypingAnimation(List<DialogSegment> segments, List<Text> nodes) {
        int totalLength = segments.stream().mapToInt(s -> s.text.length()).sum();
        currentIndex = 0;
        typingTimeline = new Timeline();
        typingTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(TYPING_SPEED_MS), event -> {
                    if (currentIndex <= totalLength) {
                        distributeTypedChars(segments, nodes, currentIndex);
                        currentIndex++;
                    } else {
                        typingTimeline.stop();
                    }
                }));
        typingTimeline.setCycleCount(totalLength + 1);
        typingTimeline.play();
    }

    /**
     * Распределяет напечатанные символы по сегментам.
     *
     * @param segments список сегментов
     * @param nodes    список Text-узлов
     * @param charPos  текущая позиция символа
     */
    private void distributeTypedChars(List<DialogSegment> segments, List<Text> nodes, int charPos) {
        int remaining = charPos;
        for (int i = 0; i < segments.size(); i++) {
            String segText = segments.get(i).text;
            if (remaining <= segText.length()) {
                nodes.get(i).setText(segText.substring(0, remaining));
                clearRemainingNodes(nodes, i + 1);
                return;
            }
            nodes.get(i).setText(segText);
            remaining -= segText.length();
        }
    }

    /**
     * Очищает текст в узлах, начиная с указанного индекса.
     *
     * @param nodes     список узлов
     * @param fromIndex начальный индекс
     */
    private void clearRemainingNodes(List<Text> nodes, int fromIndex) {
        for (int j = fromIndex; j < nodes.size(); j++) {
            nodes.get(j).setText("");
        }
    }

    /**
     * Устанавливает текст всех сегментов сразу (без анимации).
     *
     * @param segments сегменты
     * @param nodes    узлы
     */
    private void setAllSegmentTexts(List<DialogSegment> segments, List<Text> nodes) {
        for (int i = 0; i < segments.size(); i++) {
            nodes.get(i).setText(segments.get(i).text);
        }
    }

    /**
     * Создаёт Text-узел для сегмента (обычный или улика).
     *
     * @param seg сегмент
     * @return настроенный Text-узел
     */
    private Text createSegmentNode(DialogSegment seg) {
        Text node = new Text("");
        node.getStyleClass().add("dialog-text");
        bindSegmentFontSize(node);
        copyTextEffect(node);

        if (seg.clue != null) {
            setupClueNode(node, seg.clue);
        }
        return node;
    }

    /**
     * Привязывает размер шрифта сегмента к высоте сцены.
     *
     * @param node текстовый узел
     */
    private void bindSegmentFontSize(Text node) {
        if (sceneRef == null || settingsRef == null) return;
        node.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                sceneRef.heightProperty().multiply(settingsRef.dialogFontSizeMultiplier).asString(),
                "px;"));
    }

    /**
     * Копирует эффект тени с основного текста на узел сегмента.
     *
     * @param node целевой узел
     */
    private void copyTextEffect(Text node) {
        if (!(dialogueText.getEffect() instanceof DropShadow source)) return;
        DropShadow copy = new DropShadow();
        copy.setRadius(source.getRadius());
        copy.setOffsetX(source.getOffsetX());
        copy.setOffsetY(source.getOffsetY());
        copy.setColor(source.getColor());
        node.setEffect(copy);
    }

    /**
     * Настраивает узел как кликабельную улику.
     *
     * @param node     текстовый узел
     * @param clueData данные улики
     */
    private void setupClueNode(Text node, ClueData clueData) {
        node.getStyleClass().add("clue-word");
        node.setUnderline(true);
        node.setFill(Color.web("#fffacd"));
        node.setCursor(Cursor.HAND);

        node.setOnMouseEntered(e -> {
            if (!node.getStyleClass().contains("clue-collected")) {
                node.setFill(Color.web("#ffd700"));
            }
        });
        node.setOnMouseExited(e -> {
            if (!node.getStyleClass().contains("clue-collected")) {
                node.setFill(Color.web("#fffacd"));
            }
        });
        node.setOnMouseClicked(e -> {
            SceneController.setPlayerChoice(clueData.getKey(), clueData.getValue());
            markClueCollected(node);
            e.consume();
        });
    }

    /**
     * Помечает улику как собранную (зелёный, без подчёркивания).
     *
     * @param node узел улики
     */
    private void markClueCollected(Text node) {
        node.getStyleClass().add("clue-collected");
        node.setFill(Color.web("#88ff88"));
        node.setUnderline(false);
        node.setCursor(Cursor.DEFAULT);
        node.setOnMouseClicked(null);
        node.setOnMouseEntered(null);
        node.setOnMouseExited(null);
    }

    // ── Сегменты текста ────────────────────────────────────────────

    /**
     * Сегмент текста: обычный фрагмент или улика.
     */
    private static class DialogSegment {
        final String text;
        final ClueData clue;

        DialogSegment(String text, ClueData clue) {
            this.text = text;
            this.clue = clue;
        }
    }

    /**
     * Разбивает текст на сегменты: обычные и улики.
     *
     * @param text  полный текст
     * @param clues список улик
     * @return список сегментов
     */
    private List<DialogSegment> buildSegments(String text, List<ClueData> clues) {
        List<DialogSegment> segments = new ArrayList<>();
        int lastEnd = 0;
        for (ClueData clue : clues) {
            int start = text.indexOf(clue.getDisplayText(), lastEnd);
            if (start < 0) continue;
            if (start > lastEnd) {
                segments.add(new DialogSegment(text.substring(lastEnd, start), null));
            }
            int end = start + clue.getDisplayText().length();
            segments.add(new DialogSegment(text.substring(start, end), clue));
            lastEnd = end;
        }
        if (lastEnd < text.length()) {
            segments.add(new DialogSegment(text.substring(lastEnd), null));
        }
        return segments;
    }

    // ── Стили ──────────────────────────────────────────────────────

    /**
     * Обновляет CSS-класс стиля на текстовом узле.
     *
     * @param node      узел
     * @param baseClass базовый класс (не удаляется)
     * @param styleName имя нового класса темы
     */
    private void updateNodeStyle(Text node, String baseClass, String styleName) {
        node.getStyleClass().removeIf(s -> !s.equals(baseClass) && !s.equals("text"));
        if (styleName != null && !styleName.isEmpty()) {
            node.getStyleClass().add(styleName);
        }
    }

    /**
     * Обновляет стили дочерних узлов TextFlow.
     *
     * @param styleName имя класса темы
     */
    private void updateTextFlowChildStyles(String styleName) {
        if (!usingTextFlow || dialogueTextFlow == null) return;
        for (javafx.scene.Node node : dialogueTextFlow.getChildren()) {
            if (!(node instanceof Text t)) continue;
            t.getStyleClass().removeIf(s ->
                    !s.equals("dialog-text") && !s.equals("text")
                    && !s.equals("clue-word") && !s.equals("clue-collected"));
            if (styleName != null && !styleName.isEmpty()) {
                t.getStyleClass().add(styleName);
            }
        }
    }

    /**
     * Конвертирует JavaFX Color в CSS hex-строку.
     *
     * @param c цвет
     * @return строка вида #rrggbb
     */
    private static String colorToCssHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    /**
     * Возвращает контейнер кнопок меню (для SceneInfo.setHboxButton).
     *
     * @return HBox с кнопками
     */
    public HBox getMenuButtons() {
        return navButtons;
    }

    /**
     * Возвращает корневой StackPane FXML.
     *
     * @return корневой узел
     */
    public StackPane getRootPane() {
        return rootPane;
    }
}
