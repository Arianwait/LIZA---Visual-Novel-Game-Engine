package kz.aws.game.mainscene;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import kz.aws.game.actionscenarios.ShowMainMenu;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.utils.MenuResourceCache;
import kz.aws.game.utils.ThemesConfigParser;
import kz.aws.game.utils.VirtualViewport;

/**
 * Стартовая анимация логотипа: fade-in/fade-out поверх чёрного фона,
 * параллельно предзагружает ресурсы меню, затем открывает главное меню.
 */
public class LogoAnimation extends StackPane {

    public LogoAnimation(AppSettings appSettings) {
        // Загрузка изображения логотипа
        appSettings.getRoot().setStyle("-fx-background-color: black;");
        
        // Предзагружаем ресурсы меню (в т.ч. фон текущей темы), чтобы при переходе в меню фон появился сразу
        String themeBg = null;
        var theme = ThemesConfigParser.getThemeById(appSettings.getUiTheme());
        if (theme != null && theme.getBackground() != null && !theme.getBackground().isEmpty()) {
            themeBg = theme.getBackground();
        }
        System.out.println("Starting MENU resources preload during logo animation...");
        MenuResourceCache.preloadMenuResources(themeBg);
        
        Image logoImage = MenuResourceCache.getImage("file:lib/Logo/logo.png");
        if (logoImage == null) {
            logoImage = new Image("file:lib/Logo/logo.png");
        }
        
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setOpacity(1.0); // Начальная прозрачность
        StackPane.setAlignment(logoImageView, Pos.CENTER);
        logoImageView.setPreserveRatio(true);
        logoImageView.toFront();

        logoImageView.setFitWidth(VirtualViewport.width(0.8));
        logoImageView.setFitHeight(VirtualViewport.height(0.8));
        
        // Добавление логотипа в вертикальный контейнер (VBox)
        getChildren().add(logoImageView);

        // Запуск анимации появления логотипа
        playLogoAnimation(appSettings);
    }

    private void playLogoAnimation(AppSettings appSettings) {
        // Если ресурсы уже загружены (например, повторный показ), анимация быстрее
        double fadeInDuration = MenuResourceCache.isPreloaded() ? 1.5 : 2.5;
        double fadeOutDuration = 1.5;
        
        FadeTransition fadeInTransition = new FadeTransition(Duration.seconds(fadeInDuration), this);
        fadeInTransition.setFromValue(0.0);
        fadeInTransition.setToValue(1.0);

        FadeTransition fadeOutTransition = new FadeTransition(Duration.seconds(fadeOutDuration), this);
        fadeOutTransition.setFromValue(1.0);
        fadeOutTransition.setToValue(0.0);

        fadeInTransition.setOnFinished(event -> {
            // Вместо Thread.sleep используем неблокирующую проверку
            waitForResourcesAndFadeOut(fadeOutTransition);
        });

        fadeOutTransition.setOnFinished(event -> {
            System.out.println("Logo animation finished. Menu resources preloaded: " + MenuResourceCache.isPreloaded());
            ShowMainMenu.initializeMainMenuScene(appSettings);
        });

        // Запуск анимации появления
        fadeInTransition.play();
    }
    
    /**
     * Ждет загрузки ресурсов, не блокируя UI поток
     */
    private void waitForResourcesAndFadeOut(FadeTransition fadeOut) {
        if (MenuResourceCache.isPreloaded()) {
            fadeOut.play();
        } else {
            // Проверяем снова через 100мс
            // System.out.println("Waiting for menu resources..."); // Слишком много спама
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> waitForResourcesAndFadeOut(fadeOut));
            pause.play();
        }
    }
}
