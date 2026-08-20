package kz.aws.game.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.model.HistoryStep;
import kz.aws.game.engine.model.PuzzleCommand;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.StopEffectCommand;
import kz.aws.game.engine.model.VisualEffectCommand;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.parser.SceneXmlParser;
import kz.aws.game.engine.render.SceneRenderer;
import kz.aws.game.panel.BaseGamePanel;
import kz.aws.game.panel.GamePanel;
import kz.aws.game.panel.PanelContext;
import kz.aws.game.panel.PuzzleRegistry;
import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.scenedetails.DialogChoicesController;
import kz.aws.game.scenedetails.NameInputPane;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenelist.GameData;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Игровой движок визуальной новеллы: машина состояний повествования
 * (next/back/jumpTo/choose), история с дельта-снимками, сохранение и
 * восстановление состояния, запуск паззлов и панели выбора.
 *
 * Script-слой (allScenes из XML) после парсинга не мутируется:
 * на экран уходят display-копии кадров с runtime-визуалом.
 */
public class GameEngine {
    private Map<Integer, List<SceneFrame>> allScenes;
    private List<HistoryStep> history;
    private int historyIndex = -1;

    // Current State
    private int currentSceneId;
    private int currentFrameIndex;

    private final SceneRenderer renderer;
    private boolean isFinished = false;
    private boolean waitingForChoice = false;

    /** Runtime-визуал: то, что сейчас на экране; script-кадры не мутируются. */
    private VisualState runtimeVisual;
    /** Последний отображённый display-кадр — prev для анимации перехода. */
    private SceneFrame lastDisplayed;
    /** Открытая панель выбора; null — панель не показана. */
    private DialogChoicesController activeChoicePane;

    // Дельта-трекинг истории: снимки пишутся только при изменении
    private Map<String, Object> lastSavedGameVars = null;
    private Map<String, String> lastSavedChoices = null;
    private VisualState lastSavedVisual = null;

    // Global game variables (flags)
    private Map<String, Object> gameVariables = new HashMap<>();
    /** Словарь подстановок для текста/имён: {ключ} → значение (имя игрока и др.). */
    private Map<String, String> playerVariables = new HashMap<>();

    public GameEngine(StackPane root, DialogPanel tableDetail) {
        this.renderer = new SceneRenderer(root, tableDetail);
        this.history = new ArrayList<>();
        this.allScenes = new HashMap<>();
    }

    /**
     * Конструктор для тестов: позволяет подменить рендерер и набор сцен
     * без парсинга XML и создания JavaFX-иерархии.
     *
     * @param renderer стаб рендерера
     * @param scenes   готовый набор сцен
     */
    GameEngine(SceneRenderer renderer, Map<Integer, List<SceneFrame>> scenes) {
        this.renderer = renderer;
        this.history = new ArrayList<>();
        this.allScenes = scenes;
    }
    
    public void updateView(StackPane root, DialogPanel tableDetail) {
        this.renderer.updateViewReferences(root, tableDetail);
        // Force re-render of current state to bind to new view
        renderCurrentFrameImmediate();
    }

    public void initializeAndLoadAll() {
        kz.aws.game.engine.resources.CharacterLibrary.initialize();
        this.allScenes = SceneXmlParser.parseAllScenes();
        System.out.println("Loaded scenes: " + allScenes.keySet());
    }

    /**
     * Начинает новую игру: полностью сбрасывает runtime-состояние движка
     * и глобальное состояние (флаги, репутацию, выборы, список реплик),
     * затем переходит на стартовую сцену.
     *
     * @param startSceneId id стартовой сцены
     */
    public void startNewGame(int startSceneId) {
        history.clear();
        historyIndex = -1;
        gameVariables.clear();
        playerVariables.clear();
        lastSavedGameVars = null;
        lastSavedChoices = null;
        lastSavedVisual = null;
        runtimeVisual = null;
        lastDisplayed = null;
        waitingForChoice = false;
        isFinished = false;

        SceneController.resetGameData();
        SceneInfo.clearChoices();
        SceneInfo.clearDialogs();

        ensureScenesLoaded();
        jumpTo(startSceneId);
    }

