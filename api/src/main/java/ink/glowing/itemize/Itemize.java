package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static ink.glowing.itemize.KeyedType.keyedType;

/**
 * {@link ResolvingChief} registry and the core of Itemize API
 */
public interface Itemize {
    /**
     * Itemize namespace
     */
    String NAMESPACE = "itemize";

    /**
     * Default key when no key is selected
     */
    Key DEFAULT_CHIEF_KEY = itemizeKey("default");

    /**
     * Reload all registered {@link ResolvingChief}s
     * @throws ConfigurateException when fails to reload
     */
    void reloadAll() throws ConfigurateException;

    /**
     * Creates a file (if one doesn't exist) in the data folder
     * @param name file name
     * @param resource should file be loaded from the jar
     * @return created or existing file
     * @see Itemize#getDataFolder()
     */
    @NotNull File prepareFile(@NotNull String name, boolean resource) throws IOException;

    /**
     * Get the default data folder
     * @return default data folder
     */
    @NotNull File getDataFolder();

    /**
     * Check if this {@link Itemize} instance contains {@link ResolvingChief} of specified type
     * with the default key
     * @param type type to check for
     * @see Itemize#DEFAULT_CHIEF_KEY
     */
    default boolean hasChief(@NotNull Class<?> type) {
        return hasKeyedChief(keyedType(DEFAULT_CHIEF_KEY, type));
    }

    /**
     * Check if this {@link Itemize} instance contains {@link ResolvingChief} of specified key-type pair
     * @param key key to check for
     * @param type type to check for
     */
    default boolean hasKeyedChief(@NotNull Key key, @NotNull Class<?> type) {
        return hasKeyedChief(keyedType(key, type));
    }

    /**
     * Check if this {@link Itemize} instance contains {@link ResolvingChief} of specified keyed type
     * @param keyedType key-type pair to check for
     */
    boolean hasKeyedChief(@NotNull KeyedType<?> keyedType);

    /**
     * Get the {@link ResolvingChief} of the specified type with the default key
     * @param type type of the chief
     * @param <T> type parameter
     * @return the resolving chief
     * @see Itemize#DEFAULT_CHIEF_KEY
     */
    default <T> @NotNull ResolvingChief<T> getChief(@NotNull Class<T> type) {
        return getKeyedChief(DEFAULT_CHIEF_KEY, type);
    }

    /**
     * Get the {@link ResolvingChief} of the specified key-type pair
     * @param key key of the chief
     * @param type type of the chief
     * @param <T> type parameter
     * @return the resolving chief
     */
    default <T> @NotNull ResolvingChief<T> getKeyedChief(@NotNull Key key, @NotNull Class<T> type) {
        return getKeyedChief(keyedType(key, type));
    }

    /**
     * Get the {@link ResolvingChief} of the specified keyed type
     * @param keyedType key-type pair of the chief
     * @param <T> type parameter
     * @return the resolving chief
     */
    <T> @NotNull ResolvingChief<T> getKeyedChief(@NotNull KeyedType<T> keyedType);

    /**
     * Get the {@link ResolvingChief} of the specified type with the default key, optionally creating it if it doesn't exist
     * @param type type of the chief
     * @param create whether to create the chief if it doesn't exist
     * @param <T> type parameter
     * @return the resolving chief, or {@code null} if not found and {@code create} is {@code false}
     * @see Itemize#DEFAULT_CHIEF_KEY
     */
    @Contract("_, true -> !null")
    default <T> @Nullable ResolvingChief<T> getChief(@NotNull Class<T> type, boolean create) {
        return getKeyedChief(DEFAULT_CHIEF_KEY, type, create);
    }

    /**
     * Get the {@link ResolvingChief} of the specified key-type pair, optionally creating it if it doesn't exist
     * @param key key of the chief
     * @param type type of the chief
     * @param create whether to create the chief if it doesn't exist
     * @param <T> type parameter
     * @return the resolving chief, or {@code null} if not found and {@code create} is {@code false}
     */
    @Contract("_, _, true -> !null")
    default <T> @Nullable ResolvingChief<T> getKeyedChief(@NotNull Key key, @NotNull Class<T> type, boolean create) {
        return getKeyedChief(keyedType(key, type), create);
    }

    /**
     * Get the {@link ResolvingChief} of the specified keyed type, optionally creating it if it doesn't exist
     * @param keyedType key-type pair of the chief
     * @param create whether to create the chief if it doesn't exist
     * @param <T> type parameter
     * @return the resolving chief, or {@code null} if not found and {@code create} is {@code false}
     */
    @Contract("_, true -> !null")
    <T> @Nullable ResolvingChief<T> getKeyedChief(@NotNull KeyedType<T> keyedType, boolean create);

    /**
     * Enforce the specified {@link ResolvingChief} for the given key and type.
     * If there was a previously existing chief, its resolvers will migrate onto a new one,
     * and the old one will be returned.
     * @param key key of the chief
     * @param type type of the chief
     * @param chief resolving chief to enforce
     * @param <T> type parameter
     * @return the previously existing chief, or {@code null} otherwise
     */
    default <T> @Nullable ResolvingChief<T> enforceChief(@NotNull Key key, @NotNull Class<T> type, @NotNull ResolvingChief<T> chief) {
        return enforceChief(keyedType(key, type), chief);
    }

    /**
     * Enforce the specified {@link ResolvingChief} for the given keyed type.
     * If there was a previously existing chief, its resolvers will migrate onto a new one,
     * and the old one will be returned.
     * @param keyedType key-type pair of the chief
     * @param chief resolving chief to enforce
     * @param <T> type parameter
     * @return the previously existing chief, or {@code null} otherwise
     */
    <T> @Nullable ResolvingChief<T> enforceChief(@NotNull KeyedType<T> keyedType, @NotNull ResolvingChief<T> chief);

    /**
     * Get the logger of this {@link Itemize} instance
     * @return the logger
     */
    @NotNull Logger getLogger();

    /**
     * Create an itemize key with the specified value.
     * Normally you will find no reason to use this method.
     * @param value value of the key
     * @return the created key
     * @see Itemize#NAMESPACE
     */
    @ApiStatus.Internal
    static @NotNull Key itemizeKey(@KeyPattern.Value @NotNull String value) {
        return Key.key(NAMESPACE, value);
    }
}
