package kz.aws.game.engine.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kz.aws.game.engine.model.AnimationCommand;
import kz.aws.game.engine.model.CharacterState;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.StateCommand;

/**
 * Проверяет разбор сценария: устойчивость к битым id и дублям,
 * наследование состояния внутри сцены и команды состояния.
 */
class SceneXmlParserTest {

    @TempDir
    Path tempDir;

    /**
     * Записывает сценарий во временный файл и разбирает его.
     *
     * @param xml содержимое файла сценария
     * @return разобранные сцены
     * @throws IOException если файл не удалось создать
     */
    private Map<Integer, List<SceneFrame>> parse(String xml) throws IOException {
        Path file = tempDir.resolve("Dialog_Structured.xml");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        return SceneXmlParser.parseScenesFrom(file.toString());
    }

    @Test
    void missingFileGivesEmptyResult() {
        Map<Integer, List<SceneFrame>> scenes =
                SceneXmlParser.parseScenesFrom(tempDir.resolve("нет.xml").toString());
        assertTrue(scenes.isEmpty());
    }

    @Test
    void malformedXmlGivesEmptyResult() throws IOException {
        assertTrue(parse("<dialogs><dialog id=\"1\"></dialogs>").isEmpty());
    }

    @Test
    void brokenSceneIdIsSkippedButOthersLoad() throws IOException {
        // регрессия: один битый id обрушивал весь сценарий
        String xml = """
                <dialogs>
                  <dialog id="1"><character name="Liza">Первая</character></dialog>
                  <dialog id="5a"><character name="Liza">Битая</character></dialog>
                  <dialog id="2"><character name="Liza">Вторая</character></dialog>
                </dialogs>
                """;
        Map<Integer, List<SceneFrame>> scenes = parse(xml);

        assertEquals(2, scenes.size());
        assertTrue(scenes.containsKey(1));
        assertTrue(scenes.containsKey(2));
    }

    @Test
    void duplicateSceneIdKeepsFirst() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1"><character name="Liza">Первая</character></dialog>
                  <dialog id="1"><character name="Liza">Дубль</character></dialog>
                </dialogs>
                """;
        Map<Integer, List<SceneFrame>> scenes = parse(xml);

        assertEquals(1, scenes.size());
        assertEquals("Первая", scenes.get(1).get(0).getText().trim());
    }

    @Test
    void sceneWithoutIdIsSkipped() throws IOException {
        String xml = """
                <dialogs>
                  <dialog><character name="Liza">Без id</character></dialog>
                  <dialog id="3"><character name="Liza">С id</character></dialog>
                </dialogs>
                """;
        assertEquals(1, parse(xml).size());
    }

    @Test
    void backgroundAndNextSceneAreParsed() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1" background="bg/room.png" nextScene="2">
                    <character name="Liza">Реплика</character>
                  </dialog>
                  <dialog id="2"><character name="Liza">Вторая</character></dialog>
                </dialogs>
                """;
        SceneFrame frame = parse(xml).get(1).get(0);

        assertEquals("bg/room.png", frame.getVisualState().getBackgroundPath());
        assertEquals(2, frame.getNextSceneId());
    }

    @Test
    void characterStatePersistsAcrossFramesWithinScene() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Liza">
                      <command type="character" action="showPerson" target="Liza" value="smile"/>
                      Появилась
                    </character>
                    <character name="Liza">Вторая реплика</character>
                  </dialog>
                </dialogs>
                """;
        List<SceneFrame> frames = parse(xml).get(1);

        CharacterState second = frames.get(1).getVisualState().getCharacters().get("Liza");
        assertTrue(second.isVisible(), "персонаж остаётся видимым в следующем кадре сцены");
        assertEquals("smile", second.getPose());
    }

    @Test
    void removeFromSceneHidesCharacter() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Liza">
                      <command type="character" action="showPerson" target="Liza" value="smile"/>
                      Появилась
                    </character>
                    <character name="Liza">
                      <command type="character" action="removeFromScene" target="Liza"/>
                      Ушла
                    </character>
                  </dialog>
                </dialogs>
                """;
        List<SceneFrame> frames = parse(xml).get(1);

        assertFalse(frames.get(1).getVisualState().getCharacters().get("Liza").isVisible());
    }

    @Test
    void stateCommandsAreParsed() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Маркус">
                      <command type="character" action="SetFlag" target="knows_fact" value="true"/>
                      <command type="character" action="ReduceReputathion" target="Маркус" value="10"/>
                      Реплика
                    </character>
                  </dialog>
                </dialogs>
                """;
        List<AnimationCommand> commands = parse(xml).get(1).get(0).getEntryAnimations();

        long stateCommands = commands.stream().filter(c -> c instanceof StateCommand).count();
        assertEquals(2, stateCommands, "обе команды состояния должны быть разобраны");
    }

    @Test
    void choicesAreParsedWithConditions() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Liza">
                      <choice>
                        <option text="Довериться" requestsID="2" addChoice="met"
                                CharacterChoice="Liza" minRep="60" maxRep="100"/>
                      </choice>
                      Реплика
                    </character>
                  </dialog>
                  <dialog id="2"><character name="Liza">Вторая</character></dialog>
                </dialogs>
                """;
        SceneFrame frame = parse(xml).get(1).get(0);

        assertTrue(frame.hasChoices());
        assertEquals("Довериться", frame.getChoices().get(0).getText());
        assertEquals(2, frame.getChoices().get(0).getTargetSceneId());
        assertEquals(60, frame.getChoices().get(0).getMinRep());
        assertEquals("Liza", frame.getChoices().get(0).getCharacterChoice());
    }
}
