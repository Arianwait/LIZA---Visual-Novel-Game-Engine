package kz.aws.game.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.CheckBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimatedCheckBox {
    
    // Константы для анимации
    private static final double SCALE_UP = 1.2;
    private static final double SCALE_DOWN = 0.8;
    private static final Duration ANIMATION_DURATION = Duration.seconds(0.2);
    private static final Duration TOTAL_DURATION = Duration.seconds(0.3);
    
    // Переиспользуемые объекты для оптимизации
    private static final DropShadow SHADOW = new DropShadow(5, Color.rgb(0, 0, 0, 0.5));
    
    public static void addCheckBoxAnimation(CheckBox checkBox) {
        if (checkBox == null) {
            System.err.println("CheckBox не может быть null");
            return;
        }
        
        // Создаем Timeline один раз для переиспользования
        Timeline timeline = new Timeline();
        
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Останавливаем предыдущую анимацию
            timeline.stop();
            timeline.getKeyFrames().clear();
            
            if (newValue) {
                // Анимация при активации
                timeline.getKeyFrames().addAll(
                    new KeyFrame(ANIMATION_DURATION, 
                        new KeyValue(checkBox.scaleXProperty(), SCALE_UP),
                        new KeyValue(checkBox.scaleYProperty(), SCALE_UP)),
                    new KeyFrame(TOTAL_DURATION, 
                        new KeyValue(checkBox.scaleXProperty(), 1),
                        new KeyValue(checkBox.scaleYProperty(), 1))
                );
                checkBox.setEffect(SHADOW); // Добавляем тень
            } else {
                // Анимация при деактивации
                timeline.getKeyFrames().addAll(
                    new KeyFrame(ANIMATION_DURATION, 
                        new KeyValue(checkBox.scaleXProperty(), SCALE_DOWN),
                        new KeyValue(checkBox.scaleYProperty(), SCALE_DOWN)),
                    new KeyFrame(TOTAL_DURATION, 
                        new KeyValue(checkBox.scaleXProperty(), 1),
                        new KeyValue(checkBox.scaleYProperty(), 1))
                );
                checkBox.setEffect(null); // Убираем тень
            }
            timeline.play();
        });
    }
}