    /** Загружает сцены, если инициализация была пропущена. */
    private void ensureScenesLoaded() {
        if (allScenes == null || allScenes.isEmpty()) {
            allScenes = SceneXmlParser.parseAllScenes();
        }
    }

    public GameData getSaveData() {
        GameData data = new GameData();
        data.setCurrentSceneId(currentSceneId);
        data.setCurrentFrameIndex(currentFrameIndex);
        data.setHistory(new ArrayList<>(history));
        data.setGameVariables(new HashMap<>(gameVariables));
        data.setPlayerVariables(new HashMap<>(playerVariables));
        data.setClicker(currentFrameIndex);
        data.setChoice(SceneInfo.getChoiceList());
        if (SceneInfo.getAppSettings() != null) {
            data.setUiTheme(SceneInfo.getAppSettings().getUiTheme());
        }
        return data;
    }

    /**
     * Восстанавливает состояние движка из данных сохранения и рендерит текущий кадр.
     *
     * @param data данные сохранения (null игнорируется)
     */
    public void restoreState(GameData data) {
        if (data == null) return;

        currentSceneId = data.getCurrentSceneId();
        currentFrameIndex = data.getCurrentFrameIndex();
        waitingForChoice = false;
        isFinished = false;
        lastDisplayed = null;

        restoreHistory(data);
        restoreVariables(data);
        restoreUiState(data);
        syncDeltaTracking();
        ensureHistoryNotEmpty();
        renderCurrentFrameImmediate();
    }

    /**
     * Восстанавливает список истории и позицию в нём.
     *
     * @param data данные сохранения
     */
    private void restoreHistory(GameData data) {
        if (data.getHistory() != null) {
            history = new ArrayList<>(data.getHistory());
            historyIndex = history.size() - 1;
        } else {
            history.clear();
            historyIndex = -1;
        }
    }

    /**
     * Восстанавливает игровые флаги и пользовательские переменные.
     *
     * @param data данные сохранения
     */
    private void restoreVariables(GameData data) {
        if (data.getGameVariables() != null) {
            gameVariables = new HashMap<>(data.getGameVariables());
        }
        if (data.getPlayerVariables() != null) {
            playerVariables = new HashMap<>(data.getPlayerVariables());
        }
    }

    /**
     * Восстанавливает тему интерфейса и список сделанных выборов.
     *
     * @param data данные сохранения
     */
    private void restoreUiState(GameData data) {
        if (data.getUiTheme() != null && !data.getUiTheme().isEmpty()
                && SceneInfo.getAppSettings() != null) {
            SceneInfo.getAppSettings().setUiTheme(data.getUiTheme());
        }
        SceneInfo.loadChoiceList(data.getChoiceList());
    }

    /** Синхронизирует дельта-трекинг и runtime-визуал с загруженной историей. */
    private void syncDeltaTracking() {
        lastSavedGameVars = new HashMap<>(gameVariables);
        lastSavedChoices = SceneController.getPlayerChoicesSnapshot();
        VisualState visual = findLastVisualSnapshot(historyIndex);
        runtimeVisual = (visual != null) ? visual.clone() : null;
        lastSavedVisual = (visual != null) ? visual.clone() : null;
    }

    /** Если сейв без истории — записывает полный снимок текущего кадра. */
    private void ensureHistoryNotEmpty() {
        if (!history.isEmpty()) return;
        lastSavedGameVars = null;
        lastSavedChoices = null;
        lastSavedVisual = null;
        recordHistoryStep();
    }

