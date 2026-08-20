package kz.aws.game.engine.model;

/**
 * Команда запуска загадки, прикреплённая к фрейму диалога.
 *
 * <p>Создаётся {@code SceneXmlParser} при разборе:
 * <pre>
 *   &lt;command type="puzzle"
 *            id="tic_tac_toe"
 *            flag="won_ttt"
 *            onSuccess="5"
 *            onFailure="6"/&gt;
 * </pre>
 *
 * <ul>
 *   <li>{@code id}        — id панели из {@link kz.aws.game.panel.GamePanel#id()}</li>
 *   <li>{@code flag}      — имя флага, куда пишется true/false по итогу (опционально)</li>
 *   <li>{@code onSuccess} — номер сцены при победе (опционально, -1 = нет)</li>
 *   <li>{@code onFailure} — номер сцены при проигрыше (опционально, -1 = нет)</li>
 * </ul>
 */
public class PuzzleCommand implements AnimationCommand {

    private static final long serialVersionUID = 1L;

    private final String puzzleId;
    private final String flag;
    private final int onSuccess;
    private final int onFailure;

    public PuzzleCommand(String puzzleId, String flag, int onSuccess, int onFailure) {
        this.puzzleId  = puzzleId  != null ? puzzleId  : "";
        this.flag      = flag      != null ? flag      : "";
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    @Override
    public String getType() {
        return "puzzle";
    }

    public String getPuzzleId()  { return puzzleId; }
    public String getFlag()      { return flag; }
    public int    getOnSuccess() { return onSuccess; }
    public int    getOnFailure() { return onFailure; }
}
