package kz.aws.game.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.layout.StackPane;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.render.SceneRenderer;
import kz.aws.game.scenelist.GameData;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Проверяет сохранение и загрузку: полный roundtrip состояния,
 * совместимость с сейвами старого формата (без новых полей)
 * и устойчивость к сценарию, урезанному после создания сейва.
 */
class GameEngineSaveLoadTest {

    /** Стаб рендерера: не трогает JavaFX-анимации. */
    private static class SilentRenderer extends SceneRenderer {
        SilentRenderer() {
            super(new StackPane(), null);
        }

        @Override
        public void renderFrame(SceneFrame frame, SceneFrame previousFrame, boolean animate) {
            // рендер в тестах не нужен
        }

        @Override
        public void stopAllEffects() {
            // эффектов нет
        }
    }

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        SceneController.resetGameData();
        SceneInfo.clearChoices();
        engine = new GameEngine(new SilentRenderer(), buildScenes(3));
    }

    /**
     * Сценарий из одной сцены id=1 с заданным числом кадров.
     *
     * @param frameCount количество кадров
     * @return карта сцен
     */
    private Map<Integer, List<SceneFrame>> buildScenes(int frameCount) {
        List<SceneFrame> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(new SceneFrame("Liza", "Кадр " + i, new VisualState()));
        }
        Map<Integer, List<SceneFrame>> scenes = new HashMap<>();
        scenes.put(1, frames);
        return scenes;
    }

    @Test
    void saveLoadRoundtripRestoresFullState() {
        engine.startNewGame(1);
        engine.setPlayerVariable("playerName", "Тест");
        SceneController.setFlag("met_liza", true);
        SceneController.setCharacterReputation("Liza", 80);
        SceneController.setPlayerChoice("evidence_1", "нож");
        SceneInfo.addChoice("Довериться");
        engine.next();

        GameData save = engine.getSaveData();

        // «Перезапуск игры»: чистое глобальное состояние и новый движок
        SceneController.resetGameData();
        SceneInfo.clearChoices();
        GameEngine loaded = new GameEngine(new SilentRenderer(), buildScenes(3));
        loaded.restoreState(save);

        assertEquals(1, loaded.getCurrentSceneId());
        assertEquals(1, loaded.getCurrentFrameIndex());
        assertEquals("Тест", loaded.getPlayerVariable("playerName"));
        assertTrue(SceneController.getFlag("met_liza"));
        assertEquals(80, SceneController.getCharacterReputation("Liza"));
        assertEquals("нож", SceneController.getPlayerChoice("evidence_1"));
        assertTrue(SceneInfo.containsChoice("Довериться"));
    }

    @Test
    void legacySaveWithoutNewFieldsLoads() {
        // Сейв старого формата: новые поля десериализуются как null
        GameData legacy = new GameData();
        legacy.setCurrentSceneId(1);
        legacy.setCurrentFrameIndex(1);
        legacy.setHistory(null);
        legacy.setGameFlags(null);
        legacy.setCharacterReputation(null);
        legacy.setPlayerChoices(null);

        assertDoesNotThrow(() -> engine.restoreState(legacy));
        assertEquals(1, engine.getCurrentSceneId());
        assertEquals(1, engine.getCurrentFrameIndex());
        assertFalse(engine.isFinished());
    }

    @Test
    void saveFromTrimmedScenarioIsClamped() {
        engine.startNewGame(1);
        engine.next();
        engine.next(); // кадр 2 — последний из трёх
        GameData save = engine.getSaveData();
        assertEquals(2, save.getCurrentFrameIndex());

        // Сценарий урезали до одного кадра
        GameEngine trimmed = new GameEngine(new SilentRenderer(), buildScenes(1));
        trimmed.restoreState(save);

        assertEquals(0, trimmed.getCurrentFrameIndex());
        assertDoesNotThrow(trimmed::next);
    }

    @Test
    void saveReferencingMissingSceneDoesNotCrash() {
        engine.startNewGame(1);
        GameData save = engine.getSaveData();
        save.setCurrentSceneId(999);

        GameEngine loaded = new GameEngine(new SilentRenderer(), buildScenes(3));
        assertDoesNotThrow(() -> loaded.restoreState(save));
        assertTrue(loaded.isFinished());
        assertDoesNotThrow(loaded::next);
    }

    @Test
    void backThenNextAfterScenarioTrimDoesNotCrash() {
        // Регрессия: back оставлял битый frameIndex, следующий next падал с IOOBE
        engine.startNewGame(1);
        engine.next();
        engine.next();
        GameData save = engine.getSaveData();

        GameEngine trimmed = new GameEngine(new SilentRenderer(), buildScenes(2));
        trimmed.restoreState(save);

        assertDoesNotThrow(() -> {
            trimmed.back();
            trimmed.next();
            trimmed.next();
            trimmed.next();
        });
    }
}
