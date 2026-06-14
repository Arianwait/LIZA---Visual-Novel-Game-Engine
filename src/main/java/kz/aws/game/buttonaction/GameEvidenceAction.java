package kz.aws.game.buttonaction;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.BaseGamePanel;
import kz.aws.game.panel.PanelContext;
import kz.aws.game.panel.PuzzleRegistry;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Действие кнопки «Улики»: открывает панель улик поверх сцены.
 * Панель модальная — блокирует клики на время просмотра.
 */
@ButtonAction("game-btn-evidence")
public final class GameEvidenceAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        StackPane root = appSettings.getRoot();
        if (root == null) return;

        BaseGamePanel panel = PuzzleRegistry.create("evidence_board");
        if (panel == null) return;

        panel.init(new PanelContext(appSettings));

        SceneInfo.disableEventHandler(root);

        panel.setOnComplete(result -> {
            root.getChildren().remove(panel);
            SceneInfo.enableEventHandler(root);
        });

        root.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);
        panel.toFront();
    }
}
