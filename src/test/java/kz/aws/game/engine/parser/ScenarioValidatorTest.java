package kz.aws.game.engine.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.engine.model.PuzzleCommand;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.VisualState;

/**
 * Проверяет, что валидатор находит битые переходы сценария до запуска игры.
 */
class ScenarioValidatorTest {

    /**
     * Создаёт сцену из одного кадра.
     *
     * @param frame кадр сцены
     * @return карта сцен с единственной сценой id=1
     */
    private Map<Integer, List<SceneFrame>> sceneWith(SceneFrame frame) {
        Map<Integer, List<SceneFrame>> scenes = new HashMap<>();
        scenes.put(1, List.of(frame));
        return scenes;
    }

    private SceneFrame frame() {
        return new SceneFrame("Liza", "Текст", new VisualState());
    }

    @Test
    void emptyScenarioIsReported() {
        List<String> problems = ScenarioValidator.validate(new HashMap<>());
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("пуст"));
    }

    @Test
    void validScenarioHasNoProblems() {
        SceneFrame f = frame();
        f.setNextSceneId(1);
        assertTrue(ScenarioValidator.validate(sceneWith(f)).isEmpty());
    }

    @Test
    void danglingNextSceneIsReported() {
        SceneFrame f = frame();
        f.setNextSceneId(99);
        List<String> problems = ScenarioValidator.validate(sceneWith(f));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("nextScene=99"));
    }

    @Test
    void endOfGameIsNotAProblem() {
        SceneFrame f = frame(); // nextSceneId = -1 по умолчанию
        assertTrue(ScenarioValidator.validate(sceneWith(f)).isEmpty());
    }

    @Test
    void choiceToMissingSceneIsReported() {
        SceneFrame f = frame();
        f.setChoices(List.of(new ChoiceOption("Уйти", 42, "", "", "", 0, 100)));
        List<String> problems = ScenarioValidator.validate(sceneWith(f));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("42"));
    }

    @Test
    void unreachableReputationRangeIsReported() {
        SceneFrame f = frame();
        f.setChoices(List.of(new ChoiceOption("Довериться", 1, "", "", "Liza", 80, 20)));
        List<String> problems = ScenarioValidator.validate(sceneWith(f));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("недостижим"));
    }

    @Test
    void puzzleTransitionsAreChecked() {
        SceneFrame f = frame();
        f.addAnimation(new PuzzleCommand("code", "flag", 77, 1));
        List<String> problems = ScenarioValidator.validate(sceneWith(f));
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("onSuccess=77"));
    }

    @Test
    void emptySceneIsReported() {
        Map<Integer, List<SceneFrame>> scenes = new HashMap<>();
        scenes.put(5, List.of());
        List<String> problems = ScenarioValidator.validate(scenes);
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("нет ни одного кадра"));
    }
}
