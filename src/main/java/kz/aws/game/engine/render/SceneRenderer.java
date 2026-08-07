package kz.aws.game.engine.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import kz.aws.game.engine.effect.VisualEffectPlayer;
import kz.aws.game.engine.effect.VisualEffectRegistry;
import kz.aws.game.engine.model.CharacterState;
import kz.aws.game.engine.model.ClueData;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.VisualEffectCommand;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.resources.CharacterLibrary;
import kz.aws.game.scenedetails.DimOverlay;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.soundtrack.SoundManager;
import kz.aws.game.utils.ImageViewPool;
import kz.aws.game.utils.VariableSubstitution;
import kz.aws.game.utils.VirtualViewport;

/**
 * Рендерит кадры сцены: фон, персонажи, overlay, текст, визуальные эффекты.
 *
 * <p>Визуальный контент (фон + персонажи) размещается в отдельном
 * {@code sceneContentLayer}, что позволяет применять эффекты (зум, фильтры)
 * только к игровой сцене, не затрагивая UI.
 */
public class SceneRenderer {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private final Map<String, javafx.animation.Animation> activeAnimations = new HashMap<>();
    private static final ImageViewPool imageViewPool = ImageViewPool.getInstance();

    private StackPane root;
    private final Map<String, ImageView> characterViews = new HashMap<>();
    private DialogPanel tableDetail;
    private ImageView backgroundView;
    /** Текущий полноэкранный overlay (затемнение + текст); скрывается при смене кадра без overlay. */
    private DimOverlay currentOverlay;
    /** Слой контента (фон + персонажи) — отдельный от UI для применения эффектов. */
    private Pane sceneContentLayer;
    /** Активные визуальные эффекты: ключ — effectType, значение — плеер + команда. */
    private final Map<String, ActiveEffect> activeEffectPlayers = new HashMap<>();

    private record ActiveEffect(VisualEffectPlayer player, VisualEffectCommand command) {}


    /**
     * Добавляет изображение в глобальный кеш.
     *
     * @param path  путь к изображению
     * @param image объект Image
     */
    public static void addToCache(String path, Image image) {
        imageCache.put(path, image);
    }

    /**
     * Очищает кеш игровых сцен.
     * Вызывается при возвращении в главное меню для освобождения памяти.
     */
    public static void clearSceneCache() {
        int sizeBefore = imageCache.size();
        imageCache.clear();
        System.out.println("Scene cache cleared. Freed " + sizeBefore + " images from memory.");
    }

    /**
     * Возвращает размер кеша сцен.
     *
     * @return количество закешированных изображений
     */
    public static int getSceneCacheSize() {
        return imageCache.size();
    }

    /**
     * Создаёт рендерер сцены.
     *
     * @param root        корневой StackPane приложения
     * @param tableDetail панель диалога (UI)
     */
    public SceneRenderer(StackPane root, DialogPanel tableDetail) {
        this.root = root;
        this.tableDetail = tableDetail;
        setupSceneContentLayer();
    }

    /**
     * Обновляет ссылки на root и tableDetail при пересоздании сцены.
     *
     * @param root        новый корневой StackPane
     * @param tableDetail новая панель диалога
     */
    public void updateViewReferences(StackPane root, DialogPanel tableDetail) {
        StackPane oldRoot = this.root;

        if (currentOverlay != null) {
            currentOverlay.hideOverlay(oldRoot);
            currentOverlay = null;
        }

        this.root = root;
        this.tableDetail = tableDetail;
        releaseAllCharacterViews();
        stopAllAnimations();

        migrateSceneContentLayer(oldRoot);
        migrateActiveEffects(oldRoot);
    }

    /**
     * Переносит overlay-артефакты активных эффектов из старого root в новый.
     * Эффекты на sceneContentLayer не затрагиваются — слой переносится целиком.
     *
     * @param oldRoot предыдущий корневой StackPane
     */
    private void migrateActiveEffects(StackPane oldRoot) {
        for (ActiveEffect active : activeEffectPlayers.values()) {
            active.player().migrate(oldRoot, root);
        }
    }

