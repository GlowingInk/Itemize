package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyedValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Class} with an associated {@link Key}.
 * @param <T> the class value type
 */
public interface KeyedType<T> extends KeyedValue<Class<T>> {
    /**
     * Creates a new {@link KeyedType} instance with the specified key and type.
     * @param key the key associated with the type
     * @param type the class type
     * @param <T> the class value type
     * @return a new {@link KeyedType} instance
     */
    static <T> @NotNull KeyedType<T> keyedType(@NotNull Key key, @NotNull Class<T> type) {
        return new Impl<>(key, type);
    }

    /**
     * Creates a new {@link KeyedType} instance from an existing {@link KeyedValue}.
     * @param keyedValue the existing keyed value
     * @param <T> the class value type
     * @return a new {@link KeyedType} instance
     */
    static <T> @NotNull KeyedType<T> keyedType(@NotNull KeyedValue<Class<T>> keyedValue) {
        return new Impl<>(keyedValue.key(), keyedValue.value());
    }

    /**
     * Gets the class type associated with this {@link KeyedType}.
     * @return the class type
     */
    @NotNull Class<T> type();

    /**
     * Gets the value associated with this {@link KeyedType}.
     * This method is deprecated and will return the class type.
     * @return the class type
     * @deprecated use {@link #type()} instead
     */
    @Deprecated @Override
    default @NotNull Class<T> value() {
        return type();
    }

    /**
     * Internal implementation of {@link KeyedType}.
     * @param key the key associated with the type
     * @param type the class type
     * @param <T> the class value type
     */
    @ApiStatus.Internal
    record Impl<T>(@NotNull Key key, @NotNull Class<T> type) implements KeyedType<T> { }
}