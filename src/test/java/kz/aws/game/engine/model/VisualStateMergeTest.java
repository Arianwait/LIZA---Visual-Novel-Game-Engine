package kz.aws.game.engine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import kz.aws.game.engine.model.CharacterState.Position;

/**
 * Контракт runtime-визуала: {@link VisualState#merge} наследует состояние
 * на границе сцен, не мутируя script-слой (кадры из XML).
 */
class VisualStateMergeTest {

    @Test
    void mergeWithNullScriptAndPreviousGivesEmptyState() {
        VisualState out = VisualState.merge(null, null);
        assertNull(out.getBackgroundPath());
        assertNull(out.getMusicPath());
        assertTrue(out.getCharacters().isEmpty());
    }

    @Test
    void mergeInheritsBackgroundAndMusicWhenScriptBlank() {
        VisualState previous = new VisualState();
        previous.setBackgroundPath("bg/street.png");
        previous.setMusicPath("music/theme.mp3");

        VisualState out = VisualState.merge(new VisualState(), previous);

        assertEquals("bg/street.png", out.getBackgroundPath());
        assertEquals("music/theme.mp3", out.getMusicPath());
    }

    @Test
    void mergePrefersScriptBackgroundAndMusic() {
        VisualState script = new VisualState();
        script.setBackgroundPath("bg/room.png");
        script.setMusicPath("music/tense.mp3");
        VisualState previous = new VisualState();
        previous.setBackgroundPath("bg/street.png");
        previous.setMusicPath("music/theme.mp3");

        VisualState out = VisualState.merge(script, previous);

        assertEquals("bg/room.png", out.getBackgroundPath());
        assertEquals("music/tense.mp3", out.getMusicPath());
    }

    @Test
    void mergeCarriesVisibleCharacterMissingInScript() {
        VisualState previous = new VisualState();
        previous.updateCharacter("Liza",
                new CharacterState("Liza", "smile", Position.LEFT, true));

        VisualState out = VisualState.merge(new VisualState(), previous);

        assertTrue(out.getCharacters().containsKey("Liza"));
        assertTrue(out.getCharacters().get("Liza").isVisible());
    }

    @Test
    void mergeDoesNotCarryInvisibleCharacter() {
        VisualState previous = new VisualState();
        previous.updateCharacter("Liza",
                new CharacterState("Liza", "smile", Position.LEFT, false));

        VisualState out = VisualState.merge(new VisualState(), previous);

        assertFalse(out.getCharacters().containsKey("Liza"));
    }

    @Test
    void mergeKeepsScriptRemoveFromScene() {
        // removeFromScene в script ставит visible=false — script побеждает
        VisualState script = new VisualState();
        script.updateCharacter("Liza",
                new CharacterState("Liza", "default", Position.OUT, false));
        VisualState previous = new VisualState();
        previous.updateCharacter("Liza",
                new CharacterState("Liza", "smile", Position.LEFT, true));

        VisualState out = VisualState.merge(script, previous);

        assertFalse(out.getCharacters().get("Liza").isVisible());
    }

    @Test
    void mergeDoesNotMutateScriptOrPrevious() {
        VisualState script = new VisualState();
        VisualState previous = new VisualState();
        previous.setBackgroundPath("bg/street.png");
        previous.updateCharacter("Liza",
                new CharacterState("Liza", "smile", Position.LEFT, true));
        VisualState scriptBefore = script.clone();
        VisualState previousBefore = previous.clone();

        VisualState out = VisualState.merge(script, previous);
        out.setBackgroundPath("bg/changed.png");
        out.getCharacters().get("Liza").setPose("angry");

        assertEquals(scriptBefore, script);
        assertEquals(previousBefore, previous);
    }

    @Test
    void repeatedEntryIntoSameSceneDoesNotAccumulateInScript() {
        // Два захода в одну сцену с разными предыдущими состояниями:
        // script-кадр обязан остаться нетронутым (главный инвариант рефакторинга)
        VisualState script = new VisualState();
        VisualState scriptBefore = script.clone();

        VisualState fromSceneA = new VisualState();
        fromSceneA.updateCharacter("Liza",
                new CharacterState("Liza", "smile", Position.LEFT, true));
        VisualState fromSceneB = new VisualState();
        fromSceneB.updateCharacter("Miron",
                new CharacterState("Miron", "default", Position.RIGHT, true));

        VisualState outA = VisualState.merge(script, fromSceneA);
        VisualState outB = VisualState.merge(script, fromSceneB);

        assertEquals(scriptBefore, script);
        assertTrue(outA.getCharacters().containsKey("Liza"));
        assertFalse(outA.getCharacters().containsKey("Miron"));
        assertTrue(outB.getCharacters().containsKey("Miron"));
        assertFalse(outB.getCharacters().containsKey("Liza"));
    }

    @Test
    void mergeIsIdempotentForHistoryRestore(){
        // merge(script, merge(script, prev)) == merge(script, prev) —
        // на этом держится восстановление runtime-визуала из снимка истории
        VisualState script = new VisualState();
        script.setBackgroundPath("bg/room.png");
        script.updateCharacter("Liza",
                new CharacterState("Liza", "default", Position.OUT, false));
        VisualState previous = new VisualState();
        previous.setMusicPath("music/theme.mp3");
        previous.updateCharacter("Miron",
                new CharacterState("Miron", "default", Position.RIGHT, true));

        VisualState once = VisualState.merge(script, previous);
        VisualState twice = VisualState.merge(script, once);

        assertEquals(once, twice);
    }

    @Test
    void withVisualCopiesFrameButSharesScriptLists() {
        VisualState scriptVisual = new VisualState();
        SceneFrame frame = new SceneFrame("Liza", "Привет!", scriptVisual);
        frame.setNextSceneId(7);

        VisualState runtime = new VisualState();
        SceneFrame display = frame.withVisual(runtime);

        assertNotSame(frame, display);
        assertSame(runtime, display.getVisualState());
        assertSame(scriptVisual, frame.getVisualState());
        assertEquals("Liza", display.getSpeakerName());
        assertEquals("Привет!", display.getText());
        assertEquals(7, display.getNextSceneId());
        assertSame(frame.getEntryAnimations(), display.getEntryAnimations());
    }
}
