package ink.glowing.itemize;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.function.Supplier;

/**
 * A {@link String}-to-object ({@link T}) resolver.
 * @param <T> the type to resolve into
 */
public interface Resolver<T> extends Keyed {
    /**
     * A simple {@code null} supplier.
     */
    Supplier<?> EMPTY_SUPPLIER = () -> null;

    /**
     * Reload this {@link Resolver<T>} instance.
     * Default implementations does nothing.
     * @param core the main {@link Itemize} instance
     * @param chief the chief, which has this {@link Resolver<T>} registered
     * @throws ConfigurateException on invalid configuration
     */
    default void reload(@NotNull Itemize core, @NotNull ResolvingChief<T> chief) throws ConfigurateException { }

    /**
     * Resolve {@link String} into a object {@link T}.
     * @param params the parameters
     * @return the generated object {@link T}
     */
    @Nullable T resolve(@NotNull String params);

    /**
     * Turn {@link Resolver<T>} into a {@link Supplier<T>} with predefined parameters.
     * @param params the parameters
     * @return the generating {@link Supplier<T>}
     */
    default @NotNull Supplier<@Nullable T> asSuppler(@NotNull String params) {
        return () -> resolve(params);
    }

    /**
     * A simple {@code null} supplier with generic type.
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull Supplier<@Nullable T> emptySuppler() {
        return (Supplier<T>) EMPTY_SUPPLIER;
    }
}
