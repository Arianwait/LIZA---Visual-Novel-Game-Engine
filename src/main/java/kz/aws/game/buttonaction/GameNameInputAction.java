package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Действие кнопки «Ввести имя»: открывает диалог ввода имени игрока.
 */
@ButtonAction("game-btn-name-input")
public final class GameNameInputAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        var engine = SceneInfo.getGameEngine();
        if (engine != null) {
            engine.showInputDialogFor("playerName", "Введите имя:");
        }
    }
}