    /**
     * Переход на сцену внутри игры — без очистки истории и переменных.
     * Используется выборами, паззлами и переходом nextScene.
     *
     * @param sceneId id целевой сцены
     */
    public void jumpTo(int sceneId) {
        waitingForChoice = false;

        List<SceneFrame> frames = allScenes.get(sceneId);
        if (frames == null || frames.isEmpty()) {
            System.err.println("jumpTo: scene " + sceneId + " not found or empty");
            isFinished = true;
            return;
        }

        isFinished = false;
        currentSceneId = sceneId;
        currentFrameIndex = 0;

        SceneFrame script = frames.get(0);
        SceneFrame display = present(script);
        recordHistoryStep();
        showFrameExtras(script, () -> renderDisplay(display, lastDisplayed != null));
    }

    /**
     * Готовит кадр к показу: объединяет script-визуал с текущим runtime-визуалом
     * и обновляет runtimeVisual. Script-кадр не мутируется.
     *
     * @param script кадр из allScenes
     * @return display-копия кадра с runtime-визуалом
     */
    private SceneFrame present(SceneFrame script) {
        runtimeVisual = VisualState.merge(script.getVisualState(), runtimeVisual);
        return script.withVisual(runtimeVisual);
    }

    /**
     * Рендерит display-кадр, запоминает его как последний показанный
     * и синхронизирует legacy-счётчик кадров.
     *
     * @param display кадр с runtime-визуалом
     * @param animate анимировать ли переход от прошлого кадра
     */
    private void renderDisplay(SceneFrame display, boolean animate) {
        renderer.renderFrame(display, animate ? lastDisplayed : null, animate);
        lastDisplayed = display;
        SceneInfo.setCliker(currentFrameIndex);
    }

    /**
     * Записывает шаг истории с дельта-снимками изменившихся переменных,
     * выборов и runtime-визуала (null — без изменений). Обрезает redo-хвост.
     */
    private void recordHistoryStep() {
        if (historyIndex < history.size() - 1) {
            history.subList(historyIndex + 1, history.size()).clear();
        }
        history.add(new HistoryStep(currentSceneId, currentFrameIndex,
                snapshotGameVarsIfChanged(), snapshotChoicesIfChanged(), snapshotVisualIfChanged()));
        historyIndex++;
    }

    /**
     * @return снимок gameVariables, если они изменились с прошлого шага; иначе null
     */
    private Map<String, Object> snapshotGameVarsIfChanged() {
        if (gameVariables.equals(lastSavedGameVars)) return null;
        lastSavedGameVars = new HashMap<>(gameVariables);
        return new HashMap<>(gameVariables);
    }

    /**
     * @return снимок playerChoices, если они изменились с прошлого шага; иначе null
     */
    private Map<String, String> snapshotChoicesIfChanged() {
        Map<String, String> now = SceneController.getPlayerChoicesSnapshot();
        if (now.equals(lastSavedChoices)) return null;
        lastSavedChoices = new HashMap<>(now);
        return now;
    }

    /**
     * @return клон runtime-визуала, если он изменился с прошлого шага; иначе null
     */
    private VisualState snapshotVisualIfChanged() {
        if (runtimeVisual == null || runtimeVisual.equals(lastSavedVisual)) return null;
        lastSavedVisual = runtimeVisual.clone();
        return runtimeVisual.clone();
    }

    /**
     * Продвигает диалог вперёд: по истории (redo), на следующий кадр
     * или на следующую сцену. Блокируется активным паззлом и ожиданием выбора.
     */
    public void next() {
        if (isFinished || SceneInfo.isPuzzleActive() || waitingForChoice) return;

        if (historyIndex < history.size() - 1) {
            historyIndex++;
            restoreStateFromHistory(history.get(historyIndex), true);
            return;
        }

        List<SceneFrame> frames = allScenes.get(currentSceneId);
        if (frames == null) return;

        if (currentFrameIndex < frames.size() - 1) {
            advanceFrame(frames);
        } else {
            advanceScene(frames.get(currentFrameIndex));
        }
    }

