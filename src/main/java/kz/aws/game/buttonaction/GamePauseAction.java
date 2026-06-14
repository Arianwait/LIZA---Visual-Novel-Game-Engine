package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenedetails.GamePauseMenuController;

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
