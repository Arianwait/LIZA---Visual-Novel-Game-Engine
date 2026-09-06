package kz.arianwait.game.engine.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kz.arianwait.game.engine.model.CharacterState;
import kz.arianwait.game.engine.model.CharacterState.Position;
import kz.arianwait.game.engine.model.SceneFrame;

/**
 * Проверяет механику выбегания персонажа за пределы экрана:
 * runToLeft/runToRight уводят за край, setFromLeft/setFromRight
 * ставят за краем для последующего выбегания на сцену.
 */
class OffScreenMovementTest {

    @TempDir
    Path tempDir;

    /**
     * Разбирает сценарий из строки.
     *
     * @param xml содержимое сценария
     * @return разобранные сцены
     * @throws IOException если файл не создан
     */
    private Map<Integer, List<SceneFrame>> parse(String xml) throws IOException {
        Path file = tempDir.resolve("Dialog_Structured.xml");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        return SceneXmlParser.parseScenesFrom(file.toString());
    }

    /**
     * Возвращает состояние персонажа в указанном кадре.
     *
     * @param scenes разобранные сцены
     * @param frame  индекс кадра
     * @param name   имя персонажа
     * @return состояние персонажа
     */
    private CharacterState stateOf(Map<Integer, List<SceneFrame>> scenes, int frame, String name) {
        return scenes.get(1).get(frame).getVisualState().getCharacters().get(name);
    }

    @Test
    void runToLeftMovesCharacterOffScreen() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Elein">
                      <command type="character" action="showPerson" target="Elein" value="Stay"/>
                      Стоит
                    </character>
                    <character name="">
                      <command type="character" action="runToLeft" target="Elein"/>
                      Убегает
                    </character>
                  </dialog>
                </dialogs>
                """;
        CharacterState state = stateOf(parse(xml), 1, "Elein");

        assertEquals(Position.OUT_LEFT, state.getPosition());
        assertTrue(state.isVisible(), "остаётся на сцене, иначе анимация ухода не проиграется");
    }

    @Test
    void runToRightMovesCharacterOffScreen() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Elein">
                      <command type="character" action="showPerson" target="Elein" value="Stay"/>
                      Стоит
                    </character>
                    <character name="">
                      <command type="character" action="runToRight" target="Elein"/>
                      Убегает
                    </character>
                  </dialog>
                </dialogs>
                """;
        assertEquals(Position.OUT_RIGHT, stateOf(parse(xml), 1, "Elein").getPosition());
    }

    @Test
    void legacyRunToCommandIsParsed() throws IOException {
        // старый синтаксис: Персонаж:runToLeft
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Elein">
                      <command>Elein:showPerson:Stay</command>Стоит
                    </character>
                    <character name="">
                      <command>Elein:runToLeft</command>Убегает
                    </character>
                  </dialog>
                </dialogs>
                """;
        CharacterState state = stateOf(parse(xml), 1, "Elein");

        assertEquals(Position.OUT_LEFT, state.getPosition());
        assertTrue(state.isVisible());
    }

    @Test
    void setFromLeftPlacesCharacterBeyondEdge() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Anna">
                      <command>Anna:showPerson:Stay</command>
                      <command>Anna:setFromLeft</command>Появляется слева
                    </character>
                  </dialog>
                </dialogs>
                """;
        assertEquals(Position.OUT_LEFT, stateOf(parse(xml), 0, "Anna").getPosition());
    }

    @Test
    void characterRunsBackOntoScene() throws IOException {
        // выбежал за край, затем вернулся в центр
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Elein">
                      <command>Elein:showPerson:Stay</command>Стоит
                    </character>
                    <character name="">
                      <command>Elein:runToLeft</command>Убежала
                    </character>
                    <character name="Elein">
                      <command>Elein:move_Center</command>Вернулась
                    </character>
                  </dialog>
                </dialogs>
                """;
        Map<Integer, List<SceneFrame>> scenes = parse(xml);

        assertEquals(Position.OUT_LEFT, stateOf(scenes, 1, "Elein").getPosition());
        assertEquals(Position.CENTER, stateOf(scenes, 2, "Elein").getPosition());
        assertTrue(stateOf(scenes, 2, "Elein").isVisible());
    }

    @Test
    void offScreenPositionsAreMarkedAsSuch() {
        assertTrue(Position.OUT_LEFT.isOffScreen());
        assertTrue(Position.OUT_RIGHT.isOffScreen());
        assertTrue(!Position.LEFT.isOffScreen());
        assertTrue(!Position.CENTER.isOffScreen());
    }
}