    /**
     * Показывает следующий кадр текущей сцены.
     *
     * @param frames кадры текущей сцены
     */
    private void advanceFrame(List<SceneFrame> frames) {
        currentFrameIndex++;
        SceneFrame script = frames.get(currentFrameIndex);
        SceneFrame display = present(script);
        recordHistoryStep();
        showFrameExtras(script, () -> renderDisplay(display, true));
    }

    /**
     * Завершает сцену: переходит на nextSceneId или завершает игру.
     *
     * @param lastFrame последний кадр текущей сцены
     */
    private void advanceScene(SceneFrame lastFrame) {
        int nextSceneId = lastFrame.getNextSceneId();
        if (nextSceneId != -1 && allScenes.containsKey(nextSceneId)) {
            jumpTo(nextSceneId);
        } else {
            isFinished = true;
        }
    }

    /** Возвращается на шаг назад по истории. Блокируется активным паззлом. */
    public void back() {
        if (SceneInfo.isPuzzleActive()) return;
        if (historyIndex <= 0) return;
        historyIndex--;
        isFinished = false;
        renderer.stopAllEffects();
        restoreStateFromHistory(history.get(historyIndex), false);
    }

    /**
     * Восстанавливает состояние из шага истории (позицию, переменные, выборы,
     * runtime-визуал — из ближайших непустых снимков) и рендерит кадр.
     *
     * @param step    шаг истории
     * @param animate анимировать ли переход
     */
    private void restoreStateFromHistory(HistoryStep step, boolean animate) {
        currentSceneId = step.getSceneId();
        currentFrameIndex = step.getFrameIndex();
        restoreSnapshotsFromHistory();

        List<SceneFrame> frames = allScenes.get(currentSceneId);
        if (frames == null || currentFrameIndex >= frames.size()) {
            System.err.println("Error restoring history: Scene/Frame not found ("
                    + currentSceneId + "/" + currentFrameIndex + ")");
            return;
        }
        SceneFrame script = frames.get(currentFrameIndex);
        renderDisplay(present(script), animate);
        reopenChoicesIfPresent(script);
    }

    /**
     * Пересоздаёт панель выбора после восстановления кадра (back/redo/загрузка):
     * закрывает устаревшую панель и, если кадр содержит выбор, показывает её
     * заново — иначе откат на choice-кадр позволил бы уйти мимо ветки.
     *
     * @param script восстановленный script-кадр
     */
    private void reopenChoicesIfPresent(SceneFrame script) {
        closeActiveChoicePane();
        waitingForChoice = false;
        showChoicesIfPresent(script);
    }

    /** Закрывает открытую панель выбора, если она есть. */
    private void closeActiveChoicePane() {
        if (activeChoicePane == null) return;
        AppSettings appSettings = SceneInfo.getAppSettings();
        if (appSettings != null && appSettings.getRoot() != null) {
            activeChoicePane.closeDialogButtonPane(appSettings.getRoot());
        }
        activeChoicePane = null;
    }

    /** Восстанавливает переменные, выборы и визуал из ближайших непустых снимков истории. */
    private void restoreSnapshotsFromHistory() {
        Map<String, Object> vars = findLastGameVars(historyIndex);
        Map<String, String> choices = findLastPlayerChoices(historyIndex);
        gameVariables = (vars != null) ? new HashMap<>(vars) : new HashMap<>();
        lastSavedGameVars = new HashMap<>(gameVariables);
        SceneController.restorePlayerChoices(choices);
        lastSavedChoices = (choices != null) ? new HashMap<>(choices) : new HashMap<>();

        VisualState visual = findLastVisualSnapshot(historyIndex);
        runtimeVisual = (visual != null) ? visual.clone() : null;
        lastSavedVisual = (visual != null) ? visual.clone() : null;
    }

