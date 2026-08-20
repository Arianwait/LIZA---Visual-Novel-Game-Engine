package kz.aws.game.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.layout.StackPane;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.StateCommand;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.render.SceneRenderer;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Проверяет выполнение команд состояния из сценария (SetFlag, репутация).
 * До подключения к новому движку эти команды молча игнорировались.
 */
class GameEngineStateCommandTest {

    /** Стаб рендерера без JavaFX-анимаций. */
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

    private SceneFrame frame;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        SceneController.resetGameData();
        SceneInfo.clearChoices();
        frame = new SceneFrame("Маркус", "Реплика", new VisualState());
        Map<Integer, List<SceneFrame>> scenes = new HashMap<>();
        scenes.put(1, List.of(frame));
        engine = new GameEngine(new SilentRenderer(), scenes);
    }

    @Test
    void setFlagCommandIsApplied() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.SET_FLAG, "knows_fact_1", "true"));
        engine.startNewGame(1);
        assertTrue(SceneController.getFlag("knows_fact_1"));
    }

    @Test
    void setFlagFalseIsApplied() {
        SceneController.setFlag("knows_fact_1", true);
        frame.addAnimation(new StateCommand(StateCommand.Kind.SET_FLAG, "knows_fact_1", "false"));
        engine.startNewGame(1);
        assertFalse(SceneController.getFlag("knows_fact_1"));
    }

    @Test
    void reduceReputationSubtractsFromCurrent() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.REDUCE_REPUTATION, "Маркус", "10"));
        engine.startNewGame(1);
        assertEquals(40, SceneController.getCharacterReputation("Маркус"));
    }

    @Test
    void appendReputationAddsToCurrent() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.ADD_REPUTATION, "Маркус", "15"));
        engine.startNewGame(1);
        assertEquals(65, SceneController.getCharacterReputation("Маркус"));
    }

    @Test
    void setReputationOverwritesValue() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.SET_REPUTATION, "Маркус", "80"));
        engine.startNewGame(1);
        assertEquals(80, SceneController.getCharacterReputation("Маркус"));
    }

    @Test
    void reputationIsClampedToRange() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.REDUCE_REPUTATION, "Маркус", "999"));
        engine.startNewGame(1);
        assertEquals(0, SceneController.getCharacterReputation("Маркус"));
    }

    @Test
    void invalidNumberIsIgnored() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.REDUCE_REPUTATION, "Маркус", "много"));
        engine.startNewGame(1);
        assertEquals(50, SceneController.getCharacterReputation("Маркус"));
    }

    @Test
    void emptyTargetIsIgnored() {
        frame.addAnimation(new StateCommand(StateCommand.Kind.SET_FLAG, "", "true"));
        engine.startNewGame(1);
        assertTrue(SceneController.getFlagsSnapshot().isEmpty());
    }
}
