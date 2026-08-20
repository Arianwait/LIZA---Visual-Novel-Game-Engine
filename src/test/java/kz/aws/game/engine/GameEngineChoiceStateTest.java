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
import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.render.SceneRenderer;
import kz.aws.game.scenelist.SceneController;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Проверяет откат глобального состояния при «Назад»: список выборов,
 * флаги и репутация должны возвращаться к значениям того шага,
 * иначе условия вариантов считаются по «будущему» состоянию.
 */
class GameEngineChoiceStateTest {

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

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        SceneController.resetGameData();
        SceneInfo.clearChoices();
        engine = new GameEngine(new SilentRenderer(), buildScenes());
    }

    /**
     * Сцена 1: три кадра без выборов (панель выбора требует JavaFX-контроллера,
     * поэтому ветвление эмулируется прямыми вызовами API состояния).
     *
     * @return карта сцен
     */
    private Map<Integer, List<SceneFrame>> buildScenes() {
        List<SceneFrame> frames = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            frames.add(new SceneFrame("Liza", "Кадр " + i, new VisualState()));
        }
        Map<Integer, List<SceneFrame>> scenes = new HashMap<>();
        scenes.put(1, frames);
        return scenes;
    }

    @Test
    void backRestoresChoiceList() {
        engine.startNewGame(1);
        assertFalse(SceneInfo.containsChoice("Довериться"));

        SceneInfo.addChoice("Довериться");
        engine.next();
        assertTrue(SceneInfo.containsChoice("Довериться"));

        engine.back();
        assertFalse(SceneInfo.containsChoice("Довериться"),
                "после back список выборов должен вернуться к состоянию развилки");
    }

    @Test
    void backRestoresFlags() {
        engine.startNewGame(1);
        SceneController.setFlag("found_knife", true);
        engine.next();

        engine.back();
        assertFalse(SceneController.getFlag("found_knife"));
    }

    @Test
    void backRestoresReputation() {
        engine.startNewGame(1);
        SceneController.setCharacterReputation("Liza", 90);
        engine.next();

        engine.back();
        assertEquals(50, SceneController.getCharacterReputation("Liza"),
                "репутация должна вернуться к дефолту, каким была на шаге развилки");
    }

    @Test
    void redoAfterBackRestoresForwardState() {
        engine.startNewGame(1);
        SceneController.setFlag("found_knife", true);
        SceneInfo.addChoice("Довериться");
        engine.next();

        engine.back();
        engine.next(); // redo по истории

        assertTrue(SceneController.getFlag("found_knife"));
        assertTrue(SceneInfo.containsChoice("Довериться"));
    }

    @Test
    void reputationConditionRespectsBounds() {
        SceneController.setCharacterReputation("Liza", 50);
        assertTrue(available(option("Liza", 50, 50)), "границы включительно");
        assertTrue(available(option("Liza", 0, 100)));
        assertFalse(available(option("Liza", 51, 100)), "ниже minRep — недоступен");
        assertFalse(available(option("Liza", 0, 49)), "выше maxRep — недоступен");
    }

    @Test
    void reputationConditionUsesDefaultForUnknownCharacter() {
        // дефолт репутации — 50
        assertTrue(available(option("Незнакомец", 50, 50)));
        assertFalse(available(option("Незнакомец", 60, 100)));
    }

    @Test
    void emptyCharacterChoiceSkipsReputationCheck() {
        assertTrue(available(option("", 90, 100)));
    }

    @Test
    void choiceConditionsSupportSemicolonLists() {
        SceneInfo.addChoice("A");
        SceneInfo.addChoice("B");

        assertTrue(available(new ChoiceOption("t", 1, "A;B", "", "", 0, 100)));
        assertFalse(available(new ChoiceOption("t", 1, "A;C", "", "", 0, 100)),
                "требуется отсутствующий выбор C");
        assertFalse(available(new ChoiceOption("t", 1, "", "B", "", 0, 100)),
                "запрещённый выбор B присутствует");
        assertTrue(available(new ChoiceOption("t", 1, "", "C", "", 0, 100)));
    }

    /**
     * Создаёт вариант выбора с условием репутации.
     *
     * @param character имя персонажа
     * @param minRep    минимальная репутация
     * @param maxRep    максимальная репутация
     * @return вариант выбора
     */
    private ChoiceOption option(String character, int minRep, int maxRep) {
        return new ChoiceOption("Вариант", 1, "", "", character, minRep, maxRep);
    }

    /**
     * Проверяет доступность варианта через движок.
     *
     * @param option вариант выбора
     * @return true — вариант доступен
     */
    private boolean available(ChoiceOption option) {
        return engine.isChoiceAvailable(option);
    }
}