    /** Walk back to find the last non-null gameVariables snapshot. */
    private Map<String, Object> findLastGameVars(int fromIndex) {
        for (int i = fromIndex; i >= 0; i--) {
            Map<String, Object> v = history.get(i).getGameVariables();
            if (v != null) return v;
        }
        return null;
    }

    /** Walk back to find the last non-null playerChoices snapshot. */
    private Map<String, String> findLastPlayerChoices(int fromIndex) {
        for (int i = fromIndex; i >= 0; i--) {
            Map<String, String> c = history.get(i).getPlayerChoicesSnapshot();
            if (c != null) return c;
        }
        return null;
    }

    /** Walk back to find the last non-null visual snapshot. */
    private VisualState findLastVisualSnapshot(int fromIndex) {
        for (int i = fromIndex; i >= 0; i--) {
            VisualState v = history.get(i).getVisualSnapshot();
            if (v != null) return v;
        }
        return null;
    }

    /** Мгновенно (без анимации) рендерит текущий кадр с runtime-визуалом. */
    private void renderCurrentFrameImmediate() {
        List<SceneFrame> frames = allScenes.get(currentSceneId);
        if (frames == null || currentFrameIndex >= frames.size()) return;
        SceneFrame script = frames.get(currentFrameIndex);
        renderDisplay(present(script), false);
        reopenChoicesIfPresent(script);
    }
    
