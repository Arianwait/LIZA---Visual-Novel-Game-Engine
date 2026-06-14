package kz.aws.game.mainscene;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneBuilder;

public class SaveLoudSecthion extends VBox {

    private static final String SAVE_DIRECTORY = "save";
    private SceneBuilder sceneBuilder;
    public SaveLoudSecthion(AppSettings appSettings) {
        setAlignment(Pos.CENTER);
        setSpacing(10);

        Label titleLabel = new Label("Выберите сохранение:");
        titleLabel.setStyle("-fx-font-size: "+ appSettings.getStagePain().getHeight()*0.02  +"; -fx-font-weight: bold; -fx-text-fill: brown;");

        List<String> saveFiles = getSaveFilesList();
        
        
        
        for (String saveFile : saveFiles) {
        	String FileText = removeFileExtension(saveFile);
        	String[] parts = FileText.split("_");
            String timestamp = parts[1];
            Button saveButton = createSaveButton(timestamp);
            getChildren().add(saveButton);
            saveButton.getStyleClass().add("game-button");
            saveButton.setOnAction(event -> {getChildren().setAll(sceneBuilder = new SceneBuilder(appSettings, saveFile, Integer.parseInt(parts[1]))); getChildren().setAll(sceneBuilder);});
        }
    }
    
    
    private List<String> getSaveFilesList() {
        List<String> saveFiles = new ArrayList<>();
        File saveDirectory = new File(SAVE_DIRECTORY);

        if (saveDirectory.exists() && saveDirectory.isDirectory()) {
            File[] files = saveDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".ser")) {
                        saveFiles.add(file.getName());
                    }
                }
            }
        }

        return saveFiles;
    }

    private Button createSaveButton(String saveFileName) {
        Button saveButton = new Button(saveFileName);

        
        
        // Добавляем слушатель события для удаления сохранения
        saveButton.setOnAction(event -> {
            boolean isDeleted = deleteSaveFile(saveFileName);
            if (isDeleted) {
                // Если файл удален успешно, убираем кнопку из VBox
                getChildren().remove(saveButton);
            }
        });

        return saveButton;
    }

    private boolean deleteSaveFile(String saveFileName) {
        File saveFile = new File(SAVE_DIRECTORY, saveFileName);
        return saveFile.delete();
    }
    
    private static String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}
