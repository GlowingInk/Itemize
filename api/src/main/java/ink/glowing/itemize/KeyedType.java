package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyedValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface KeyedType<T> extends KeyedValue<Class<T>> {
    static <T> @NotNull KeyedType<T> keyedType(@NotNull Key key, @NotNull Class<T> type) {
        return new Impl<>(key, type);
    }

    static <T> @NotNull KeyedType<T> keyedType(@NotNull KeyedValue<Class<T>> keyedValue) {
        return new Impl<>(keyedValue.key(), keyedValue.value());
    }

    @NotNull Class<T> type();

    @Deprecated @Override
    default @NotNull Class<T> value() {
        return type();
    }

    @ApiStatus.Internal
    record Impl<T>(@NotNull Key key, @NotNull Class<T> type) implements KeyedType<T> { }
}
