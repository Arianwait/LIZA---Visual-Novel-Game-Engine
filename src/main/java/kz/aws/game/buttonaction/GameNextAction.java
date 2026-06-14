package kz.aws.game.buttonaction;

import kz.aws.game.actionscenarios.ShowMainMenu;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.DialogList;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Действие кнопки «Далее» в игровой панели: следующий кадр или возврат в главное меню по окончании.
 */
@ButtonAction("game-btn-next")
public final class GameNextAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        var engine = SceneInfo.getGameEngine();
        if (engine == null) return;
        if (engine.isFinished()) {
            DialogList.clearDialogs();
            SceneInfo.clearChoices();
            engine.cleanup();
            SceneInfo.setGameEngine(null);
            ShowMainMenu.initializeMainMenuScene(appSettings);
        } else {
            engine.next();
            if (engine.isFinished()) {
                DialogList.clearDialogs();
                SceneInfo.clearChoices();
                engine.cleanup();
                SceneInfo.setGameEngine(null);
                ShowMainMenu.initializeMainMenuScene(appSettings);
            }
        }
    }
}