    /**
     * Переносит sceneContentLayer из старого root в новый,
     * сохраняя все активные эффекты (трансформации, фильтры, overlay).
     *
     * @param oldRoot предыдущий корневой StackPane
     */
    private void migrateSceneContentLayer(StackPane oldRoot) {
        if (sceneContentLayer == null) {
            setupSceneContentLayer();
            return;
        }

        oldRoot.getChildren().remove(sceneContentLayer);
        root.getChildren().add(0, sceneContentLayer);
    }

    /**
     * Возвращает слой контента (фон + персонажи) для применения визуальных эффектов.
     *
     * @return слой контента
     */
    public Pane getSceneContentLayer() {
        return sceneContentLayer;
    }

    /**
     * Запускает все визуальные эффекты кадра параллельно.
     * Если эффект с таким типом уже активен, сначала останавливает предыдущий.
     *
     * @param effects список команд визуальных эффектов из кадра
     */
    public void playVisualEffects(List<VisualEffectCommand> effects) {
        for (VisualEffectCommand cmd : effects) {
            VisualEffectPlayer player = VisualEffectRegistry.get(cmd.getEffectType());
            if (player == null) continue;

            stopEffect(cmd.getEffectType());
            String type = cmd.getEffectType();
            activeEffectPlayers.put(type, new ActiveEffect(player, cmd));
            player.play(root, sceneContentLayer, cmd, null);
        }
    }

    /**
     * Останавливает конкретный активный эффект по типу.
     *
     * @param effectType идентификатор эффекта (blink, zoom, colorFilter и др.)
     */
    public void stopEffect(String effectType) {
        ActiveEffect active = activeEffectPlayers.remove(effectType);
        if (active != null) {
            active.player().stop(root, sceneContentLayer);
        }
    }

    /**
     * Останавливает все активные визуальные эффекты.
     */
    public void stopAllEffects() {
        for (ActiveEffect active : activeEffectPlayers.values()) {
            active.player().stop(root, sceneContentLayer);
        }
        activeEffectPlayers.clear();
    }

    /**
     * Рендерит кадр: фон, персонажи, overlay, текст, стиль.
     *
     * @param frame         текущий кадр
     * @param previousFrame предыдущий кадр (для анимации перехода) или null
     * @param animate       {@code true} — анимировать переход
     */
    public void renderFrame(SceneFrame frame, SceneFrame previousFrame, boolean animate) {
        stopAllAnimations();

        applyThemeSwitch(frame);

        if (animate && previousFrame != null) {
            animateTransition(previousFrame.getVisualState(), frame.getVisualState());
        } else {
            applyVisualState(frame.getVisualState());
        }

        Map<String, String> playerVars = getPlayerVariables();
        updateOverlay(frame, playerVars);
        updateDialogText(frame, playerVars, animate);
    }

    // ── Инициализация ──────────────────────────────────────────────

    /**
     * Создаёт слой контента (sceneContentLayer) и фоновый ImageView внутри него.
     */
    private void setupSceneContentLayer() {
        if (sceneContentLayer != null) {
            root.getChildren().remove(sceneContentLayer);
        }
        if (backgroundView != null) {
            imageViewPool.release(backgroundView);
        }

        sceneContentLayer = new StackPane();
        sceneContentLayer.setPickOnBounds(false);
        sceneContentLayer.setPrefSize(VirtualViewport.DESIGN_WIDTH, VirtualViewport.DESIGN_HEIGHT);

        backgroundView = imageViewPool.acquireConfigured();
        backgroundView.setPreserveRatio(false);
        backgroundView.setFitWidth(VirtualViewport.DESIGN_WIDTH);
        backgroundView.setFitHeight(VirtualViewport.DESIGN_HEIGHT);

        sceneContentLayer.getChildren().add(backgroundView);
        root.getChildren().add(0, sceneContentLayer);
    }

