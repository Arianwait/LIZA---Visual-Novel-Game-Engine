package kz.aws.game.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.model.CharacterState;
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
    
    // Global game variables (flags)
    private Map<String, Object> gameVariables = new HashMap<>();
    /** Словарь подстановок для текста/имён: {ключ} → значение (имя игрока и др.). */
    private Map<String, String> playerVariables = new HashMap<>();

    public GameEngine(StackPane root, DialogPanel tableDetail) {
        this.renderer = new SceneRenderer(root, tableDetail);
        this.history = new ArrayList<>();
        this.allScenes = new HashMap<>();
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

    public void startNewGame(int startSceneId) {
        this.history.clear();
        this.historyIndex = -1;
        this.gameVariables.clear();
        this.playerVariables.clear();
        this.lastSavedGameVars = null;
        this.lastSavedChoices = null;
        
        // Ensure allScenes are loaded if not already (e.g. if skipped initialization)
        if (this.allScenes == null || this.allScenes.isEmpty()) {
             this.allScenes = SceneXmlParser.parseAllScenes();
        }

        loadSceneInternal(startSceneId, 0);
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

    public void restoreState(GameData data) {
        if (data == null) return;
        
        this.currentSceneId = data.getCurrentSceneId();
        this.currentFrameIndex = data.getCurrentFrameIndex();
        
        if (data.getHistory() != null) {
            this.history = new ArrayList<>(data.getHistory());
            this.historyIndex = this.history.size() - 1;
        } else {
            this.history.clear();
            this.historyIndex = -1;
        }
        
        if (data.getGameVariables() != null) {
            this.gameVariables = new HashMap<>(data.getGameVariables());
        }
        if (data.getPlayerVariables() != null) {
            this.playerVariables = new HashMap<>(data.getPlayerVariables());
        }
        if (data.getUiTheme() != null && !data.getUiTheme().isEmpty() && SceneInfo.getAppSettings() != null) {
            SceneInfo.getAppSettings().setUiTheme(data.getUiTheme());
        }
        SceneInfo.loadChoiceList(data.getChoiceList());

        // Sync delta tracking with restored state
        this.lastSavedGameVars = new HashMap<>(this.gameVariables);
        this.lastSavedChoices = SceneController.getPlayerChoicesSnapshot();

        System.out.println("Restoring Game: Scene=" + currentSceneId + ", Frame=" + currentFrameIndex + ", HistSize=" + history.size() + ", HistIndex=" + historyIndex);

        // If save had no history, record at least the current frame (will store full snapshot since lastSaved* is fresh)
        if (history.isEmpty()) {
            this.lastSavedGameVars = null;
            this.lastSavedChoices = null;
            List<SceneFrame> frames = allScenes.get(currentSceneId);
            if (frames != null && currentFrameIndex < frames.size()) {
                recordHistoryStep(frames.get(currentFrameIndex));
            }
        }
        
        renderCurrentFrameImmediate();
    }

    private void loadSceneInternal(int sceneId, int frameIndex) {
        this.currentSceneId = sceneId;
        this.currentFrameIndex = frameIndex;
        this.isFinished = false;
        
        List<SceneFrame> frames = allScenes.get(sceneId);
        if (frames == null || frames.isEmpty()) {
            System.err.println("Scene " + sceneId + " not found or empty.");
            isFinished = true; // Or handle error
            return;
        }
        
        if (frameIndex >= frames.size()) {
             System.err.println("Frame " + frameIndex + " out of bounds for scene " + sceneId);
             return;
        }

        // Record initial step in history if history is empty
        if (historyIndex == -1) {
            recordHistoryStep(frames.get(frameIndex));
        }

        showFrameExtras(frames.get(frameIndex), this::renderCurrentFrameImmediate);
    }

    private Map<String, Object> lastSavedGameVars = null;
    private Map<String, String> lastSavedChoices = null;

    private void recordHistoryStep(SceneFrame frame) {
        // If we are in the middle of history and change something, truncate future history
        if (historyIndex < history.size() - 1) {
             history.subList(historyIndex + 1, history.size()).clear();
        }

        // Only store snapshot if state changed since last save
        Map<String, Object> varsSnapshot = null;
        if (!gameVariables.equals(lastSavedGameVars)) {
            varsSnapshot = new HashMap<>(gameVariables);
            lastSavedGameVars = new HashMap<>(gameVariables);
        }

        Map<String, String> choicesNow = SceneController.getPlayerChoicesSnapshot();
        Map<String, String> choicesSnapshot = null;
        if (!choicesNow.equals(lastSavedChoices)) {
            choicesSnapshot = choicesNow;
            lastSavedChoices = new HashMap<>(choicesNow);
        }

        HistoryStep step = new HistoryStep(currentSceneId, currentFrameIndex, varsSnapshot, choicesSnapshot);
        history.add(step);
        historyIndex++;
    }

    public void next() {
        if (isFinished) {
            System.out.println("DEBUG: Next called but isFinished=true");
            return;
        }
        if (SceneInfo.isPuzzleActive()) {
            System.out.println("DEBUG: Next blocked — puzzle is active");
            return;
        }
        if (waitingForChoice) {
            System.out.println("DEBUG: Next blocked — waiting for player choice");
            return;
        }
        
        // 1. If we are traversing back-then-forward in history
        if (historyIndex < history.size() - 1) {
            historyIndex++;
            System.out.println("DEBUG: Next (History) -> Index=" + historyIndex);
            restoreStateFromHistory(history.get(historyIndex), true); // Animate? Maybe yes
            return;
        }

        // 2. Normal progression
        List<SceneFrame> currentFrames = allScenes.get(currentSceneId);
        if (currentFrames == null) return;
        
        SceneFrame currentFrame = currentFrames.get(currentFrameIndex);
        
        if (currentFrameIndex < currentFrames.size() - 1) {
            // Next frame in same scene
            SceneFrame prevFrame = currentFrame;
            currentFrameIndex++;
            SceneFrame nextFrame = currentFrames.get(currentFrameIndex);
            
            System.out.println("DEBUG: Next (Frame) -> Scene " + currentSceneId + " Frame " + currentFrameIndex
                    + " | chars in prev: " + prevFrame.getVisualState().getCharacters().entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue().isVisible())
                        .collect(java.util.stream.Collectors.joining(","))
                    + " | chars in next: " + nextFrame.getVisualState().getCharacters().entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue().isVisible())
                        .collect(java.util.stream.Collectors.joining(",")));
            recordHistoryStep(nextFrame);
            SceneFrame nf = nextFrame;
            SceneFrame pf = prevFrame;
            showFrameExtras(nextFrame, () -> renderer.renderFrame(nf, pf, true));
            
            // Legacy sync
            SceneInfo.setCliker(currentFrameIndex); 
            
        } else {
            // End of scene, check for next scene
            int nextSceneId = currentFrame.getNextSceneId();
            if (nextSceneId != -1 && allScenes.containsKey(nextSceneId)) {
                // Transition to next scene
                System.out.println("DEBUG: Next (Scene Transition) -> " + currentSceneId + " to " + nextSceneId);
                currentSceneId = nextSceneId;
                currentFrameIndex = 0;
                SceneFrame nextSceneFirstFrame = allScenes.get(currentSceneId).get(0);

                // Inherit background/music from previous scene if not set
                inheritVisualState(nextSceneFirstFrame, currentFrame);

                recordHistoryStep(nextSceneFirstFrame);
                SceneFrame firstFrame = nextSceneFirstFrame;
                showFrameExtras(nextSceneFirstFrame, () -> renderer.renderFrame(firstFrame, currentFrame, true));
                
                // Legacy sync (reset clicker for new scene? or keep global? usually reset)
                SceneInfo.setCliker(currentFrameIndex); 
            } else {
                isFinished = true;
                System.out.println("Game Finished or No Next Scene");
            }
        }
    }

    /**
     * Наследование визуального состояния при переходе между сценами.
     * Фон и музыка: если не заданы — берём из предыдущей сцены.
     * Персонажи: видимые в предыдущей сцене переносятся во ВСЕ кадры новой сцены,
     * чтобы команды (removeFromScene и др.) корректно с ними работали.
     */
    private void inheritVisualState(SceneFrame firstFrame, SceneFrame source) {
        if (firstFrame == null || source == null) return;
        VisualState sourceState = source.getVisualState();
        if (sourceState == null) return;

        // Собираем видимых персонажей из предыдущей сцены
        Map<String, CharacterState> inheritedChars = new HashMap<>();
        for (Map.Entry<String, CharacterState> entry : sourceState.getCharacters().entrySet()) {
            if (entry.getValue().isVisible()) {
                inheritedChars.put(entry.getKey(), entry.getValue().clone());
            }
        }

        System.out.println("INHERIT: Scene " + currentSceneId + " <- inherited chars: " + inheritedChars.keySet()
                + ", bg=" + sourceState.getBackgroundPath() + ", music=" + sourceState.getMusicPath());

        // Патчим все кадры новой сцены
        List<SceneFrame> newSceneFrames = allScenes.get(currentSceneId);
        if (newSceneFrames != null) {
            for (int fi = 0; fi < newSceneFrames.size(); fi++) {
                SceneFrame frame = newSceneFrames.get(fi);
                VisualState vs = frame.getVisualState();
                if (vs == null) continue;

                // Фон
                String bg = vs.getBackgroundPath();
                if (bg == null || bg.isEmpty()) {
                    vs.setBackgroundPath(sourceState.getBackgroundPath());
                }

                // Музыка
                String music = vs.getMusicPath();
                if (music == null || music.isEmpty()) {
                    vs.setMusicPath(sourceState.getMusicPath());
                }

                // Персонажи: добавляем унаследованных как visible.
                // Если кадр уже содержит этого персонажа (например, removeFromScene
                // поставил visible=false), НЕ перезаписываем — команда сцены имеет приоритет.
                // Если кадр НЕ содержит персонажа — добавляем как visible.
                for (Map.Entry<String, CharacterState> entry : inheritedChars.entrySet()) {
                    boolean alreadyHas = vs.getCharacters().containsKey(entry.getKey());
                    if (!alreadyHas) {
                        vs.updateCharacter(entry.getKey(), entry.getValue().clone());
                        System.out.println("INHERIT: frame " + fi + " <- added " + entry.getKey() + " as VISIBLE");
                    } else {
                        CharacterState existing = vs.getCharacters().get(entry.getKey());
                        System.out.println("INHERIT: frame " + fi + " already has " + entry.getKey()
                                + " visible=" + existing.isVisible() + " pose=" + existing.getPose());
                    }
                }
            }
        }
    }

    public void back() {
        if (SceneInfo.isPuzzleActive()) {
            System.out.println("DEBUG: Back blocked — puzzle is active");
            return;
        }
        if (historyIndex > 0) {
            historyIndex--;
            isFinished = false; // Reset finished state when going back
            renderer.stopAllEffects();
            System.out.println("DEBUG: Back -> Index=" + historyIndex);
            restoreStateFromHistory(history.get(historyIndex), false);
        } else {
            System.out.println("DEBUG: Back ignored (Index=" + historyIndex + ")");
        }
    }

    private void restoreStateFromHistory(HistoryStep step, boolean animate) {
        this.currentSceneId = step.getSceneId();
        this.currentFrameIndex = step.getFrameIndex();

        // Find last non-null snapshots walking back through history
        Map<String, Object> vars = findLastGameVars(historyIndex);
        Map<String, String> choices = findLastPlayerChoices(historyIndex);

        this.gameVariables = (vars != null) ? new HashMap<>(vars) : new HashMap<>();
        this.lastSavedGameVars = new HashMap<>(this.gameVariables);
        SceneController.restorePlayerChoices(choices);
        this.lastSavedChoices = (choices != null) ? new HashMap<>(choices) : new HashMap<>();
        
        // Render
        List<SceneFrame> frames = allScenes.get(currentSceneId);
        if (frames != null && currentFrameIndex < frames.size()) {
             SceneFrame frame = frames.get(currentFrameIndex);
             SceneFrame prevFrame = null;
             
             // Try to find previous frame from history to enable animation
             if (animate && historyIndex > 0) {
                 HistoryStep prevStep = history.get(historyIndex - 1);
                 List<SceneFrame> prevFrames = allScenes.get(prevStep.getSceneId());
                 if (prevFrames != null && prevStep.getFrameIndex() < prevFrames.size()) {
                     prevFrame = prevFrames.get(prevStep.getFrameIndex());
                 }
             }
             
             renderer.renderFrame(frame, prevFrame, animate); 
             
             SceneInfo.setCliker(currentFrameIndex);
        } else {
            System.err.println("Error restoring history: Scene/Frame not found (" + currentSceneId + "/" + currentFrameIndex + ")");
        }
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

    private void renderCurrentFrameImmediate() {
        List<SceneFrame> frames = allScenes.get(currentSceneId);
        if (frames != null && currentFrameIndex < frames.size()) {
            renderer.renderFrame(frames.get(currentFrameIndex), null, false);
            SceneInfo.setCliker(currentFrameIndex);
        }
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
     * Показывает панель выбора, если кадр содержит варианты.
     * Блокирует next() до выбора игрока.
     *
     * @param frame текущий кадр
     */
    private void showChoicesIfPresent(SceneFrame frame) {
        if (!frame.hasChoices()) return;

        AppSettings appSettings = SceneInfo.getAppSettings();
        StackPane root = appSettings != null ? appSettings.getRoot() : null;
        if (root == null || appSettings == null) return;

        waitingForChoice = true;

        List<ChoiceOption> options = frame.getChoices();
        List<String> texts = new ArrayList<>();
        List<Integer> sceneIds = new ArrayList<>();
        List<Boolean> trust = new ArrayList<>();

        for (ChoiceOption opt : options) {
            texts.add(opt.getText());
            sceneIds.add(opt.getTargetSceneId());
            trust.add(isChoiceAvailable(opt));
        }

        DialogChoicesController choicePane = new DialogChoicesController(appSettings);
        choicePane.createButtons(texts, sceneIds, trust, appSettings);
        choicePane.showDialogButtonPane(root);
    }

    /**
     * Проверяет доступность варианта выбора по условиям репутации и флагов.
     *
     * @param opt вариант выбора
     * @return true — вариант доступен
     */
    private boolean isChoiceAvailable(ChoiceOption opt) {
        if (!matchesChoiceConditions(opt.getAddChoice(), opt.getRemoveChoice())) return false;
        return true;
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
                loadSceneInternal(targetScene, 0);
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
