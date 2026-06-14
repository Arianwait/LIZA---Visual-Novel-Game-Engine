package kz.aws.game.buttonaction;

import kz.aws.game.actionscenarios.ShowMainMenu;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenedetails.ConfirmDialogController;
import kz.aws.game.scenelist.DialogList;
import kz.aws.game.scenelist.SceneInfo;

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
