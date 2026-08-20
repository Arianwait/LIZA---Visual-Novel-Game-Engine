package kz.aws.game.engine;

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
import kz.aws.game.engine.model.CharacterState;
import kz.aws.game.engine.model.CharacterState.Position;
import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.render.SceneRenderer;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Интеграционные тесты игрового цикла без JavaFX-тулкита (стаб рендерера):
 * выбор сохраняет прогресс, «назад» через границу сцен возвращает визуал,
 * новая игра полностью сбрасывает состояние.
 */
class GameEngineFlowTest {

    /** Стаб рендерера: записывает display-кадры, не запускает JavaFX-анимации. */
    private static class RecordingRenderer extends SceneRenderer {
        final List<SceneFrame> rendered = new ArrayList<>();

        RecordingRenderer() {
            super(new StackPane(), null);
        }

        @Override
        public void renderFrame(SceneFrame frame, SceneFrame previousFrame, boolean animate) {
            rendered.add(frame);
        }

        @Override
        public void stopAllEffects() {
            // в тестах эффектов нет
        }

        SceneFrame last() {
            return rendered.get(rendered.size() - 1);
        }
    }

    private RecordingRenderer renderer;
    private GameEngine engine;
    private SceneFrame scene2Frame0;

    @BeforeEach
    void setUp() {
        renderer = new RecordingRenderer();
        engine = new GameEngine(renderer, buildScenes());
    }

    /**
     * Сцена 1 (1 кадр, Liza на сцене, nextSceneId=2) → сцена 2 (2 кадра, без персонажей).
     */
    private Map<Integer, List<SceneFrame>> buildScenes() {
        VisualState withLiza = new VisualState();
        withLiza.updateCharacter("Liza",
                new CharacterState("Liza", "smile", Position.LEFT, true));
        SceneFrame s1f0 = new SceneFrame("Liza", "Привет", withLiza);
        s1f0.setNextSceneId(2);

        scene2Frame0 = new SceneFrame("Miron", "Сцена 2", new VisualState());
        SceneFrame s2f1 = new SceneFrame("Miron", "Кадр 2", new VisualState());

        Map<Integer, List<SceneFrame>> scenes = new HashMap<>();
        scenes.put(1, List.of(s1f0));
        scenes.put(2, List.of(scene2Frame0, s2f1));
        return scenes;
    }

    @Test
    void chooseKeepsProgressAndUnblocksDialog() {
        engine.startNewGame(1);
        engine.setPlayerVariable("playerName", "Test");
        SceneController.setFlag("met_liza", true);

        engine.choose(new ChoiceOption("Пойти дальше", 2, "", "", "", 0, 100));

        assertEquals(2, engine.getCurrentSceneId());
        assertEquals("Test", engine.getPlayerVariable("playerName"));
        assertTrue(SceneController.getFlag("met_liza"));
        assertTrue(SceneInfo.containsChoice("Пойти дальше"));

        engine.next(); // waitingForChoice сброшен — диалог не заблокирован
        assertEquals("Кадр 2", renderer.last().getText());
    }

    @Test
    void backAcrossSceneBoundaryRestoresCharacters() {
        engine.startNewGame(1);
        engine.next(); // конец сцены 1 → jumpTo(2)

        assertEquals(2, engine.getCurrentSceneId());
        // script-кадр сцены 2 не тронут наследованием...
        assertTrue(scene2Frame0.getVisualState().getCharacters().isEmpty());
        // ...а display-кадр унаследовал персонажа
        assertTrue(renderer.last().getVisualState().getCharacters().get("Liza").isVisible());

        engine.back();

        assertEquals(1, engine.getCurrentSceneId());
        assertTrue(renderer.last().getVisualState().getCharacters().get("Liza").isVisible());
    }

    @Test
    void startNewGameClearsEngineAndGlobalState() {
        engine.startNewGame(1);
        engine.setPlayerVariable("playerName", "Test");
        SceneController.setFlag("met_liza", true);
        SceneInfo.addChoice("Выбор");

        engine.startNewGame(1);

        assertEquals("", engine.getPlayerVariable("playerName"));
        assertFalse(SceneController.getFlag("met_liza"));
        assertFalse(SceneInfo.containsChoice("Выбор"));
    }
}
