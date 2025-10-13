package ink.glowing.itemize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.function.Supplier;

/**
 * A {@link String}-to-object ({@link T}) resolver.
 * @param <T> the type to resolve into
 */
@FunctionalInterface
public interface Resolver<T> {
    /**
     * A simple {@code null} supplier.
     */
    Supplier<?> EMPTY_SUPPLIER = () -> null;

    /**
     * Reload this {@link Resolver<T>} instance.
     * Default implementations is no-op. Implementations are free to throw any {@link RuntimeException}s.
     * @param chief the chief, which has this {@link Resolver<T>} registered
     * @throws ConfigurateException on invalid configuration
     */
    default void reload(@NotNull ResolvingChief<T> chief) throws ConfigurateException { }

    /**
     * Resolve {@link String} into an object {@link T}.
     * @param params the parameters
     * @return the generated object {@link T}
     */
    @Nullable T resolve(@NotNull String params);

    /**
     * Turn {@link Resolver<T>} into a {@link Supplier<T>} with predefined parameters.
     * Default implementations calls {@link Resolver#resolve(String)} on every {@link Supplier#get()} call.
     * @param params the parameters
     * @return the generating {@link Supplier<T>}
     */
    default @NotNull Supplier<@Nullable T> asSuppler(@NotNull String params) {
        return () -> resolve(params);
    }

    /**
     * A simple {@code null} supplier with generic type.
     * @param <T> returning type of a {@link Supplier}
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull Supplier<@Nullable T> emptySuppler() {
        return (Supplier<T>) EMPTY_SUPPLIER;
    }
}
