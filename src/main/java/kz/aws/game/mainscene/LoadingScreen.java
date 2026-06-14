package kz.aws.game.mainscene;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.utils.AssetPreloader;
import kz.aws.game.utils.MenuResourceCache;

public class LoadingScreen {

    private final StackPane root;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final ImageView logoImageView;

    public LoadingScreen(double width, double height) {
        root = new StackPane();
        root.getStyleClass().add("loading-root");
        root.setStyle("-fx-background-color: black;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        // Логотип
        Image logoImage = MenuResourceCache.getImage("file:lib/Logo/logo.png");
        if (logoImage == null) {
            // Если нет в кеше, загружаем напрямую
            logoImage = new Image("file:lib/Logo/logo.png");
        }
        logoImageView = new ImageView(logoImage);
        logoImageView.setPreserveRatio(true);
        logoImageView.setFitWidth(width * 0.4); // 40% ширины экрана
        logoImageView.setFitHeight(height * 0.4); // 40% высоты экрана
        logoImageView.setOpacity(0.9);

        statusLabel = new Label("Loading...");
        statusLabel.getStyleClass().add("loading-label");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("loading-bar");
        progressBar.setPrefWidth(width * 0.3); // 30% ширины экрана
        progressBar.setPrefHeight(20);

        content.getChildren().addAll(logoImageView, statusLabel, progressBar);
        root.getChildren().add(content);
    }

    public Scene getScene() {
        return null; // Not used
    }
    
    public StackPane getRoot() {
        return root;
    }

    public void startLoading(int startSceneId, Runnable onFinished) {
        AssetPreloader task = new AssetPreloader(startSceneId);
        
        progressBar.progressProperty().bind(task.progressProperty());
        // Custom text binding: "Loading... 45%"
        statusLabel.textProperty().bind(task.progressProperty().multiply(100).asString("LOADING... %.0f%%"));
        // statusLabel.textProperty().bind(task.messageProperty()); // Old way

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                // Unbind to prevent leaks
                progressBar.progressProperty().unbind();
                statusLabel.textProperty().unbind();
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        });
        
        task.setOnFailed(e -> {
            System.err.println("Loading failed: " + task.getException().getMessage());
            task.getException().printStackTrace();
            // Proceed anyway or show error? For now proceed.
            if (onFinished != null) onFinished.run();
        });

        new Thread(task).start();
    }
}
