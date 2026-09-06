package kz.aws.game.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kz.aws.game.utils.UiConfigParser.ButtonConfig;

/**
 * Проверяет фильтрацию кнопок меню по условию visible-when.
 * Регрессия: «Продолжить» показывалась при отсутствии сохранений,
 * потому что меню читало кнопки без учёта условий.
 */
class ButtonVisibilityTest {

    /** Резолвер, считающий, что сохранений нет. */
    private static final Function<String, Boolean> NO_SAVES = c -> !"hasSaveFiles".equals(c);
    /** Резолвер, считающий, что сохранения есть. */
    private static final Function<String, Boolean> HAS_SAVES = c -> true;

    /**
     * Проверяет наличие кнопки с указанным id в списке.
     *
     * @param buttons список кнопок
     * @param id      искомый идентификатор
     * @return true — кнопка присутствует
     */
    private boolean contains(List<ButtonConfig> buttons, String id) {
        return buttons.stream().anyMatch(b -> id.equals(b.id));
    }

    @Test
    void continueIsHiddenWithoutSaves() {
        List<ButtonConfig> buttons =
                UiConfigParser.getButtonsForView("main-menu", null, NO_SAVES);
        assertFalse(contains(buttons, "menu-btn-continue"),
                "без сохранений кнопка «Продолжить» показываться не должна");
    }

    @Test
    void continueIsShownWithSaves() {
        List<ButtonConfig> buttons =
                UiConfigParser.getButtonsForView("main-menu", null, HAS_SAVES);
        assertTrue(contains(buttons, "menu-btn-continue"));
    }

    @Test
    void unconditionalButtonsAlwaysVisible() {
        List<ButtonConfig> buttons =
                UiConfigParser.getButtonsForView("main-menu", null, NO_SAVES);
        assertTrue(contains(buttons, "menu-btn-new-game"),
                "кнопки без visible-when видны всегда");
    }
}
