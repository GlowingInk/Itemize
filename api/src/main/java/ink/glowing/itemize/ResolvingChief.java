package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface ResolvingChief<T> {
    void reloadResolvers(@NotNull Itemize itemize) throws ConfigurateException;

    /**
     * Register a new resolver. {@link Key#MINECRAFT_NAMESPACE} is not allowed
     * @param resolver resolver to register
     * @return is registration was successful or not
     */
    boolean addResolver(@NotNull Resolver<T> resolver);

    @Nullable
    Resolver<T> getResolver(@NotNull String keyStr);

    @Nullable
    Resolver<T> getResolver(@NotNull Key key);

    default boolean hasResolver(@NotNull String keyStr) {
        return getResolver(keyStr) != null;
    }

    default boolean hasResolver(@NotNull Key key) {
        return getResolver(key) != null;
    }

    default @Nullable T resolve(@NotNull KeyedValue<String> keyedValue) {
        return resolve(keyedValue.key(), keyedValue.value());
    }

    default @Nullable T resolve(@NotNull String fullValue) {
        String[] split = fullValue.split(" ", 2);
        return resolve(split[0], split.length > 1 ? split[1] : "");
    }

    @Nullable T resolve(@NotNull String keyStr, @NotNull String params);

    @Nullable T resolve(@NotNull Key key, @NotNull String params);

    @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull KeyedValue<String> keyedValue);

    @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull String keyStr, @NotNull String params);

    @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull Key key, @NotNull String params);

    int resolversCount();

    void forEachResolver(@NotNull BiConsumer<@NotNull Key, @NotNull Resolver<T>> action);
}
