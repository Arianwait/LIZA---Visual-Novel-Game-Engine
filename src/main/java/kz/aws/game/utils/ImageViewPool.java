package kz.aws.game.utils;

import javafx.scene.image.ImageView;

/**
 * Специализированный пул для ImageView объектов
 * Экономит память за счет переиспользования ImageView вместо создания новых
 */
public class ImageViewPool extends ObjectPool<ImageView> {
    
    private static final int DEFAULT_POOL_SIZE = 10; // Максимум 10 ImageView в пуле
    
    private static ImageViewPool instance;
    
    /**
     * Получить экземпляр пула (Singleton)
     */
    public static synchronized ImageViewPool getInstance() {
        if (instance == null) {
            instance = new ImageViewPool();
        }
        return instance;
    }
    
    /**
     * Создать новый пул ImageView
     */
    private ImageViewPool() {
        super(
            ImageView::new, // Фабрика для создания новых ImageView
            DEFAULT_POOL_SIZE, // Максимальный размер пула
            ImageViewPool::resetImageView // Функция сброса состояния
        );
    }
    
    /**
     * Сброс состояния ImageView перед возвратом в пул
     * 
     * @param imageView ImageView для сброса
     */
    private static void resetImageView(ImageView imageView) {
        if (imageView == null) return;
        
        // ВАЖНО: Сначала отвязываем все свойства, чтобы избежать RuntimeException
        // при попытке установить значения для привязанных свойств
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();
        imageView.imageProperty().unbind();
        
        // Сбрасываем все свойства ImageView
        imageView.setImage(null);
        imageView.setVisible(false);
        imageView.setOpacity(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setRotate(0);
        imageView.setFitWidth(0);
        imageView.setFitHeight(0);
        imageView.setPreserveRatio(true);
        
        // Удаляем все эффекты
        imageView.setEffect(null);
    }
    
    /**
     * Получить ImageView из пула с базовыми настройками
     * 
     * @return Настроенный ImageView
     */
    public ImageView acquireConfigured() {
        ImageView view = acquire();
        view.setPreserveRatio(true);
        return view;
    }
    
    /**
     * Очистить пул (освободить все объекты)
     * Вызывать при завершении работы приложения
     */
    public static void clearPool() {
        if (instance != null) {
            instance.clear();
        }
    }
    
    /**
     * Получить статистику пула (для отладки)
     * 
     * @return Количество объектов в пуле
     */
    public static int getPoolSize() {
        return instance != null ? instance.size() : 0;
    }
}
