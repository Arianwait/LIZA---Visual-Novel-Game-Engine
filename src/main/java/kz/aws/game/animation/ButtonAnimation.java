package kz.aws.game.animation;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class ButtonAnimation {
	public static void animateButtonsSequentially(Node... buttons) {
	    SequentialTransition sequentialTransition = new SequentialTransition();

	    for (Node button : buttons) {
	        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), button);
	        transition.setFromY(button.getLayoutY() + 20); // Стартовая позиция ниже
	        transition.setToY(button.getLayoutY());       // Конечная позиция
	        transition.setCycleCount(1);
	        sequentialTransition.getChildren().add(transition);
	    }

	    sequentialTransition.play();
	}
	
	public static void addButtonHoverAnimation(Button... buttons) {
	    for (Button button : buttons) {
	        // Добавляем событие для наведения
	        button.setOnMouseEntered(e -> {
	            ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), button);
	            scaleUp.setToX(1.1); // Увеличение по ширине
	            scaleUp.setToY(1.1); // Увеличение по высоте
	            scaleUp.setInterpolator(Interpolator.EASE_OUT); // Плавный эффект
	            scaleUp.play();
	        });

	        // Добавляем событие для убирания курсора
	        button.setOnMouseExited(e -> {
	            ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), button);
	            scaleDown.setToX(1.0); // Возврат к стандартному размеру
	            scaleDown.setToY(1.0);
	            scaleDown.setInterpolator(Interpolator.EASE_IN); // Плавный эффект возврата
	            scaleDown.play();
	        });

	        // Добавляем эффект нажатия
	        button.setOnMousePressed(e -> {
	            ScaleTransition scalePress = new ScaleTransition(Duration.seconds(0.1), button);
	            scalePress.setToX(0.95); // Сжатие по ширине
	            scalePress.setToY(0.95); // Сжатие по высоте
	            scalePress.setInterpolator(Interpolator.EASE_IN);
	            scalePress.play();
	        });

	        button.setOnMouseReleased(e -> {
	            ScaleTransition scaleRelease = new ScaleTransition(Duration.seconds(0.1), button);
	            scaleRelease.setToX(1.1); // Возврат к увеличенному состоянию
	            scaleRelease.setToY(1.1);
	            scaleRelease.setInterpolator(Interpolator.EASE_OUT);
	            scaleRelease.play();
	        });
	    }
	}
}
