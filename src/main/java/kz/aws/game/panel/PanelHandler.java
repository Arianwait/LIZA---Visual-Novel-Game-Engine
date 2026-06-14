package kz.aws.game.panel;

import javafx.scene.Node;
import kz.aws.game.appsettings.AppSettings;

/**
 * Создаёт узел панели по настройкам. Реализации — классы с аннотацией {@link Panel}.
 */
@FunctionalInterface
public interface PanelHandler {
    Node create(AppSettings appSettings);
}
