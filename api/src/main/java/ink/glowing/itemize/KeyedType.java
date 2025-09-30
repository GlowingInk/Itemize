package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyedValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Class} with an associated {@link Key}. Everything is expected to be unmodifiable.
 * @param <T> the class value type
 * @param key the key associated with the type
 * @param type the class type
 */
public record KeyedType<T>(@NotNull Key key, @NotNull Class<T> type) implements KeyedValue<Class<T>> {
    /**
     * Creates a new {@link KeyedType} instance from an existing {@link KeyedValue}.
     * @param keyedValue the existing keyed value
     */
    public KeyedType(@NotNull KeyedValue<Class<T>> keyedValue) {
        this(keyedValue.key(), keyedValue.value());
    }

    /**
     * Gets the value associated with this {@link KeyedType}.
     * This method is deprecated and will return the class type.
     * @return the class type
     * @deprecated use {@link KeyedType#type()} instead
     */
    @Deprecated
    @Override
    public @NotNull Class<T> value() {
        return type();
    }

    /**
     * Checks whether the provided {@link KeyedValue} is equal to this one,
     * without explicit object class check
     * @param other the other keyed value
     * @return is the provided {@link KeyedValue} equal to this one
     */
    @Contract("null -> false")
    public boolean isSimilar(@Nullable KeyedValue<Class<T>> other) {
        return other != null && key().equals(other.key()) && type().equals(other.value());
    }
}