    /**
     * Возвращает все ImageView персонажей в пул и очищает коллекцию.
     */
    private void releaseAllCharacterViews() {
        for (ImageView view : characterViews.values()) {
            if (view != null) {
                if (sceneContentLayer != null) {
                    sceneContentLayer.getChildren().remove(view);
                }
                root.getChildren().remove(view);
                imageViewPool.release(view);
            }
        }
        characterViews.clear();
    }

    // ── Остановка анимаций ─────────────────────────────────────────

    /**
     * Останавливает анимации переходов персонажей (fade in/out, move).
     * Визуальные эффекты НЕ сбрасываются — они живут между кадрами
     * и выключаются только явной командой {@code stopEffect}/{@code clearEffects}.
     */
    private void stopAllAnimations() {
        for (Map.Entry<String, javafx.animation.Animation> entry : new HashMap<>(activeAnimations).entrySet()) {
            entry.getValue().stop();
            if (entry.getKey().endsWith("_fadeout")) {
                String charName = entry.getKey().replace("_fadeout", "");
                removeCharacter(charName);
            }
        }
        activeAnimations.clear();
    }

    // ── Тема и переменные ──────────────────────────────────────────

    /**
     * Переключает тему интерфейса, если указано в кадре.
     *
     * @param frame текущий кадр
     */
    private void applyThemeSwitch(SceneFrame frame) {
        if (frame.getSwitchToTheme() != null && !frame.getSwitchToTheme().isEmpty()) {
            kz.aws.game.appsettings.AppSettings appSettings = SceneInfo.getAppSettings();
            if (appSettings != null) {
                appSettings.setUiTheme(frame.getSwitchToTheme());
            }
        }
    }

    /**
     * Получает словарь переменных игрока для подстановки {ключ}.
     *
     * @return словарь переменных или null
     */
    private Map<String, String> getPlayerVariables() {
        return (SceneInfo.getGameEngine() != null) ? SceneInfo.getGameEngine().getPlayerVariables() : null;
    }

    // ── Overlay ────────────────────────────────────────────────────

    /**
     * Показывает или скрывает полноэкранный DimOverlay.
     *
     * @param frame      текущий кадр
     * @param playerVars словарь подстановок
     */
    private void updateOverlay(SceneFrame frame, Map<String, String> playerVars) {
        String overlayText = frame.getOverlayText();
        if (overlayText != null && !overlayText.isEmpty()) {
            showOverlay(VariableSubstitution.replaceVariables(overlayText, playerVars));
        } else {
            hideOverlay();
        }
    }

    /**
     * Показывает overlay с текстом, скрывая панель диалога.
     *
     * @param text текст для отображения
     */
    private void showOverlay(String text) {
        if (currentOverlay != null) {
            currentOverlay.hideOverlay(root);
        }
        if (tableDetail != null) tableDetail.setPanelVisible(false);
        currentOverlay = new DimOverlay(text, 0.95);
        currentOverlay.showOverlay(root);
    }

    /**
     * Скрывает overlay и восстанавливает панель диалога.
     */
    private void hideOverlay() {
        if (currentOverlay != null) {
            currentOverlay.hideOverlay(root);
            currentOverlay = null;
        }
        if (tableDetail != null) tableDetail.setPanelVisible(true);
    }

    // ── Текст и UI ─────────────────────────────────────────────────

    /**
     * Обновляет текст диалога, имя говорящего, голос и стиль.
     *
     * @param frame      текущий кадр
     * @param playerVars словарь подстановок
     * @param animate    анимировать появление текста
     */
    private void updateDialogText(SceneFrame frame, Map<String, String> playerVars, boolean animate) {
        if (tableDetail == null) return;

        String name = VariableSubstitution.replaceVariables(frame.getSpeakerName(), playerVars);
        String text = VariableSubstitution.replaceVariables(frame.getText(), playerVars);
        tableDetail.changeNameText(name);

        if (frame.hasClues()) {
            tableDetail.changeDialogTextWithClues(text, resolveClues(frame.getClues(), playerVars), animate);
        } else {
            tableDetail.changeDialogText(text, animate);
        }

        if (animate && frame.getVoicePath() != null && !frame.getVoicePath().isEmpty()) {
            SoundManager.playVoice(frame.getVoicePath());
        }

        tableDetail.updateStyle(frame.getTextStyle());
        tableDetail.resetNameColor();
    }

