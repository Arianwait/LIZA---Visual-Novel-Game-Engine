package kz.aws.game.panel.puzzle.puzzelpanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import kz.aws.game.panel.BaseGamePanel;
import kz.aws.game.panel.GamePanel;
import kz.aws.game.panel.PanelResult;
import kz.aws.game.scenelist.SceneController;

import java.util.ArrayList;
import java.util.List;

/**
 * Панель «Доска улик» — показывает собранные игроком улики.
 *
 * <p>Улики хранятся в {@code playerChoices} с префиксом {@value #EVIDENCE_PREFIX}.
 * Если улик нет — показывается сообщение «Улики не найдены».
 *
 * <p>Кнопка «Улики» в игровом меню открывает эту панель через {@code GameEvidenceAction}.
 */
@GamePanel(
    id            = "evidence_board",
    title         = "Улики",
    widthPercent  = 65,
    heightPercent = 75,
    modal         = true
)
public class EvidencePanel extends BaseGamePanel {

    private static final String EVIDENCE_PREFIX = "evidence_";

    @Override
    protected void initialize() {
        setStyle(
            "-fx-background-color: rgba(45, 30, 20, 0.95);" +
            "-fx-border-color: rgba(180, 140, 100, 0.5); -fx-border-width: 2;" +
            "-fx-border-radius: 10; -fx-background-radius: 10;"
        );

        Label titleLabel = new Label(getPanelAnnotation().title());
        titleLabel.setStyle(
            "-fx-text-fill: #e8d5c0; -fx-font-size: 20; -fx-font-weight: bold;"
        );

        VBox itemsBox = new VBox(8);
        itemsBox.setPadding(new Insets(8));
        List<String> items = collectEvidence();
        if (items.isEmpty()) {
            Label empty = new Label("Улики не найдены.");
            empty.setStyle(
                "-fx-text-fill: #a89070; -fx-font-size: 14; -fx-font-style: italic;"
            );
            itemsBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < items.size(); i++) {
                Label item = new Label((i + 1) + ". " + items.get(i));
                item.setStyle("-fx-text-fill: #c4a882; -fx-font-size: 14;");
                item.setWrapText(true);
                itemsBox.getChildren().add(item);
            }
        }

        ScrollPane scroll = new ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: rgba(35, 22, 14, 0.8);"
        );
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button closeBtn = new Button("Закрыть");
        closeBtn.setStyle(
            "-fx-background-color: #4a3228; -fx-text-fill: #c4a882;" +
            "-fx-font-size: 14; -fx-padding: 6 20; -fx-background-radius: 4;" +
            "-fx-border-color: rgba(180, 140, 100, 0.4); -fx-border-radius: 4; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> complete(PanelResult.close()));

        HBox header = new HBox(titleLabel);
        header.setAlignment(Pos.CENTER);

        VBox layout = new VBox(14, header, scroll, closeBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(layout);
    }

    /**
     * Собирает все улики из playerChoices с префиксом {@value #EVIDENCE_PREFIX}.
     *
     * @return список текстов улик
     */
    private List<String> collectEvidence() {
        List<String> evidence = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String value = SceneController.getPlayerChoice(EVIDENCE_PREFIX + i);
            if (value == null || value.isEmpty()) continue;
            evidence.add(value);
        }
        return evidence;
    }
}
