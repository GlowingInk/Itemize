package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.File;
import java.util.logging.Logger;

public interface Itemize {
    String NAMESPACE = "itemize";

    void reloadAll() throws ConfigurateException;

    @NotNull File prepareFile(@NotNull String name, boolean resource);

    boolean hasChief(@NotNull Class<?> type);

    <T> @NotNull ResolvingChief<T> getChief(@NotNull Class<T> type);

    @Contract("_, true -> !null")
    <T> @Nullable ResolvingChief<T> getChief(@NotNull Class<T> type, boolean create);

    @NotNull Logger logger();

    static @NotNull Key itemizeKey(@KeyPattern.Value @NotNull String value) {
        return Key.key(NAMESPACE, value);
    }
}
