package kz.aws.game.panel;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.utils.VirtualViewport;

/**
 * Контекст выполнения игровой панели.
 * Передаётся в {@link BaseGamePanel#init(PanelContext)} и доступен
 * внутри панели через {@link BaseGamePanel#getContext()}.
 * Размеры окна — это дизайн-разрешение {@link VirtualViewport}:
 * панели верстаются в дизайн-пикселях и масштабируются вместе со сценой.
 */
public class PanelContext {

    private final AppSettings appSettings;

    public PanelContext(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public AppSettings getAppSettings() { return appSettings; }

    public int getWindowWidth()  { return (int) VirtualViewport.DESIGN_WIDTH; }
    public int getWindowHeight() { return (int) VirtualViewport.DESIGN_HEIGHT; }
}
