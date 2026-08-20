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
import kz.aws.game.utils.VirtualViewport;

/**
 * Контроллер панели диалога (FXML): текст, имя персонажа, кнопки навигации
 * и анимация печати. Все размеры задаются один раз в пикселях
 * дизайн-разрешения {@link kz.aws.game.utils.VirtualViewport}.
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
    private SceneSettings settingsRef;

    /** Сцена, на которую повешен текущий фильтр хоткеев (для снятия). */
    private static Scene hotkeyScene;
    /** Текущий фильтр хоткеев; панель пересоздаётся, фильтр должен быть один. */
    private static javafx.event.EventHandler<javafx.scene.input.KeyEvent> hotkeyFilter;

    /**
     * Инициализирует панель после загрузки FXML: задаёт размеры элементов
     * в дизайн-пикселях и настраивает визуальный режим.
     *
     * @param appSettings настройки приложения
     * @param scene       текущая сцена (для горячих клавиш)
     * @param settings    настройки внешнего вида панели
     */
    public void initialize(AppSettings appSettings, Scene scene, SceneSettings settings) {
        this.settingsRef = settings;

        setupTextEffects();
        setupButtons(appSettings, settings);
        setupHotkeyHints();
        applyFontSizes(settings);

        if (settings.useGradientPanel) {
            setupGradientMode(settings);
        } else {
            setupImageMode(settings);
        }

        applyButtonSizes(settings);
        applyContainerSize();
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
     * Создаёт подсказки горячих клавиш внизу слева (размеры в дизайн-пикселях).
     */
    private void setupHotkeyHints() {
        hotkeyHints.setSpacing(VirtualViewport.height(0.003));
        hotkeyHints.setPadding(new Insets(0, 0,
                VirtualViewport.height(0.01), VirtualViewport.width(0.01)));
        navButtons.setSpacing(VirtualViewport.width(0.006));
        navButtons.setPadding(new Insets(0,
                VirtualViewport.width(0.01), VirtualViewport.height(0.01), 0));

        String keyStyle = fontSizeStyle(VirtualViewport.height(0.014));
        String labelStyle = fontSizeStyle(VirtualViewport.height(0.013));
        String[][] hints = {{"Esc", "Меню"}, {"Tab", "Журнал"}, {"Ctrl", "Скрыть интерфейс"}, {"F", "Авто"}};
        for (String[] hint : hints) {
            Text keyText = new Text(hint[0]);
            keyText.getStyleClass().add("hotkey-key");
            keyText.setStyle(keyStyle);
            Text labelText = new Text("  " + hint[1]);
            labelText.getStyleClass().add("hotkey-label");
            labelText.setStyle(labelStyle);
            hotkeyHints.getChildren().add(new javafx.scene.text.TextFlow(keyText, labelText));
        }
    }

    /**
     * Формирует inline-стиль размера шрифта.
     *
     * @param sizePx размер в дизайн-пикселях
     * @return строка стиля вида "-fx-font-size: NNpx;"
     */
    private static String fontSizeStyle(double sizePx) {
        return String.format("-fx-font-size: %.1fpx;", sizePx);
    }

    /**
     * Привязывает горячие клавиши к сцене.
     *
     * @param appSettings настройки приложения
     * @param scene       сцена
     */
    private void setupHotkeys(AppSettings appSettings, Scene scene) {
        if (scene == null) return;
        removeHotkeys();

        hotkeyFilter = event -> handleHotkey(appSettings, event);
        hotkeyScene = scene;
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, hotkeyFilter);
    }

    /**
     * Снимает ранее установленный фильтр хоткеев.
     * Панель диалога пересоздаётся при загрузке сейва и выходе в меню —
     * без снятия фильтры накапливались и Esc/Tab срабатывали многократно.
     * Вызывается также при выходе из игры, иначе игровые хоткеи продолжали
     * работать в главном меню.
     */
    public static void removeHotkeys() {
        if (hotkeyScene == null || hotkeyFilter == null) return;
        hotkeyScene.removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, hotkeyFilter);
        hotkeyScene = null;
        hotkeyFilter = null;
    }

    /**
     * Обрабатывает нажатие горячей клавиши: паззл, открытый overlay или игра.
     *
     * @param appSettings настройки приложения
     * @param event       событие клавиши
     */
    private void handleHotkey(AppSettings appSettings, javafx.scene.input.KeyEvent event) {
        if (SceneInfo.isPuzzleActive()) {
            handlePuzzleHotkey(event);
            return;
        }
        if (hasOverlayOpen(appSettings)) {
            handleOverlayHotkey(event);
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
    }

    /**
     * Обрабатывает Esc при открытой overlay-панели — закрывает меню паузы.
     *
     * @param event событие клавиши
     */
    private void handleOverlayHotkey(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE
                && GamePauseMenuController.isOpen()) {
            GamePauseMenuController.closeCurrent();
            event.consume();
        }
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
        return kz.aws.game.utils.OverlayMarker.hasOpenOverlay(appSettings.getRoot());
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
     * Устанавливает размеры шрифтов диалога и имени в дизайн-пикселях.
     * Стиль имени остаётся binding'ом — в него подмешивается цвет персонажа.
     *
     * @param settings настройки множителей
     */
    private void applyFontSizes(SceneSettings settings) {
        textContainer.setSpacing(VirtualViewport.height(0.006));
        dialogueText.setStyle(
                fontSizeStyle(VirtualViewport.height(settings.dialogFontSizeMultiplier)));

        nameText.styleProperty().bind(Bindings.concat(
                fontSizeStyle(VirtualViewport.height(settings.nameFontSizeMultiplier)),
                " ", nameColorStyle));

        dialogueText.setWrappingWidth(VirtualViewport.width(settings.textContentWidth));
        dialogueTextFlow.setPrefWidth(VirtualViewport.width(settings.textContentWidth));
    }

    /**
     * Настраивает режим градиентной панели (полупрозрачный фон снизу).
     *
     * @param settings настройки градиента
     */
    private void setupGradientMode(SceneSettings settings) {
        backgroundImageView.setVisible(false);
        backgroundImageView.setManaged(false);

        tablePane.setPrefWidth(VirtualViewport.DESIGN_WIDTH);
        tablePane.setPrefHeight(VirtualViewport.height(settings.gradientPanelHeightMultiplier));
        tablePane.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        tablePane.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s);",
                settings.gradientStartColor, settings.gradientEndColor));
        StackPane.setAlignment(tablePane, Pos.BOTTOM_CENTER);

        applyTextMargins(settings);
        applyGradientNavButtonsLayout(settings);
        setupGradientButtonStyles(settings);

        VBox.setVgrow(textContainer, javafx.scene.layout.Priority.ALWAYS);
    }

    /**
     * Настраивает режим панели с фоновой картинкой.
     * Множители размеров исторически заданы относительно противоположной оси
     * (ширина — от высоты, высота — от ширины) — сохранено для совместимости
     * существующих конфигов SceneSettings.xml.
     *
     * @param settings настройки картинки
     */
    private void setupImageMode(SceneSettings settings) {
        backgroundImageView.setVisible(true);
        backgroundImageView.setManaged(true);
        backgroundImageView.setImage(new Image(settings.tableImagePath));
        backgroundImageView.setSmooth(true);
        backgroundImageView.setFitWidth(VirtualViewport.height(settings.tableWidthMultiplier));
        backgroundImageView.setFitHeight(VirtualViewport.width(settings.tableHeightMultiplier));

        tablePane.setMaxWidth(backgroundImageView.getFitWidth());
        tablePane.setMaxHeight(backgroundImageView.getFitHeight());
        StackPane.setAlignment(tablePane, Pos.BOTTOM_CENTER);

        applyTextMargins(settings);
    }

    // ── Размеры (дизайн-пиксели) ───────────────────────────────────

    /**
     * Устанавливает отступы текста в дизайн-пикселях.
     *
     * @param settings настройки отступов
     */
    private void applyTextMargins(SceneSettings settings) {
        textContainer.setPadding(new Insets(
                VirtualViewport.height(settings.textPaddingTop),
                0, 0,
                VirtualViewport.width(settings.textPaddingLeft)));
    }

    /**
     * Устанавливает отступ кнопок снизу в режиме градиента.
     *
     * @param settings настройки
     */
    private void applyGradientNavButtonsLayout(SceneSettings settings) {
        navButtons.setSpacing(VirtualViewport.width(settings.buttonSpacingMultiplier));
        navButtons.setPadding(new Insets(0, 0, VirtualViewport.height(0.01), 0));
    }

    /**
     * Назначает размеры, шрифт и hover-анимацию навигационным кнопкам
     * в режиме градиента.
     *
     * @param settings настройки кнопок
     */
    private void setupGradientButtonStyles(SceneSettings settings) {
        for (javafx.scene.Node node : navButtons.getChildren()) {
            if (!(node instanceof Button btn)) continue;
            applyNavButtonSize(btn, settings);
            addButtonHoverAnimation(btn);
        }
    }

    /**
     * Устанавливает размеры всех навигационных кнопок в режиме картинки.
     *
     * @param settings настройки
     */
    private void applyButtonSizes(SceneSettings settings) {
        if (settingsRef.useGradientPanel) return;
        for (javafx.scene.Node node : navButtons.getChildren()) {
            if (!(node instanceof Button btn)) continue;
            applyNavButtonSize(btn, settings);
        }
    }

    /**
     * Задаёт квадратный размер и шрифт навигационной кнопки в дизайн-пикселях.
     *
     * @param btn      кнопка
     * @param settings настройки
     */
    private void applyNavButtonSize(Button btn, SceneSettings settings) {
        double size = VirtualViewport.height(settings.buttonHeightMultiplier);
        btn.setPrefWidth(size);
        btn.setPrefHeight(size);
        btn.setStyle(fontSizeStyle(
                VirtualViewport.height(settings.buttonFontSizeMultiplier * 1.2)));
    }

    /**
     * Устанавливает размер корневого контейнера равным дизайн-разрешению.
     */
    private void applyContainerSize() {
        rootPane.setPrefWidth(VirtualViewport.DESIGN_WIDTH);
        rootPane.setPrefHeight(VirtualViewport.DESIGN_HEIGHT);
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

    /**
     * Включает или выключает кнопки навигации (вперёд/назад)
     * на время активной мини-игры.
     *
     * @param enabled true — навигация доступна
     */
    @Override
    public void setNavigationEnabled(boolean enabled) {
        if (navButtons != null) {
            navButtons.setDisable(!enabled);
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
     * Устанавливает размер шрифта сегмента в дизайн-пикселях.
     *
     * @param node текстовый узел
     */
    private void bindSegmentFontSize(Text node) {
        if (settingsRef == null) return;
        node.setStyle(fontSizeStyle(
                VirtualViewport.height(settingsRef.dialogFontSizeMultiplier)));
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
