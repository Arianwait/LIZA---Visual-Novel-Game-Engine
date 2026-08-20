package kz.aws.game.panel.puzzle.puzzlepanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import kz.aws.game.panel.BaseGamePanel;
import kz.aws.game.panel.GamePanel;
import kz.aws.game.panel.PanelResult;

/**
 * Панель: ввод кода/пароля.
 * Переопредели {@link #getExpectedCode()} в подклассе для другого кода.
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="puzzle" id="code_input" flag="safe_opened" onSuccess="12" onFailure="14"/&gt;
 * </pre>
 */
@GamePanel(
    id            = "code_input",
    title         = "Введи код",
    widthPercent  = 45,
    heightPercent = 35,
    modal         = true
)
public class CodeInputPuzzle extends BaseGamePanel {

    @Override
    protected void initialize() {
        setStyle(
            "-fx-background-color: rgba(45, 30, 20, 0.95);" +
            "-fx-border-color: rgba(180, 140, 100, 0.5); -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;"
        );

        Label titleLabel = new Label(getPanelAnnotation().title());
        titleLabel.setStyle("-fx-text-fill: #e8d5c0; -fx-font-size: 18; -fx-font-weight: bold;");

        TextField codeField = new TextField();
        codeField.setPromptText("Введите код...");
        codeField.setMaxWidth(200);
        codeField.setStyle(
            "-fx-background-color: #4a3228; -fx-text-fill: #f5e6d0;" +
            "-fx-prompt-text-fill: #a89070; -fx-border-color: rgba(180, 140, 100, 0.5);" +
            "-fx-font-size: 16; -fx-padding: 6;"
        );

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c47a7a; -fx-font-size: 12;");

        Button confirmBtn = new Button("Подтвердить");
        confirmBtn.setStyle(
            "-fx-background-color: #6b4a3a; -fx-text-fill: #f5e6d0;" +
            "-fx-font-size: 14; -fx-padding: 6 20;" +
            "-fx-border-color: rgba(180, 140, 100, 0.4);" +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;"
        );
        confirmBtn.setOnAction(e -> {
            String entered = codeField.getText().trim();
            if (getExpectedCode().equals(entered)) {
                complete(PanelResult.withData(true, entered));
            } else {
                errorLabel.setText("Неверный код. Попробуй ещё раз.");
                codeField.clear();
                codeField.requestFocus();
            }
        });
        codeField.setOnAction(e -> confirmBtn.fire());

        VBox layout = new VBox(12, titleLabel, codeField, errorLabel, confirmBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        getChildren().add(layout);
    }

    /**
     * Ожидаемый код. Переопредели в подклассе для другого значения.
     *
     * @return строка с ожидаемым кодом
     */
    protected String getExpectedCode() {
        return "1234";
    }
}
