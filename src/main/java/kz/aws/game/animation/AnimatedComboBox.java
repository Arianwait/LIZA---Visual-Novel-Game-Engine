package kz.aws.game.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.util.Duration;

public class AnimatedComboBox {

    public static void addComboBoxAnimation(ComboBox<String> comboBox) {
        comboBox.setOnShowing(e -> {
            Node listViewNode = comboBox.lookup(".list-view");
            if (listViewNode != null) {
                // Начальное состояние
                listViewNode.setOpacity(0);
                listViewNode.setScaleX(0.9);
                listViewNode.setScaleY(0.9);
                listViewNode.setTranslateY(-10);

                // Анимация открытия
                Timeline openAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(listViewNode.opacityProperty(), 0),
                        new KeyValue(listViewNode.scaleXProperty(), 0.9),
                        new KeyValue(listViewNode.scaleYProperty(), 0.9),
                        new KeyValue(listViewNode.translateYProperty(), -10)
                    ),
                    new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(listViewNode.opacityProperty(), 1),
                        new KeyValue(listViewNode.scaleXProperty(), 1),
                        new KeyValue(listViewNode.scaleYProperty(), 1),
                        new KeyValue(listViewNode.translateYProperty(), 0)
                    )
                );
                openAnimation.play();
            }
        });

        comboBox.setOnHiding(e -> {
            Node listViewNode = comboBox.lookup(".list-view");
            if (listViewNode != null) {
                // Анимация закрытия
                Timeline closeAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(listViewNode.opacityProperty(), 1),
                        new KeyValue(listViewNode.scaleXProperty(), 1),
                        new KeyValue(listViewNode.scaleYProperty(), 1),
                        new KeyValue(listViewNode.translateYProperty(), 0)
                    ),
                    new KeyFrame(Duration.seconds(0.2),
                        new KeyValue(listViewNode.opacityProperty(), 0),
                        new KeyValue(listViewNode.scaleXProperty(), 0.9),
                        new KeyValue(listViewNode.scaleYProperty(), 0.9),
                        new KeyValue(listViewNode.translateYProperty(), -10)
                    )
                );
                closeAnimation.play();
            }
        });
    }
}

