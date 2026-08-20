package kz.aws.game.utils;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Пометка модальных overlay-панелей (меню паузы, сохранения, журнал, паззлы).
 * Позволяет определить, открыта ли панель поверх игры, не полагаясь на
 * количество детей root — туда попадают ещё слой сцены и визуальные эффекты.
 */
public final class OverlayMarker {

    /** CSS-класс-маркер overlay-панели. */
    private static final String OVERLAY_CLASS = "game-overlay";

    private OverlayMarker() {
    }

    /**
     * Помечает узел как overlay-панель.
     *
     * @param node узел панели
     */
    public static void mark(Node node) {
        if (node != null && !node.getStyleClass().contains(OVERLAY_CLASS)) {
            node.getStyleClass().add(OVERLAY_CLASS);
        }
    }

    /**
     * Проверяет, открыта ли хотя бы одна помеченная overlay-панель.
     *
     * @param root корневой StackPane сцены (null — считается, что панелей нет)
     * @return true — есть открытая overlay-панель
     */
    public static boolean hasOpenOverlay(StackPane root) {
        if (root == null) return false;
        return root.getChildren().stream()
                .anyMatch(child -> child.getStyleClass().contains(OVERLAY_CLASS));
    }
}