    /**
     * Подставляет переменные в список подсказок (clues).
     *
     * @param clues      исходные подсказки
     * @param playerVars словарь подстановок
     * @return список с подставленными переменными
     */
    private List<ClueData> resolveClues(List<ClueData> clues, Map<String, String> playerVars) {
        List<ClueData> resolved = new ArrayList<>();
        for (ClueData c : clues) {
            resolved.add(new ClueData(
                c.getKey(),
                VariableSubstitution.replaceVariables(c.getValue(), playerVars),
                VariableSubstitution.replaceVariables(c.getDisplayText(), playerVars)
            ));
        }
        return resolved;
    }

    // ── Visual State ───────────────────────────────────────────────

    /**
     * Применяет визуальное состояние без анимации (мгновенный переход).
     *
     * @param state целевое визуальное состояние
     */
    private void applyVisualState(VisualState state) {
        if (state.getBackgroundPath() != null) {
            updateBackground(state.getBackgroundPath());
        }

        for (String name : new HashMap<>(characterViews).keySet()) {
             if (!state.getCharacters().containsKey(name) || !state.getCharacter(name).isVisible()) {
                 removeCharacter(name);
             }
        }

        for (Map.Entry<String, CharacterState> entry : state.getCharacters().entrySet()) {
            CharacterState charState = entry.getValue();
            if (charState.isVisible()) {
                ImageView view = updateCharacterView(entry.getKey(), charState, false);
                if (view != null) {
                    view.setTranslateX(calculateX(charState.getPosition()));
                    view.setOpacity(1.0);
                }
            }
        }
    }

