package kz.arianwait.game.buttonaction;

import kz.arianwait.game.actionscenarios.ShowMainMenu;
import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.scenedetails.ConfirmDialogController;
import kz.arianwait.game.scenelist.DialogList;
import kz.arianwait.game.scenelist.SceneInfo;

/**
 * Действие кнопки «Главное меню» в игровой панели: диалог подтверждения, затем возврат в главное меню.
 */
@ButtonAction("game-btn-menu")
public final class GameMenuAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        ConfirmDialogController confirmDialog = new ConfirmDialogController(appSettings);
        confirmDialog.show(
            "Вы уверены, что хотите выйти, все не сохраненые данные удалятся?",
            null,
            () -> {
                DialogList.clearDialogs();
                SceneInfo.clearChoices();
                if (SceneInfo.getGameEngine() != null) {
                    SceneInfo.getGameEngine().cleanup();
                    SceneInfo.setGameEngine(null);
                }
                ShowMainMenu.initializeMainMenuScene(appSettings);
            },
            () -> { }
        );
    }
}
