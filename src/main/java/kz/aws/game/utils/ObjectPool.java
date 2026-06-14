package kz.aws.game.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Универсальный пул объектов (Object Pool Pattern)
 * Экономит память за счет переиспользования объектов вместо создания новых
 * 
 * @param <T> Тип объектов в пуле
 */
public class ObjectPool<T> {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> factory;
    private final int maxSize;
    private final PoolObjectResetter<T> resetter;
    
    /**
     * Интерфейс для сброса состояния объекта перед возвратом в пул
     */
    @FunctionalInterface
    public interface PoolObjectResetter<T> {
        void reset(T obj);
    }
    
    /**
     * Создает пул объектов
     * 
     * @param factory Фабрика для создания новых объектов
     * @param maxSize Максимальный размер пула (0 = без ограничений)
     * @param resetter Функция для сброса состояния объекта перед возвратом в пул
     */
    public ObjectPool(Supplier<T> factory, int maxSize, PoolObjectResetter<T> resetter) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.resetter = resetter != null ? resetter : obj -> {}; // По умолчанию ничего не делаем
    }
    
    /**
     * Создает пул без ограничения размера
     */
    public ObjectPool(Supplier<T> factory, PoolObjectResetter<T> resetter) {
        this(factory, 0, resetter);
    }
    
    /**
     * Получить объект из пула или создать новый
     * 
     * @return Объект из пула
     */
    public T acquire() {
        T obj = pool.poll();
        if (obj == null) {
            obj = factory.get();
        }
        return obj;
    }
    
    /**
     * Вернуть объект в пул для переиспользования
     * 
     * @param obj Объект для возврата в пул
     */
    public void release(T obj) {
        if (obj == null) return;
        
        // Сбрасываем состояние объекта
        resetter.reset(obj);
        
        // Добавляем в пул, если есть место
        if (maxSize <= 0 || pool.size() < maxSize) {
            pool.offer(obj);
        }
        // Если пул переполнен, объект просто будет удален сборщиком мусора
    }
    
    /**
     * Очистить пул
     */
    public void clear() {
        pool.clear();
    }
    
    /**
     * Получить текущий размер пула
     * 
     * @return Количество объектов в пуле
     */
    public int size() {
        return pool.size();
    }
}
