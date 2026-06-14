package kz.aws.game.gameaction;

import kz.aws.game.scenelist.SceneInfo;

/**
 * Команда показа текстовой таблицы (панели диалога).
 */
@GameAction("openTextTable")
public final class OpenTextTableGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        if (ctx.getRoot() != null && SceneInfo.getTableDatail() != null) {
            SceneInfo.getTableDatail().addToScene(ctx.getRoot());
        }
    }
}
