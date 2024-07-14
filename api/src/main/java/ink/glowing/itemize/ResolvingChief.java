package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A chief responsible for managing and resolving {@link Resolver} instances.
 * @param <T> the type that the resolvers resolve into
 */
public interface ResolvingChief<T> {
    /**
     * Reload all registered resolvers.
     * @param itemize the main {@link Itemize} instance
     * @throws ConfigurateException if reloading fails
     */
    void reloadResolvers(@NotNull Itemize itemize) throws ConfigurateException;

    /**
     * Add a resolver to this chief.
     * @param resolver the resolver to add
     * @return {@code true} if the resolver was added successfully, {@code false} otherwise
     */
    boolean addResolver(@NotNull Resolver<T> resolver);

    /**
     * Get a resolver by its string key.
     * @param keyStr the string key of the resolver
     * @return the resolver, or {@code null} if not found
     */
    @Nullable Resolver<T> getResolver(@NotNull String keyStr);

    /**
     * Get a resolver by its {@link Key}.
     * @param key the key of the resolver
     * @return the resolver, or {@code null} if not found
     */
    @Nullable Resolver<T> getResolver(@NotNull Key key);

    /**
     * Check if a resolver exists by its string key.
     * @param keyStr the string key to check
     * @return {@code true} if a resolver exists with the given key, {@code false} otherwise
     */
    boolean hasResolver(@NotNull String keyStr);

    /**
     * Check if a resolver exists by its {@link Key}.
     * @param key the key to check
     * @return {@code true} if a resolver exists with the given key, {@code false} otherwise
     */
    boolean hasResolver(@NotNull Key key);

    /**
     * Resolve a value using the specified full string.
     * The string is split into a key and parameters.
     * @param fullValue the full value to resolve
     * @return the resolved value, or {@code null} if resolution fails
     */
    default @Nullable T resolve(@NotNull String fullValue) {
        String[] split = fullValue.split(" ", 2);
        return resolve(split[0], split.length > 1 ? split[1] : "");
    }

    /**
     * Resolve a value using the specified {@link KeyedValue}.
     * @param keyedValue the keyed value to resolve
     * @return the resolved value, or {@code null} if resolution fails
     */
    default @Nullable T resolve(@NotNull KeyedValue<String> keyedValue) {
        return resolve(keyedValue.key(), keyedValue.value());
    }

    /**
     * Resolve a value using the specified key and parameters.
     * @param keyStr the string key
     * @param params the parameters
     * @return the resolved value, or {@code null} if resolution fails
     */
    @Nullable T resolve(@NotNull String keyStr, @NotNull String params);

    /**
     * Resolve a value using the specified key and parameters.
     * @param key the key
     * @param params the parameters
     * @return the resolved value, or {@code null} if resolution fails
     */
    @Nullable T resolve(@NotNull Key key, @NotNull String params);

    /**
     * Get a resolving function for the specified string key.
     * @param keyStr the string key
     * @return the resolving function, or {@code null} if no resolver is found
     */
    @Nullable Function<@NotNull String, @Nullable T> resolvingFunction(@NotNull String keyStr);

    /**
     * Get a resolving function for the specified key.
     * @param key the key
     * @return the resolving function, or {@code null} if no resolver is found
     */
    @Nullable Function<@NotNull String, @Nullable T> resolvingFunction(@NotNull Key key);

    /**
     * Get a resolving supplier for the specified full value.
     * The value is split into a key and parameters.
     * @param fullValue the full value to resolve
     * @return the resolving supplier, or {@code null} if no resolver is found
     */
    default @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull String fullValue) {
        String[] split = fullValue.split(" ", 2);
        return resolvingSupplier(split[0], split.length > 1 ? split[1] : "");
    }

    /**
     * Get a resolving supplier for the specified {@link KeyedValue}.
     * @param keyedValue the keyed value to resolve
     * @return the resolving supplier, or {@code null} if no resolver is found
     */
    default @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull KeyedValue<String> keyedValue) {
        return resolvingSupplier(keyedValue.key(), keyedValue.value());
    }

    /**
     * Get a resolving supplier for the specified key and parameters.
     * @param keyStr the string key
     * @param params the parameters
     * @return the resolving supplier, or {@code null} if no resolver is found
     */
    @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull String keyStr, @NotNull String params);

    /**
     * Get a resolving supplier for the specified key and parameters.
     * @param key the key
     * @param params the parameters
     * @return the resolving supplier, or {@code null} if no resolver is found
     */
    @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull Key key, @NotNull String params);

    /**
     * Get the number of registered resolvers.
     * @return the number of resolvers
     */
    int resolversCount();

    /**
     * Perform an action for each registered resolver.
     * @param action the action to perform
     */
    void forEachResolver(@NotNull BiConsumer<@NotNull Key, @NotNull Resolver<T>> action);
}

