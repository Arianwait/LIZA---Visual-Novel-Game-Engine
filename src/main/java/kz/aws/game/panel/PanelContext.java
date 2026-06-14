package kz.aws.game.panel;

import kz.aws.game.appsettings.AppSettings;

/**
 * Контекст выполнения игровой панели.
 * Передаётся в {@link BaseGamePanel#init(PanelContext)} и доступен
 * внутри панели через {@link BaseGamePanel#getContext()}.
 */
public class PanelContext {

    private final AppSettings appSettings;

    public PanelContext(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public AppSettings getAppSettings() { return appSettings; }

    public int getWindowWidth()  { return appSettings != null ? appSettings.getWindowWidth()  : 1280; }
    public int getWindowHeight() { return appSettings != null ? appSettings.getWindowHeight() : 720;  }
}
