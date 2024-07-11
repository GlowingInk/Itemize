package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.File;
import java.util.logging.Logger;

import static ink.glowing.itemize.KeyedType.keyedType;

public interface Itemize {
    String NAMESPACE = "itemize";
    Key DEFAULT_CHIEF_KEY = itemizeKey("default");

    void reloadAll() throws ConfigurateException;

    @NotNull File prepareFile(@NotNull String name, boolean resource);

    default boolean hasChief(@NotNull Class<?> type) {
        return hasKeyedChief(keyedType(DEFAULT_CHIEF_KEY, type));
    }

    default boolean hasKeyedChief(@NotNull Key key, @NotNull Class<?> type) {
        return hasKeyedChief(keyedType(key, type));
    }

    boolean hasKeyedChief(@NotNull KeyedType<?> typedKey);

    default <T> void enforceChief(@NotNull Key key, @NotNull Class<T> type, @NotNull ResolvingChief<T> chief) {
        enforceChief(keyedType(key, type), chief);
    }

    <T> void enforceChief(@NotNull KeyedType<T> keyedType, @NotNull ResolvingChief<T> chief);

    default <T> @NotNull ResolvingChief<T> getChief(@NotNull Class<T> type) {
        return getKeyedChief(DEFAULT_CHIEF_KEY, type);
    }

    default <T> @NotNull ResolvingChief<T> getKeyedChief(@NotNull Key key, @NotNull Class<T> type) {
        return getKeyedChief(keyedType(key, type));
    }

    <T> @NotNull ResolvingChief<T> getKeyedChief(@NotNull KeyedType<T> keyedType);

    @Contract("_, true -> !null")
    default <T> @Nullable ResolvingChief<T> getChief(@NotNull Class<T> type, boolean create) {
        return getKeyedChief(DEFAULT_CHIEF_KEY, type, create);
    }

    @Contract("_, _, true -> !null")
    default <T> @Nullable ResolvingChief<T> getKeyedChief(@NotNull Key key, @NotNull Class<T> type, boolean create) {
        return getKeyedChief(keyedType(key, type), create);
    }

    @Contract("_, true -> !null")
    <T> @Nullable ResolvingChief<T> getKeyedChief(@NotNull KeyedType<T> keyedType, boolean create);

    @NotNull Logger logger();

    static @NotNull Key itemizeKey(@KeyPattern.Value @NotNull String value) {
        return Key.key(NAMESPACE, value);
    }
}
