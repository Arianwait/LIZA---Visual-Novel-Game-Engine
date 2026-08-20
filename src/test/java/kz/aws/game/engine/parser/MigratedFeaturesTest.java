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
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.SoundCommand;
import kz.aws.game.engine.model.StateCommand;

/**
 * Проверяет фичи, перенесённые из удалённого пакета gameaction:
 * звуковые эффекты кадра, запуск титров и запись выбора игрока —
 * в новом синтаксисе и в legacy-командах старого Dialog.xml.
 */
class MigratedFeaturesTest {

    @TempDir
    Path tempDir;

    /**
     * Записывает сценарий во временный файл и разбирает его.
     *
     * @param xml содержимое сценария
     * @return разобранные сцены
     * @throws IOException если файл не удалось создать
     */
    private Map<Integer, List<SceneFrame>> parse(String xml) throws IOException {
        Path file = tempDir.resolve("Dialog_Structured.xml");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        return SceneXmlParser.parseScenesFrom(file.toString());
    }

    /**
     * Оборачивает содержимое персонажа в минимальный сценарий из одной сцены.
     *
     * @param characterBody внутренности элемента character
     * @return XML сценария
     */
    private String scenarioWith(String characterBody) {
        return """
                <dialogs>
                  <dialog id="1">
                    <character name="Liza">%s</character>
                  </dialog>
                </dialogs>
                """.formatted(characterBody);
    }

    @Test
    void soundAttributeIsParsed() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Liza" sound="lib/sound/door.mp3">Скрип двери</character>
                  </dialog>
                </dialogs>
                """;
        assertEquals("lib/sound/door.mp3", parse(xml).get(1).get(0).getSoundPath());
    }

    @Test
    void blankSoundAttributeLeavesNull() throws IOException {
        String xml = """
                <dialogs>
                  <dialog id="1">
                    <character name="Liza" sound="  ">Реплика</character>
                  </dialog>
                </dialogs>
                """;
        org.junit.jupiter.api.Assertions.assertNull(parse(xml).get(1).get(0).getSoundPath());
    }

    @Test
    void legacySoundEffectCommandIsParsed() throws IOException {
        List<AnimationCommand> commands =
                parse(scenarioWith("<command>Scene:SoundEffect:lib/sound/shot.mp3</command>Выстрел"))
                        .get(1).get(0).getEntryAnimations();

        SoundCommand sound = commands.stream()
                .filter(c -> c instanceof SoundCommand)
                .map(c -> (SoundCommand) c)
                .findFirst()
                .orElseThrow();
        assertEquals("lib/sound/shot.mp3", sound.getSoundPath());
    }

    @Test
    void titresCommandSetsFrameFlag() throws IOException {
        SceneFrame frame = parse(scenarioWith(
                "<command type=\"character\" action=\"startTitles\"/>Конец")).get(1).get(0);
        assertTrue(frame.isStartTitles());
    }

    @Test
    void legacyTitresCommandNameIsSupported() throws IOException {
        // историческое написание из старых сценариев
        SceneFrame frame = parse(scenarioWith(
                "<command type=\"character\" action=\"StartTitries\"/>Конец")).get(1).get(0);
        assertTrue(frame.isStartTitles());
    }

    @Test
    void ordinaryFrameDoesNotStartTitres() throws IOException {
        assertFalse(parse(scenarioWith("Обычная реплика")).get(1).get(0).isStartTitles());
    }

    @Test
    void setChoiceCommandIsParsed() throws IOException {
        List<AnimationCommand> commands = parse(scenarioWith(
                "<command type=\"character\" action=\"SetChoice\" target=\"evidence_1\" value=\"нож\"/>Улика"))
                .get(1).get(0).getEntryAnimations();

        StateCommand state = commands.stream()
                .filter(c -> c instanceof StateCommand)
                .map(c -> (StateCommand) c)
                .findFirst()
                .orElseThrow();
        assertEquals(StateCommand.Kind.SET_CHOICE, state.getKind());
        assertEquals("evidence_1", state.getTarget());
        assertEquals("нож", state.getValue());
    }

    @Test
    void legacySetChoiceCommandIsParsed() throws IOException {
        // формат из старого Dialog.xml: System:SetChoice:ключ:значение
        List<AnimationCommand> commands = parse(scenarioWith(
                "<command>System:SetChoice:qualities_positive:honesty</command>Реплика"))
                .get(1).get(0).getEntryAnimations();

        StateCommand state = commands.stream()
                .filter(c -> c instanceof StateCommand)
                .map(c -> (StateCommand) c)
                .findFirst()
                .orElseThrow();
        assertEquals(StateCommand.Kind.SET_CHOICE, state.getKind());
        assertEquals("qualities_positive", state.getTarget());
        assertEquals("honesty", state.getValue());
    }

    @Test
    void legacySetFlagCommandIsParsed() throws IOException {
        List<AnimationCommand> commands = parse(scenarioWith(
                "<command>System:SetFlag:qualities_chosen:true</command>Реплика"))
                .get(1).get(0).getEntryAnimations();

        StateCommand state = commands.stream()
                .filter(c -> c instanceof StateCommand)
                .map(c -> (StateCommand) c)
                .findFirst()
                .orElseThrow();
        assertEquals(StateCommand.Kind.SET_FLAG, state.getKind());
        assertEquals("qualities_chosen", state.getTarget());
        assertEquals("true", state.getValue());
    }

    @Test
    void legacyReputationCommandIsParsed() throws IOException {
        // формат: Персонаж:AppendReputathion:значение (историческое написание)
        List<AnimationCommand> commands = parse(scenarioWith(
                "<command>Liza:AppendReputathion:10</command>Реплика"))
                .get(1).get(0).getEntryAnimations();

        StateCommand state = commands.stream()
                .filter(c -> c instanceof StateCommand)
                .map(c -> (StateCommand) c)
                .findFirst()
                .orElseThrow();
        assertEquals(StateCommand.Kind.ADD_REPUTATION, state.getKind());
        assertEquals("Liza", state.getTarget());
        assertEquals("10", state.getValue());
    }

    @Test
    void withVisualKeepsSoundAndTitres() throws IOException {
        SceneFrame frame = parse(scenarioWith(
                "<command type=\"character\" action=\"startTitles\"/>Конец")).get(1).get(0);
        frame.setSoundPath("lib/sound/door.mp3");

        SceneFrame display = frame.withVisual(frame.getVisualState());

        assertEquals("lib/sound/door.mp3", display.getSoundPath());
        assertTrue(display.isStartTitles());
    }
}
