package kz.aws.game.background;

import java.io.Serializable;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class BackgroundDispatcher implements SceneBackground, Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8312913447842542033L;

    public Background getBackground(String filePathNOTfile) {
        String filePath = "file:" + filePathNOTfile;

        // Проверяем формат файла
        if (filePathNOTfile.toLowerCase().endsWith(".gif")) {
            // Если это GIF, берем первый кадр
            Image gifImage = new Image(filePath, true); // true для асинхронной загрузки
            BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
            BackgroundImage backgroundImg = new BackgroundImage(gifImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            return new Background(backgroundImg);
        } else {
            // Для других форматов (например, PNG/JPG)
            Image image = new Image(filePath);
            BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
            BackgroundImage backgroundImg = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
            return new Background(backgroundImg);
        }
    }
    
    /**
     * Создать Background из уже загруженного Image (из кеша)
     */
    public Background getBackgroundFromImage(Image image) {
        if (image == null) return null;
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage backgroundImg = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, backgroundSize);
        return new Background(backgroundImg);
    }
}
