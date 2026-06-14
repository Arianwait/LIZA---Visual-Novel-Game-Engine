package kz.aws.game.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimatedSlider {

    public static void addSliderAnimation(Slider slider) {
        // Добавляем тень к ползунку (thumb)
        DropShadow shadow = new DropShadow(5, Color.rgb(0, 0, 0, 0.5));

        slider.setOnMouseEntered(event -> {
            Timeline hoverAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(slider.opacityProperty(), slider.getOpacity())),
                new KeyFrame(Duration.seconds(0.2), new KeyValue(slider.opacityProperty(), 1))
            );
            hoverAnimation.play();
            slider.lookup(".thumb").setEffect(shadow); // Добавляем тень на ползунок
        });

        slider.setOnMouseExited(event -> {
            Timeline exitAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(slider.opacityProperty(), slider.getOpacity())),
                new KeyFrame(Duration.seconds(0.2), new KeyValue(slider.opacityProperty(), 0.8))
            );
            exitAnimation.play();
            slider.lookup(".thumb").setEffect(null); // Убираем тень с ползунка
        });

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Timeline valueAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(slider.valueProperty(), oldValue.doubleValue())),
                new KeyFrame(Duration.seconds(0.1), new KeyValue(slider.valueProperty(), newValue.doubleValue()))
            );
            valueAnimation.play();
        });
    }
}

