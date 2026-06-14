package kz.aws.game.mainscene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javafx.animation.TranslateTransition;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import kz.aws.game.actionscenarios.ShowMainMenu;
import kz.aws.game.appsettings.AppSettings;

public class TitresAnimation{
    public void start(AppSettings appSettings) {


        Text titres = new Text();
        titres.setFont(Font.font(20));
        appSettings.getRoot().getChildren().add(titres);

        try {
            // Чтение текста из файла
            BufferedReader reader = new BufferedReader(new FileReader("lib/titries.txt"));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            titres.setText(content.toString());

            // Создание анимации для титров с задержкой и увеличенной продолжительностью
            TranslateTransition transition = new TranslateTransition(Duration.seconds(12), titres); // Продолжительность анимации увеличена до 6 секунд
            transition.setFromY(appSettings.getScene().getHeight()); // Появление сверху
            transition.setToY(-appSettings.getScene().getHeight()); // Движение вниз
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
        
        appSettings.getStagePain().show();
    }
}
