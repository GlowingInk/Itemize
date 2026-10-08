package ink.glowing.itemize.util.random.pool;

import ink.glowing.itemize.util.random.RngUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.random.RandomGenerator;

import static ink.glowing.itemize.util.random.pool.SingletonPool.emptyPool;

public final class UniformPool<T> implements RandomPool<T> {
    private final List<T> elements;

    public UniformPool(@NotNull Collection<@Nullable T> elements) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    public static  <T> @NotNull RandomPool<T> uniformPool(@NotNull Iterable<T> iterable) {
        if (iterable instanceof Collection<T> collection) {
            return uniformPool(collection);
        } else {
            List<T> elements = new ArrayList<>();
            for (T item : iterable) elements.add(item);
            return new UniformPool<>(elements);
        }
    }

    public static <T> @NotNull RandomPool<T> uniformPool(@NotNull Collection<T> collection) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> new SingletonPool<>(collection.iterator().next());
            default -> new UniformPool<>(collection);
        };
    }
    public static <T> @NotNull RandomPool<T> uniformPool(@NotNull SequencedCollection<T> collection) {
        return switch (collection.size()) {
            case 0 -> emptyPool();
            case 1 -> new SingletonPool<>(collection.getFirst());
            default -> new UniformPool<>(collection);
        };
    }

    @Override
    public T next(@NotNull RandomGenerator rng) {
        return RngUtils.next(rng, elements);
    }
}
