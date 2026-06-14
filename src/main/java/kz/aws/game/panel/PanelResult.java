package kz.aws.game.panel;

/**
 * Результат выполнения игровой панели.
 *
 * <p>Вызывается через {@link BaseGamePanel#complete(PanelResult)}.
 * После вызова система:
 * <ul>
 *   <li>записывает флаг {@code flag = isSuccess()} в {@code SceneController};</li>
 *   <li>если {@code getData()} не пуст — сохраняет строку как {@code flag_data};</li>
 *   <li>переходит на {@code onSuccess} / {@code onFailure} сцену (если заданы).</li>
 * </ul>
 *
 * <p>Примеры:
 * <pre>
 *   complete(PanelResult.success());
 *   complete(PanelResult.failure());
 *   complete(PanelResult.withData(true, "knife"));   // нашли улику
 *   complete(PanelResult.withData(false, "missed")); // не нашли
 *   complete(PanelResult.close());                   // просто закрыть, без флага
 * </pre>
 */
public final class PanelResult {

    private final boolean success;
    private final String data;

    private PanelResult(boolean success, String data) {
        this.success = success;
        this.data = data != null ? data : "";
    }

    /** Панель завершена успешно. */
    public static PanelResult success() {
        return new PanelResult(true, "");
    }

    /** Панель завершена с провалом / игрок не справился. */
    public static PanelResult failure() {
        return new PanelResult(false, "");
    }

    /**
     * Результат с дополнительными данными (найденная улика, введённый код и т.д.).
     *
     * @param success {@code true} если задача выполнена
     * @param data    строка, которая сохранится как {@code flag_data} в playerChoices
     */
    public static PanelResult withData(boolean success, String data) {
        return new PanelResult(success, data);
    }

    /**
     * Просто закрыть панель без записи флага.
     * Флаг не изменится, переход на другую сцену не произойдёт.
     * Используй когда панель — только информационная (осмотр, карта и т.д.).
     */
    public static PanelResult close() {
        return new PanelResult(false, "__close__");
    }

    public boolean isSuccess() { return success; }
    public String getData()    { return data; }

    /** {@code true} если это просто закрытие без записи результата. */
    public boolean isCloseOnly() { return "__close__".equals(data); }

    @Override
    public String toString() {
        return "PanelResult{success=" + success + ", data='" + data + "'}";
    }
}
