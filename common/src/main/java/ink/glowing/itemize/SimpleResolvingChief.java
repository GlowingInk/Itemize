package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleResolvingChief<T> implements ResolvingChief<T> {
    protected final Map<Key, Resolver<T>> resolversMap;
    protected final Map<String, Key> aliases;

    public SimpleResolvingChief() {
        this.resolversMap = new ConcurrentHashMap<>();
        this.aliases = new ConcurrentHashMap<>();
    }

    @Override
    public void reloadResolvers(@NotNull Itemize itemize) throws ConfigurateException {
        for (var entry : resolversMap.entrySet()) {
            entry.getValue().reload(itemize, this);
        }
    }

    @Override
    public boolean addResolver(@NotNull Resolver<T> resolver) {
        Key key = resolver.key();
        if (key.namespace().equals(Key.MINECRAFT_NAMESPACE) || resolversMap.putIfAbsent(key, resolver) != null) {
            return false;
        }
        aliases.put(key.toString(), key);
        aliases.putIfAbsent(key.value(), key);
        return true;
    }

    @Override
    public @Nullable Resolver<T> getResolver(@NotNull String keyStr) {
        Key key = aliases.get(keyStr);
        return key != null ? getResolver(key) : null;
    }

    @Override
    public @Nullable Resolver<T> getResolver(@NotNull Key key) {
        return resolversMap.get(key);
    }

    @Override
    public boolean hasResolver(@NotNull String keyStr) {
        return aliases.containsKey(keyStr);
    }

    @Override
    public boolean hasResolver(@NotNull Key key) {
        return resolversMap.containsKey(key);
    }

    @Override
    public @Nullable T resolve(@NotNull String keyStr, @NotNull String params) {
        return resolve(getResolver(keyStr), params);
    }

    @Override
    public @Nullable T resolve(@NotNull Key key, @NotNull String params) {
        return resolve(getResolver(key), params);
    }

    @Override
    public @Nullable Function<@NotNull String, @Nullable T> resolvingFunction(@NotNull String keyStr) {
        return resolvingFunction(getResolver(keyStr));
    }

    @Override
    public @Nullable Function<@NotNull String, @Nullable T> resolvingFunction(@NotNull Key key) {
        return resolvingFunction(getResolver(key));
    }

    private @Nullable Function<@NotNull String, @Nullable T> resolvingFunction(@Nullable Resolver<T> resolver) {
        return resolver != null ? resolver::resolve : null;
    }

    private @Nullable T resolve(@Nullable Resolver<T> resolver, @NotNull String params) {
        return resolver != null ? resolver.resolve(params) : null;
    }

    @Override
    public @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull KeyedValue<String> keyedValue) {
        return resolvingSupplier(keyedValue.key(), keyedValue.value());
    }

    @Override
    public @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull String keyStr, @NotNull String params) {
        return resolvingSupplier(getResolver(keyStr), params);
    }

    @Override
    public @Nullable Supplier<@Nullable T> resolvingSupplier(@NotNull Key key, @NotNull String params) {
        return resolvingSupplier(getResolver(key), params);
    }

    private @Nullable Supplier<@Nullable T> resolvingSupplier(@Nullable Resolver<T> resolver, @NotNull String params) {
        return resolver != null ? resolver.asSuppler(params) : null;
    }

    @Override
    public int resolversCount() {
        return resolversMap.size();
    }

    @Override
    public void forEachResolver(@NotNull BiConsumer<@NotNull Key, @NotNull Resolver<T>> action) {
        resolversMap.forEach(action);
    }
}
