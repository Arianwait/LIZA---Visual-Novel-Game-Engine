package kz.aws.game.panel.puzzle;

import kz.aws.game.panel.PanelResult;

/**
 * @deprecated Используй {@link PanelResult}.
 */
@Deprecated
public final class PuzzleResult {

    @Deprecated public static PanelResult success()                        { return PanelResult.success(); }
    @Deprecated public static PanelResult failure()                        { return PanelResult.failure(); }
    @Deprecated public static PanelResult withData(boolean ok, String data){ return PanelResult.withData(ok, data); }
}
