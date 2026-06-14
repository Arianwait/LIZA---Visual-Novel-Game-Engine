package kz.aws.game.gameaction;

import kz.aws.game.scenelist.SceneInfo;

/**
 * Команда скрытия текстовой таблицы (панели диалога).
 */
@GameAction("closeTextTable")
public final class CloseTextTableGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        if (ctx.getRoot() != null && SceneInfo.getTableDatail() != null) {
            SceneInfo.getTableDatail().removeFromScene(ctx.getRoot());
        }
    }
}
