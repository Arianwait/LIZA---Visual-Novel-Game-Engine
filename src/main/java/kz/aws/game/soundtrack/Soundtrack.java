package kz.aws.game.soundtrack;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import kz.aws.game.appsettings.AppSettings;

import java.io.File;

public class Soundtrack {

    private static MediaPlayer mediaPlayer;

    public static MediaPlayer startSound(AppSettings appSettings, String audioFilePath) {

        // Проверяем, создан ли mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        Media media = new Media(new File(audioFilePath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        mediaPlayer.setVolume(appSettings.getVolumeValue());
        // Устанавливаем prefetch в продолжительность файла
        mediaPlayer.setStartTime(Duration.ZERO);
        mediaPlayer.setStopTime(media.getDuration());

        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));

        return mediaPlayer;
    }
}