    /**
     * Анимирует переход между двумя визуальными состояниями.
     *
     * @param oldState предыдущее состояние
     * @param newState новое состояние
     */
    private void animateTransition(VisualState oldState, VisualState newState) {
        if (!Objects.equals(newState.getBackgroundPath(), oldState.getBackgroundPath())) {
             updateBackground(newState.getBackgroundPath());
        }

        System.out.println("ANIMATE: characterViews=" + characterViews.keySet()
                + " | oldChars=" + oldState.getCharacters().entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue().isVisible())
                    .collect(java.util.stream.Collectors.joining(","))
                + " | newChars=" + newState.getCharacters().entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue().isVisible())
                    .collect(java.util.stream.Collectors.joining(",")));

        animateRemovedCharacters(oldState, newState);
        animateUpdatedCharacters(oldState, newState);
    }

    /**
     * Анимирует удаление персонажей, которых нет в новом состоянии.
     *
     * @param oldState предыдущее состояние
     * @param newState новое состояние
     */
    private void animateRemovedCharacters(VisualState oldState, VisualState newState) {
        for (String name : new HashMap<>(characterViews).keySet()) {
            boolean visibleInNew = newState.getCharacters().containsKey(name) && newState.getCharacter(name).isVisible();
            if (visibleInNew) continue;

            boolean logicallyVisibleInOld = oldState.getCharacters().containsKey(name) && oldState.getCharacter(name).isVisible();

            System.out.println("ANIMATE: REMOVING " + name + " visibleInNew=" + visibleInNew
                    + " logicallyVisibleInOld=" + logicallyVisibleInOld);

            if (logicallyVisibleInOld) {
                fadeOutCharacter(name);
            } else {
                removeCharacter(name);
            }
        }
    }

    /**
     * Плавно скрывает персонажа с fade out анимацией.
     *
     * @param name имя персонажа
     */
    private void fadeOutCharacter(String name) {
        ImageView view = characterViews.get(name);
        if (view == null) return;
        view.setOpacity(1.0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), view);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            removeCharacter(name);
            activeAnimations.remove(name + "_fadeout");
        });
        activeAnimations.put(name + "_fadeout", ft);
        ft.play();
    }

    /**
     * Анимирует появление и перемещение персонажей в новом состоянии.
     *
     * @param oldState предыдущее состояние
     * @param newState новое состояние
     */
    private void animateUpdatedCharacters(VisualState oldState, VisualState newState) {
        for (Map.Entry<String, CharacterState> entry : newState.getCharacters().entrySet()) {
            String name = entry.getKey();
            CharacterState newCharState = entry.getValue();
            if (!newCharState.isVisible()) continue;

            CharacterState oldCharState = oldState.getCharacter(name);
            boolean wasVisible = oldCharState.isVisible();
            ImageView view = updateCharacterView(name, newCharState, !wasVisible);

            if (wasVisible && oldCharState.getPosition() != newCharState.getPosition()) {
                animateCharacterMove(name, view, oldCharState, newCharState);
            } else if (!wasVisible) {
                fadeInCharacter(name, view, newCharState);
            } else {
                view.setTranslateX(calculateX(newCharState.getPosition()));
                view.setOpacity(1.0);
            }
        }
    }

    /**
     * Анимирует перемещение персонажа из старой позиции в новую.
     *
     * @param name         имя персонажа
     * @param view         ImageView персонажа
     * @param oldCharState старое состояние
     * @param newCharState новое состояние
     */
    private void animateCharacterMove(String name, ImageView view,
                                       CharacterState oldCharState, CharacterState newCharState) {
        view.setOpacity(1.0);
        view.setTranslateX(calculateX(oldCharState.getPosition()));
        double targetX = calculateX(newCharState.getPosition());
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1.0), view);
        tt.setToX(targetX);
        tt.setOnFinished(e -> {
            view.setTranslateX(targetX);
            activeAnimations.remove(name + "_move");
        });
        activeAnimations.put(name + "_move", tt);
        tt.play();
    }

    /**
     * Плавно показывает персонажа с fade in анимацией.
     *
     * @param name         имя персонажа
     * @param view         ImageView персонажа
     * @param newCharState состояние с позицией
     */
    private void fadeInCharacter(String name, ImageView view, CharacterState newCharState) {
        view.setOpacity(0.0);
        view.setTranslateX(calculateX(newCharState.getPosition()));
        FadeTransition ft = new FadeTransition(Duration.millis(500), view);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setOnFinished(e -> activeAnimations.remove(name + "_fadein"));
        activeAnimations.put(name + "_fadein", ft);
        ft.play();
    }

    // ── Персонажи и фон ────────────────────────────────────────────

    /**
     * Обновляет или создаёт ImageView персонажа с нужной позой.
     *
     * @param charName      имя персонажа
     * @param charState     состояние (поза, позиция)
     * @param animateFadeIn анимировать появление
     * @return ImageView персонажа или null если поза не найдена
     */
    private ImageView updateCharacterView(String charName, CharacterState charState, boolean animateFadeIn) {
        String posePath = CharacterLibrary.getPosePath(charName, charState.getPose());
        if (posePath == null) return null;

        ImageView view = getOrCreateImageView(charName);
        Image image = loadImage(posePath);
        if (view.getImage() != image) {
            view.setImage(image);
        }

        addCharacterToLayer(view);
        view.setVisible(true);
        return view;
    }

    /**
     * Добавляет ImageView персонажа в sceneContentLayer (выше фона).
     *
     * @param view ImageView для добавления
     */
    private void addCharacterToLayer(ImageView view) {
        if (sceneContentLayer.getChildren().contains(view)) return;
        int bgIndex = sceneContentLayer.getChildren().indexOf(backgroundView);
        if (bgIndex >= 0) {
            sceneContentLayer.getChildren().add(bgIndex + 1, view);
        } else {
            sceneContentLayer.getChildren().add(0, view);
        }
    }

    /**
     * Загружает изображение из кеша или с диска.
     *
     * @param path путь к изображению
     * @return загруженный Image
     */
    private Image loadImage(String path) {
        Image image = imageCache.get(path);
        if (image == null) {
            try {
                image = new Image(path, 0, 0, true, true, true);
                imageCache.put(path, image);
            } catch (Exception e) {
                System.err.println("Could not load image: " + path);
            }
        }
        return image;
    }

    /**
     * Обновляет фоновое изображение сцены.
     *
     * @param path путь к файлу фона
     */
    private void updateBackground(String path) {
        if (path == null) return;
        String normalizedPath = path.replace("\\", "/");
        String fullPath = normalizedPath.startsWith("file:") ? normalizedPath : "file:" + normalizedPath;

        System.out.println("DEBUG: Loading background: " + fullPath);

        try {
            Image bgImage = imageCache.get(fullPath);
            if (bgImage == null) {
                System.out.println("DEBUG: Cache miss. Loading synchronously...");
                bgImage = new Image(fullPath, 0, 0, true, true, false);
                imageCache.put(fullPath, bgImage);
            }

            if (bgImage.isError()) {
                 System.err.println("DEBUG: Image load error for " + fullPath + ": " + bgImage.getException());
            } else {
                 System.out.println("DEBUG: Image loaded successfully. W=" + bgImage.getWidth() + " H=" + bgImage.getHeight());
            }

            applyBackgroundImage(bgImage);
        } catch (Exception e) {
             System.err.println("Failed to load background: " + fullPath);
             e.printStackTrace();
        }
    }

    /**
     * Устанавливает изображение на backgroundView и гарантирует его видимость.
     *
     * @param bgImage изображение фона
     */
    private void applyBackgroundImage(Image bgImage) {
        if (backgroundView == null) {
            System.err.println("DEBUG: backgroundView is NULL!");
            return;
        }
        backgroundView.setImage(bgImage);
        if (!sceneContentLayer.getChildren().contains(backgroundView)) {
            System.err.println("DEBUG: backgroundView is NOT in sceneContentLayer!");
            sceneContentLayer.getChildren().add(0, backgroundView);
        }
        backgroundView.toBack();
        backgroundView.setVisible(true);
        sceneContentLayer.toBack();
        System.out.println("DEBUG: Background set. Visible=" + backgroundView.isVisible()
                + " Opacity=" + backgroundView.getOpacity());
        System.out.println("DEBUG: Root Children Count: " + root.getChildren().size());
    }

    /**
     * Получает или создаёт ImageView для персонажа.
     *
     * @param name имя персонажа
     * @return ImageView (из кеша или пула)
     */
    private ImageView getOrCreateImageView(String name) {
        return characterViews.computeIfAbsent(name, k -> {
            ImageView iv = imageViewPool.acquireConfigured();
            iv.setFitHeight(VirtualViewport.height(0.8));
            StackPane.setAlignment(iv, Pos.BOTTOM_CENTER);
            return iv;
        });
    }

    /**
     * Удаляет персонажа со сцены и возвращает ImageView в пул.
     *
     * @param name имя персонажа
     */
    private void removeCharacter(String name) {
        ImageView view = characterViews.remove(name);
        if (view != null) {
            view.setVisible(false);
            sceneContentLayer.getChildren().remove(view);
            imageViewPool.release(view);
        }
    }

    /**
     * Вычисляет X-смещение персонажа от центра экрана (LEFT/CENTER/RIGHT)
     * в дизайн-пикселях.
     *
     * @param position позиция персонажа
     * @return смещение по X в пикселях
     */
    private double calculateX(CharacterState.Position position) {
        return switch (position) {
            case LEFT -> -VirtualViewport.width(0.3);
            case RIGHT -> VirtualViewport.width(0.3);
            default -> 0;
        };
    }
}
