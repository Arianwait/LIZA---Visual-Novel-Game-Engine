package kz.aws.game.gameaction;

import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import kz.aws.game.panel.BaseGamePanel;
import kz.aws.game.panel.GamePanel;
import kz.aws.game.panel.PanelContext;
import kz.aws.game.panel.PuzzleRegistry;
import kz.aws.game.scenedetails.DimOverlay;
import kz.aws.game.scenelist.SceneController;

/**
 * Игровая команда для запуска игровой панели через CommandProcessor (legacy-путь).
 *
 * <p>Рекомендуемый способ — XML-команда {@code type="panel"} (обрабатывается в GameEngine).
 * Этот обработчик остаётся для вызовов из старого формата {@code :openPuzzle:...}.
 *
 * <p>Параметры (через «:»):
 * <pre>
 *   target:openPuzzle:panelId:flagName:sceneOnSuccess:sceneOnFailure
 * </pre>
 */
@GameAction("openPuzzle")
public final class OpenPuzzleGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] params = ctx.getParameters();
        if (params.length < 1 || params[0].isEmpty()) {
            System.err.println("openPuzzle: необходимо указать panelId");
            return;
        }

        String panelId       = params[0];
        String flagName      = params.length > 1 ? params[1] : "";
        int sceneOnSuccess   = params.length > 2 ? parseIntSafe(params[2]) : -1;
        int sceneOnFailure   = params.length > 3 ? parseIntSafe(params[3]) : -1;

        BaseGamePanel panel = PuzzleRegistry.create(panelId);
        if (panel == null) return;

        GamePanel annotation = panel.getClass().getAnnotation(GamePanel.class);
        panel.init(new PanelContext(ctx.getAppSettings()));

        StackPane root = ctx.getRoot();

        DimOverlay dimOverlay = null;
        if (annotation != null && annotation.modal()) {
            dimOverlay = new DimOverlay("", 0.6);
            dimOverlay.showOverlay(root);
        }

        final DimOverlay finalDim = dimOverlay;
        panel.setOnComplete(result -> {
            root.getChildren().remove(panel);
            if (finalDim != null) finalDim.hideOverlay(root);

            if (result.isCloseOnly()) return;

            if (!flagName.isEmpty()) {
                SceneController.setFlag(flagName, result.isSuccess());
                if (!result.getData().isEmpty()) {
                    SceneController.setPlayerChoice(flagName + "_data", result.getData());
                }
            }

            int targetScene = result.isSuccess() ? sceneOnSuccess : sceneOnFailure;
            if (targetScene > 0) {
                Consumer<Integer> navigator = ctx.getAppSettings().getSceneNavigator();
                if (navigator != null) navigator.accept(targetScene);
            }
        });

        kz.aws.game.utils.OverlayMarker.mark(panel);
        root.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);
        panel.toFront();
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
