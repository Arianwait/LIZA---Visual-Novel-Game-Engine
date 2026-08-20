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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Панель-загадка: найди пару (Memory Match).
 *
 * <p>Сетка 4×4 с 8 парами символов. Игрок открывает по две карточки —
 * если символы совпадают, карточки остаются открытыми.
 * Нужно найти все пары за ограниченное число попыток. Попытка одна — без перезапуска.
 *
 * <p>Победа → {@link PanelResult#success()}, проигрыш → {@link PanelResult#failure()}.
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="puzzle" id="memory_match" flag="won_memory" onSuccess="5" onFailure="6"/&gt;
 * </pre>
 */
@GamePanel(
    id            = "memory_match",
    title         = "Найди пару",
    widthPercent  = 45,
    heightPercent = 70,
    modal         = true
)
public class MemoryMatchPuzzle extends BaseGamePanel {

    private static final int GRID_SIZE = 4;
    private static final int TOTAL_CARDS = GRID_SIZE * GRID_SIZE;
    private static final int MAX_ATTEMPTS = 12;
    private static final String[] SYMBOLS = {"★", "♦", "♠", "♣", "♥", "◆", "▲", "●"};

    private final Button[] cards = new Button[TOTAL_CARDS];
    private final String[] cardSymbols = new String[TOTAL_CARDS];
    private final boolean[] matched = new boolean[TOTAL_CARDS];

    private int firstFlipped = -1;
    private int attemptsLeft = MAX_ATTEMPTS;
    private int matchedPairs = 0;
    private boolean inputLocked = false;

    private Label statusLabel;
    private Label attemptsLabel;

    @Override
    protected void initialize() {
        setStyle(
            "-fx-background-color: rgba(45, 30, 20, 0.95);" +
            "-fx-border-color: rgba(180, 140, 100, 0.5); -fx-border-width: 2;" +
            "-fx-border-radius: 10; -fx-background-radius: 10;"
        );

        Label title = createTitle();
        statusLabel = createStatusLabel();
        attemptsLabel = createAttemptsLabel();
        GridPane grid = buildGrid();

        VBox layout = new VBox(12, title, statusLabel, attemptsLabel, grid);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        getChildren().add(layout);

        shuffleCards();
    }

    /**
     * Создаёт заголовок панели.
     *
     * @return метка с заголовком
     */
    private Label createTitle() {
        Label title = new Label(getPanelAnnotation().title());
        title.setStyle(
            "-fx-text-fill: #e8d5c0; -fx-font-size: 20; -fx-font-weight: bold;"
        );
        return title;
    }

    /**
     * Создаёт метку статуса игры.
     *
     * @return метка статуса
     */
    private Label createStatusLabel() {
        Label label = new Label("Откройте две карточки");
        label.setStyle("-fx-text-fill: #c4a882; -fx-font-size: 14;");
        return label;
    }

    /**
     * Создаёт метку счётчика попыток.
     *
     * @return метка с оставшимися попытками
     */
    private Label createAttemptsLabel() {
        Label label = new Label(formatAttempts());
        label.setStyle("-fx-text-fill: #a89070; -fx-font-size: 13;");
        return label;
    }

    /**
     * Строит сетку 4×4 с карточками-кнопками.
     *
     * @return заполненная сетка
     */
    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        for (int i = 0; i < TOTAL_CARDS; i++) {
            final int idx = i;
            Button card = new Button("?");
            card.setPrefSize(70, 70);
            card.setStyle(hiddenStyle());
            card.setOnAction(e -> flipCard(idx));
            cards[i] = card;
            grid.add(card, i % GRID_SIZE, i / GRID_SIZE);
        }
        return grid;
    }

    /**
     * Перемешивает символы и раскладывает по карточкам.
     */
    private void shuffleCards() {
        List<String> deck = new ArrayList<>();
        for (int i = 0; i < TOTAL_CARDS / 2; i++) {
            deck.add(SYMBOLS[i]);
            deck.add(SYMBOLS[i]);
        }
        Collections.shuffle(deck);
        for (int i = 0; i < TOTAL_CARDS; i++) {
            cardSymbols[i] = deck.get(i);
        }
    }

    /**
     * Обрабатывает нажатие на карточку.
     *
     * @param idx индекс карточки в сетке
     */
    private void flipCard(int idx) {
        if (inputLocked || matched[idx] || idx == firstFlipped) return;

        revealCard(idx);

        if (firstFlipped == -1) {
            firstFlipped = idx;
            statusLabel.setText("Откройте вторую карточку");
            return;
        }

        attemptsLeft--;
        attemptsLabel.setText(formatAttempts());
        checkMatch(idx);
    }

    /**
     * Проверяет совпадение двух открытых карточек.
     *
     * @param secondIdx индекс второй открытой карточки
     */
    private void checkMatch(int secondIdx) {
        inputLocked = true;
        int first = firstFlipped;
        firstFlipped = -1;

        if (cardSymbols[first].equals(cardSymbols[secondIdx])) {
            handleMatchFound(first, secondIdx);
        } else {
            handleMismatch(first, secondIdx);
        }
    }

    /**
     * Обрабатывает найденную пару.
     *
     * @param a индекс первой карточки
     * @param b индекс второй карточки
     */
    private void handleMatchFound(int a, int b) {
        matched[a] = true;
        matched[b] = true;
        cards[a].setStyle(matchedStyle());
        cards[b].setStyle(matchedStyle());
        matchedPairs++;

        if (matchedPairs == TOTAL_CARDS / 2) {
            endGame(true, "Все пары найдены!");
            return;
        }
        // попытка расходуется и на удачное открытие: без этой проверки
        // счётчик уходил в минус и игра продолжалась после «0 попыток»
        if (attemptsLeft <= 0) {
            PauseTransition pause = new PauseTransition(Duration.millis(600));
            pause.setOnFinished(e -> endGame(false, "Попытки закончились!"));
            pause.play();
            return;
        }
        statusLabel.setText("Пара найдена! Продолжайте");
        inputLocked = false;
    }

    /**
     * Обрабатывает несовпадение — закрывает карточки с задержкой.
     *
     * @param a индекс первой карточки
     * @param b индекс второй карточки
     */
    private void handleMismatch(int a, int b) {
        statusLabel.setText("Не совпало!");

        if (attemptsLeft <= 0) {
            PauseTransition pause = new PauseTransition(Duration.millis(600));
            pause.setOnFinished(e -> endGame(false, "Попытки закончились!"));
            pause.play();
            return;
        }

        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(e -> {
            hideCard(a);
            hideCard(b);
            statusLabel.setText("Откройте две карточки");
            inputLocked = false;
        });
        pause.play();
    }

    /**
     * Показывает символ на карточке.
     *
     * @param idx индекс карточки
     */
    private void revealCard(int idx) {
        cards[idx].setText(cardSymbols[idx]);
        cards[idx].setStyle(revealedStyle());
    }

    /**
     * Скрывает карточку обратно.
     *
     * @param idx индекс карточки
     */
    private void hideCard(int idx) {
        cards[idx].setText("?");
        cards[idx].setStyle(hiddenStyle());
    }

    /**
     * Завершает игру с результатом.
     *
     * @param won     {@code true} если игрок нашёл все пары
     * @param message текст результата
     */
    private void endGame(boolean won, String message) {
        inputLocked = true;
        statusLabel.setStyle("-fx-text-fill: " + (won ? "#7ab87a" : "#c47a7a") +
                             "; -fx-font-size: 15; -fx-font-weight: bold;");
        statusLabel.setText(message);
        for (Button card : cards) card.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> complete(won ? PanelResult.success() : PanelResult.failure()));
        pause.play();
    }

    /**
     * Форматирует строку с оставшимися попытками.
     *
     * @return строка вида «Попыток осталось: N»
     */
    private String formatAttempts() {
        return "Попыток осталось: " + attemptsLeft;
    }

    private static String hiddenStyle() {
        return "-fx-background-color: #4a3228; -fx-border-color: rgba(180, 140, 100, 0.5); -fx-border-width: 2;" +
               "-fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-text-fill: #c4a882; -fx-font-size: 24; -fx-font-weight: bold; -fx-cursor: hand;";
    }

    private static String revealedStyle() {
        return "-fx-background-color: #6b4a3a; -fx-border-color: #d2aa78; -fx-border-width: 2;" +
               "-fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-text-fill: #f5e6d0; -fx-font-size: 24; -fx-font-weight: bold;";
    }

    private static String matchedStyle() {
        return "-fx-background-color: #3a5a3a; -fx-border-color: #7ab87a; -fx-border-width: 2;" +
               "-fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-text-fill: #b8e0b8; -fx-font-size: 24; -fx-font-weight: bold;";
    }
}