    public void handleMouseClick(MouseEvent event) {
        next();
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    public int getCurrentSceneId() { return currentSceneId; }

    public int getFrameCount() {
        if (allScenes.containsKey(currentSceneId)) {
            return allScenes.get(currentSceneId).size();
        }
        return 0;
    }
    
    /**
     * Если у кадра есть запрос ввода — показывает окно ввода в стиле игры (как сохранение/загрузка),
     * после закрытия вызывает afterInput (отрисовка кадра).
     */
    private void showInputDialogIfNeeded(SceneFrame frame, Runnable afterInput) {
        String key = frame.getInputRequestKey();
        if (key == null || key.isEmpty()) {
            if (afterInput != null) afterInput.run();
            return;
        }
        String prompt = frame.getInputRequestPrompt() != null ? frame.getInputRequestPrompt() : "Введите имя:";
        showInputDialogFor(key, prompt, afterInput);
    }

    /**
     * Открывает окно ввода в стиле игры (фон как у сохранения/загрузки) и сохраняет значение по ключу.
     * Вызывать из меню (кнопка «Ввести имя») или из сценария.
     *
     * @param key       Ключ переменной (например, "playerName").
     * @param prompt    Подсказка в окне (например, "Введите имя:").
     * @param onComplete Если не null — вызывается после OK или Отмена (для сценария: отрисовка кадра).
     */
    public void showInputDialogFor(String key, String prompt, Runnable onComplete) {
        if (key == null || key.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        AppSettings appSettings = SceneInfo.getAppSettings();
        StackPane root = appSettings != null ? appSettings.getRoot() : null;
        if (root == null || appSettings == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        String current = getPlayerVariable(key);
        String label = prompt != null && !prompt.isEmpty() ? prompt : "Введите значение:";
        NameInputPane.show(root, appSettings, label, current,
                value -> {
                    setPlayerVariable(key, value);
                    if (onComplete != null) onComplete.run();
                },
                () -> {
                    if (onComplete != null) onComplete.run();
                });
    }

    /**
     * Цепочка: сначала показываем диалог ввода (если нужен),
     * потом рендерим кадр (afterAll), потом показываем панель поверх (если есть).
     */
    private void showFrameExtras(SceneFrame frame, Runnable afterAll) {
        showInputDialogIfNeeded(frame, () -> {
            // Сначала рендерим кадр — фон и текст уже на месте
            if (afterAll != null) afterAll.run();
            // Визуальные эффекты (blink, zoom, colorFilter) поверх рендера
            executeVisualEffects(frame);
            // Потом поверх открываем панель (если есть)
            executePuzzleIfPresent(frame);
            // Показываем варианты выбора (если есть)
            showChoicesIfPresent(frame);
        });
    }

    /**
     * Обрабатывает выбор игрока: снимает блокировку диалога, записывает выбор
     * и переходит на целевую сцену. Прогресс (история, переменные) сохраняется.
     *
     * @param option выбранный вариант (null игнорируется)
     */
    public void choose(ChoiceOption option) {
        waitingForChoice = false;
        activeChoicePane = null;
        if (option == null || !isChoiceAvailable(option)) return;
        SceneInfo.addChoice(option.getText());
        jumpTo(option.getTargetSceneId());
    }

    /**
     * Показывает панель выбора, если кадр содержит доступные варианты.
     * Блокирует next() до выбора игрока.
     *
     * @param frame текущий кадр
     */
    private void showChoicesIfPresent(SceneFrame frame) {
        if (!frame.hasChoices()) return;

        AppSettings appSettings = SceneInfo.getAppSettings();
        StackPane root = appSettings != null ? appSettings.getRoot() : null;
        if (root == null) return;

        List<ChoiceOption> available = frame.getChoices().stream()
                .filter(this::isChoiceAvailable)
                .toList();
        if (available.isEmpty()) return;

        waitingForChoice = true;
        DialogChoicesController choicePane = new DialogChoicesController(appSettings);
        choicePane.createButtons(available, this::choose);
        choicePane.showDialogButtonPane(root);
        activeChoicePane = choicePane;
    }

    /**
     * Проверяет доступность варианта выбора: условия addChoice/removeChoice
     * и диапазон репутации персонажа (characterChoice + minRep/maxRep).
     *
     * @param opt вариант выбора
     * @return true — вариант доступен
     */
    private boolean isChoiceAvailable(ChoiceOption opt) {
        if (!matchesChoiceConditions(opt.getAddChoice(), opt.getRemoveChoice())) return false;
        return matchesReputationCondition(opt);
    }

    /**
     * Проверяет условие репутации варианта выбора.
     *
     * @param opt вариант выбора
     * @return true — условие не задано или репутация в диапазоне [minRep, maxRep]
     */
    private boolean matchesReputationCondition(ChoiceOption opt) {
        if (opt.getCharacterChoice().isEmpty()) return true;
        int rep = SceneController.getCharacterReputation(opt.getCharacterChoice());
        return rep >= opt.getMinRep() && rep <= opt.getMaxRep();
    }

    /**
     * Проверяет условия addChoice/removeChoice.
     *
     * @param required  требуемые выборы (через ;)
     * @param forbidden запрещённые выборы (через ;)
     * @return true — условия выполнены
     */
    private boolean matchesChoiceConditions(String required, String forbidden) {
        if (required != null && !required.isEmpty()) {
            for (String c : required.split(";")) {
                if (!c.isEmpty() && !SceneInfo.containsChoice(c)) return false;
            }
        }
        if (forbidden != null && !forbidden.isEmpty()) {
            for (String c : forbidden.split(";")) {
                if (!c.isEmpty() && SceneInfo.containsChoice(c)) return false;
            }
        }
        return true;
    }

    /**
     * Извлекает {@link VisualEffectCommand} из entryAnimations кадра
     * и передаёт их {@link SceneRenderer} для воспроизведения.
     *
     * @param frame текущий кадр
     */
    private void executeVisualEffects(SceneFrame frame) {
        if (frame.getEntryAnimations() == null) return;
        List<VisualEffectCommand> effects = frame.getEntryAnimations().stream()
                .filter(a -> a instanceof VisualEffectCommand)
                .map(a -> (VisualEffectCommand) a)
                .toList();
        for (VisualEffectCommand cmd : effects) {
            if (cmd instanceof StopEffectCommand stop) {
                executeStopEffect(stop);
            } else {
                renderer.playVisualEffects(List.of(cmd));
            }
        }
    }

    /**
     * Обрабатывает команду остановки эффекта: конкретного или всех.
     *
     * @param stop команда остановки
     */
    private void executeStopEffect(StopEffectCommand stop) {
        if (stop.isClearAll()) {
            renderer.stopAllEffects();
        } else {
            renderer.stopEffect(stop.getTargetEffect());
        }
    }

    /**
     * Если в кадре есть {@link PuzzleCommand} — показывает загадку поверх сцены.
     * Блокирует клики на время загадки. По завершении: пишет флаг и переходит на сцену
     * (или вызывает {@code onComplete} если переход не задан).
     */
    private void executePuzzleIfPresent(SceneFrame frame) {
        executePuzzleIfPresent(frame, null);
    }

    private void executePuzzleIfPresent(SceneFrame frame, Runnable onComplete) {
        if (frame.getEntryAnimations() == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        PuzzleCommand cmd = frame.getEntryAnimations().stream()
                .filter(a -> a instanceof PuzzleCommand)
                .map(a -> (PuzzleCommand) a)
                .findFirst()
                .orElse(null);
        if (cmd == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        AppSettings appSettings = SceneInfo.getAppSettings();
        if (appSettings == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        BaseGamePanel panel = PuzzleRegistry.create(cmd.getPuzzleId());
        if (panel == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        panel.init(new PanelContext(appSettings));
        StackPane root = appSettings.getRoot();

        // Блокировка кликов для модального режима (без затемнения фона)
        GamePanel annotation = panel.getClass().getAnnotation(GamePanel.class);
        if (annotation != null && annotation.modal()) {
            SceneInfo.disableEventHandler(root);
        }

        SceneInfo.setActivePuzzle(panel);
        setDialogNavigationEnabled(false);
        final PuzzleCommand finalCmd = cmd;
        panel.setOnComplete(result -> {
            SceneInfo.setActivePuzzle(null);
            setDialogNavigationEnabled(true);
            root.getChildren().remove(panel);
            SceneInfo.enableEventHandler(root);

            // close() — просто закрыть, без записи флагов и переходов
            if (result.isCloseOnly()) {
                if (onComplete != null) onComplete.run();
                return;
            }

            if (!finalCmd.getFlag().isEmpty()) {
                SceneController.setFlag(finalCmd.getFlag(), result.isSuccess());
                if (!result.getData().isEmpty()) {
                    SceneController.setPlayerChoice(finalCmd.getFlag() + "_data", result.getData());
                }
            }

            int targetScene = result.isSuccess() ? finalCmd.getOnSuccess() : finalCmd.getOnFailure();
            if (targetScene > 0) {
                jumpTo(targetScene);
            } else if (onComplete != null) {
                onComplete.run();
            }
        });

        root.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);
        panel.toFront();
    }

    /**
     * Включает или выключает кнопки навигации диалога (вперёд/назад)
     * на время активной мини-игры.
     *
     * @param enabled true — навигация доступна
     */
    private void setDialogNavigationEnabled(boolean enabled) {
        DialogPanel dialog = SceneInfo.getTableDatail();
        if (dialog != null) {
            dialog.setNavigationEnabled(enabled);
        }
    }

    /** Вызов из кнопки меню (без onComplete). */
    public void showInputDialogFor(String key, String prompt) {
        showInputDialogFor(key, prompt, null);
    }

    /** Словарь подстановок для {ключ} в тексте и имени персонажа. */
    public Map<String, String> getPlayerVariables() {
        return playerVariables;
    }

    public void setPlayerVariable(String key, String value) {
        if (key != null && !key.isEmpty()) {
            playerVariables.put(key, value != null ? value : "");
        }
    }

    public String getPlayerVariable(String key) {
        return key == null ? null : playerVariables.getOrDefault(key, "");
    }

    public void cleanup() {
        allScenes.clear();
        history.clear();
    }
}
