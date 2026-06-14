package kz.aws.game.panel.puzzle.puzzelpanel;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import kz.aws.game.panel.BaseGamePanel;
import kz.aws.game.panel.GamePanel;
import kz.aws.game.panel.PanelResult;

/**
 * Панель-загадка: крестики-нолики.
 * Игрок — X, компьютер — O. Попытка одна — без перезапуска.
 * Победа → {@link PanelResult#success()}, поражение/ничья → {@link PanelResult#failure()}.
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="puzzle" id="tic_tac_toe" flag="won_ttt" onSuccess="5" onFailure="6"/&gt;
 * </pre>
 */
@GamePanel(
    id            = "tic_tac_toe",
    title         = "Крестики-нолики",
    widthPercent  = 40,
    heightPercent = 60,
    modal         = true
)
public class TicTacToePuzzle extends BaseGamePanel {

    private final int[] board = new int[9];
    private final Button[] cells = new Button[9];
    private Label statusLabel;
    private boolean gameOver = false;

    @Override
    protected void initialize() {
        setStyle(
            "-fx-background-color: rgba(45, 30, 20, 0.95);" +
            "-fx-border-color: rgba(180, 140, 100, 0.5); -fx-border-width: 2;" +
            "-fx-border-radius: 10; -fx-background-radius: 10;"
        );

        Label title = new Label(getPanelAnnotation().title());
        title.setStyle("-fx-text-fill: #e8d5c0; -fx-font-size: 20; -fx-font-weight: bold;");

        statusLabel = new Label("Ваш ход (X)");
        statusLabel.setStyle("-fx-text-fill: #c4a882; -fx-font-size: 14;");

        GridPane grid = buildGrid();

        VBox layout = new VBox(14, title, statusLabel, grid);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        getChildren().add(layout);
    }

    /**
     * Строит сетку 3×3 с кнопками-клетками.
     *
     * @return заполненная сетка
     */
    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        for (int i = 0; i < 9; i++) {
            final int idx = i;
            Button cell = new Button();
            cell.setPrefSize(80, 80);
            cell.setStyle(cellStyle(""));
            cell.setOnAction(e -> playerMove(idx));
            cells[i] = cell;
            grid.add(cell, i % 3, i / 3);
        }
        return grid;
    }

    /**
     * Обрабатывает ход игрока.
     *
     * @param idx индекс клетки
     */
    private void playerMove(int idx) {
        if (gameOver || board[idx] != 0) return;
        place(idx, 1);
        if (checkWin(1))        { endGame(true,  "Вы победили!"); }
        else if (isBoardFull()) { endGame(false, "Ничья..."); }
        else {
            statusLabel.setText("Ход компьютера (O)");
            computerMove();
        }
    }

    /**
     * Ход компьютера: блокирует победу игрока или завершает свою линию.
     */
    private void computerMove() {
        int move = findBestMove(2);
        if (move == -1) move = findBestMove(1);
        if (move == -1) move = randomEmpty();
        if (move == -1) return;
        place(move, 2);
        if (checkWin(2))        { endGame(false, "Компьютер победил!"); }
        else if (isBoardFull()) { endGame(false, "Ничья..."); }
        else { statusLabel.setText("Ваш ход (X)"); }
    }

    /**
     * Ставит символ игрока в клетку.
     *
     * @param idx    индекс клетки
     * @param player 1 — игрок (X), 2 — компьютер (O)
     */
    private void place(int idx, int player) {
        board[idx] = player;
        String sym   = player == 1 ? "X" : "O";
        String color = player == 1 ? "#d4836a" : "#8bb0a0";
        cells[idx].setText(sym);
        cells[idx].setStyle(cellStyle(color));
        cells[idx].setDisable(true);
    }

    /**
     * Проверяет победу указанного игрока.
     *
     * @param p номер игрока (1 или 2)
     * @return {@code true} если есть выигрышная линия
     */
    private boolean checkWin(int p) {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : lines) {
            if (board[l[0]] == p && board[l[1]] == p && board[l[2]] == p) {
                for (int i : l) cells[i].setStyle(cellStyle("#f5e6d0"));
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, заполнена ли вся доска.
     *
     * @return {@code true} если свободных клеток нет
     */
    private boolean isBoardFull() {
        for (int v : board) if (v == 0) return false;
        return true;
    }

    /**
     * Ищет ход, завершающий линию из двух фигур указанного игрока.
     *
     * @param player номер игрока
     * @return индекс клетки или -1
     */
    private int findBestMove(int player) {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : lines) {
            int sum = 0, empty = -1;
            for (int i : l) { if (board[i] == player) sum++; else if (board[i] == 0) empty = i; }
            if (sum == 2 && empty != -1) return empty;
        }
        return -1;
    }

    /**
     * Выбирает пустую клетку по приоритету (центр → углы → стороны).
     *
     * @return индекс клетки или -1
     */
    private int randomEmpty() {
        int[] pref = {4, 0, 2, 6, 8, 1, 3, 5, 7};
        for (int i : pref) if (board[i] == 0) return i;
        return -1;
    }

    /**
     * Завершает игру с результатом.
     *
     * @param playerWon {@code true} если игрок выиграл
     * @param message   текст результата
     */
    private void endGame(boolean playerWon, String message) {
        gameOver = true;
        statusLabel.setStyle("-fx-text-fill: " + (playerWon ? "#7ab87a" : "#c47a7a") +
                             "; -fx-font-size: 15; -fx-font-weight: bold;");
        statusLabel.setText(message);
        for (Button cell : cells) cell.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> complete(playerWon ? PanelResult.success() : PanelResult.failure()));
        pause.play();
    }

    /**
     * Стиль клетки.
     *
     * @param textColor цвет текста (пустая строка — дефолтный)
     * @return CSS-строка
     */
    private static String cellStyle(String textColor) {
        String c = textColor.isEmpty() ? "#c4a882" : textColor;
        return "-fx-background-color: #4a3228; -fx-border-color: rgba(180, 140, 100, 0.5); -fx-border-width: 2;" +
               "-fx-border-radius: 4; -fx-background-radius: 4;" +
               "-fx-text-fill: " + c + "; -fx-font-size: 28; -fx-font-weight: bold; -fx-cursor: hand;";
    }
}
