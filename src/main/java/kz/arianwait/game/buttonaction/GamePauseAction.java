package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.scenedetails.GamePauseMenuController;

/**
 * Открывает overlay-меню паузы поверх игровой сцены.
 */
@ButtonAction("game-btn-pause")
public final class GamePauseAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        GamePauseMenuController pauseMenu = new GamePauseMenuController(appSettings);
        pauseMenu.show(appSettings.getRoot());
    }
}
