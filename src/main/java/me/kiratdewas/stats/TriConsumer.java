package me.kiratdewas.stats;

@FunctionalInterface()
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v) throws Exception;
}
