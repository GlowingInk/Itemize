package ink.glowing.itemize.util.random.pool;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

@FunctionalInterface
public interface RandomPool<T> {
    T next(@NotNull RandomGenerator rng);

    default @NotNull Stream<T> stream(@NotNull RandomGenerator rng) {
        return Stream.generate(() -> next(rng));
    }

    default @NotNull Stream<T> stream(@NotNull Supplier<@NotNull RandomGenerator> rngSupplier) {
        return Stream.generate(() -> next(rngSupplier.get()));
    }
}
