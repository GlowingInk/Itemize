package ink.glowing.itemize.util.random.pool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public record SingletonPool<T>(@Nullable T value) implements RandomPool<T> {
    public static final SingletonPool<?> EMPTY = new SingletonPool<>(null);

    public static <T> @NotNull SingletonPool<T> emptyPool() {
        //noinspection unchecked
        return (SingletonPool<T>) SingletonPool.EMPTY;
    }

    @Override
    public T next(@NotNull RandomGenerator rng) {
        return value;
    }

    @Override
    public @NotNull Stream<@Nullable T> stream(@NotNull RandomGenerator rng) {
        return Stream.generate(() -> value);
    }

    @Override
    public @NotNull Stream<T> stream(@NotNull Supplier<@NotNull RandomGenerator> rngSupplier) {
        return Stream.generate(() -> value);
    }
}
