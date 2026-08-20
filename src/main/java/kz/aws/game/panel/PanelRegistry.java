package kz.aws.game.panel;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Регистр панелей по id. Панели из Panels.xml показываются через {@link #show(String, AppSettings)}.
 * Сканируются только panel и mainscene (MainMenuController, SettingsMenuController, SceneSelection).
 */
public final class PanelRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(PanelRegistry.class);

    private static final String[] SCAN_PACKAGES = { "kz.aws.game.panel", "kz.aws.game.mainscene" };
    private static final Map<String, PanelHandler> HANDLERS = new HashMap<>();
    private static boolean discovered = false;

    public static void register(String id, PanelHandler handler) {
        if (id != null && handler != null) HANDLERS.put(id, handler);
    }

    public static PanelHandler get(String id) {
        ensureDiscovered();
        return HANDLERS.get(id);
    }

    /**
     * Показывает панель по id (по умолчанию при показе подменю отключается действие мыши на root).
     */
    public static void show(String panelId, AppSettings appSettings) {
        show(panelId, appSettings, true);
    }

    /**
     * Создаёт панель по id и подменяет контент главного меню (getMainMenuContentPane).
     * При {@code disableMouseOnShow == true}: для подменю (не main-menu) отключает обработчик мыши на root;
     * при возврате на main-menu — снова включает. Root берётся из appSettings; для вызова вне меню передайте false.
     */
    public static void show(String panelId, AppSettings appSettings, boolean disableMouseOnShow) {
        if (appSettings == null) return;
        VBox container = appSettings.getMainMenuContentPane();
        if (container == null) return;
        PanelHandler h = get(panelId);
        if (h == null) return;

        StackPane root = appSettings.getRoot();
        if (disableMouseOnShow && root != null) {
            if ("main-menu".equals(panelId)) {
                SceneInfo.enableEventHandler(root);
            } else {
                SceneInfo.disableEventHandler(root);
            }
        }

        Node panel = h.create(appSettings);
        if (panel != null) container.getChildren().setAll(panel);
    }

    public static void discover() {
        if (discovered) return;
        for (String pkg : SCAN_PACKAGES) {
            Reflections ref = new Reflections(pkg);
            Set<Class<?>> annotated = ref.getTypesAnnotatedWith(Panel.class);
            for (Class<?> c : annotated) {
                if (c.isInterface()) continue;
                Panel ann = c.getAnnotation(Panel.class);
                if (ann == null) continue;
                try {
                    PanelHandler handler = appSettings -> {
						try {
							return (Node) c.getConstructor(AppSettings.class).newInstance(appSettings);
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
						}
						return null;
					};
                    register(ann.value(), handler);
                } catch (Exception e) {
                    LOG.error("PanelRegistry: could not register " + c.getSimpleName() + ": " + e.getMessage());
                }
            }
        }
        discovered = true;
    }

    private static void ensureDiscovered() {
        if (!discovered) discover();
    }
}
