package kz.aws.game.mainscene;

import java.io.IOException;

import javafx.animation.TranslateTransition;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import kz.aws.game.actionscenarios.ShowMainMenu;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.utils.VirtualViewport;

/**
 * Финальные титры: текст из lib/titries.txt проезжает по экрану снизу вверх,
 * после чего открывается главное меню.
 */
public class TitlesAnimation{

    /**
     * Запускает анимацию титров.
     *
     * @param appSettings настройки приложения
     */
    public void start(AppSettings appSettings) {
        Text titres = new Text();
        titres.setFont(Font.font(20));
        appSettings.getRoot().getChildren().add(titres);

        try {
            // Чтение текста из файла (UTF-8: титры на русском)
            titres.setText(java.nio.file.Files.readString(
                    java.nio.file.Path.of("lib/titries.txt"),
                    java.nio.charset.StandardCharsets.UTF_8));

            // Создание анимации для титров с задержкой и увеличенной продолжительностью
            TranslateTransition transition = new TranslateTransition(Duration.seconds(12), titres); // Продолжительность анимации увеличена до 6 секунд
            transition.setFromY(VirtualViewport.DESIGN_HEIGHT);
            transition.setToY(-VirtualViewport.DESIGN_HEIGHT);
            transition.setCycleCount(1);
            transition.setAutoReverse(false);
//            transition.setDelay(Duration.seconds(2)); // Задержка перед началом анимации установлена на 2 секунды
            
            transition.setOnFinished(event -> {
            	ShowMainMenu.initializeMainMenuScene(appSettings);
            });

            transition.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        appSettings.getStage().show();
    }
}
