package ink.glowing.itemize;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleItemize implements Itemize {
    protected final Map<Class<?>, Map<Key, ResolvingChief<?>>> chiefs;
    protected final Itemize.Platform platform;

    public SimpleItemize(@NotNull Platform platform) {
        this.platform = platform;
        this.chiefs = new ConcurrentHashMap<>();
    }

    @Override
    public void reload() throws ConfigurateException {
        List<Exception> exceptions = new ArrayList<>();
        for (var typedChiefs : chiefs.entrySet()) {
            for (var chief : typedChiefs.getValue().entrySet()) {
                try {
                    chief.getValue().reload();
                } catch (Exception ex) {
                    exceptions.add(ex);
                }
            }
        }
        if (!exceptions.isEmpty()) {
            ConfigurateException parentEx = new ConfigurateException("Failed to reload one or multiple Chiefs");
            exceptions.forEach(parentEx::addSuppressed);
            throw parentEx;
        }
    }

    @Override
    public boolean hasKeyedChief(@NotNull Key key, @NotNull Class<?> type) {
        var typedChiefs = chiefs.get(type);
        return typedChiefs != null && typedChiefs.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> ResolvingChief<T> enforceChief(@NotNull Key key, @NotNull Class<T> type, @NotNull ResolvingChief<T> chief) {
        var typedChiefs = chiefs.computeIfAbsent(type, _ -> new ConcurrentHashMap<>());
        ResolvingChief<T> oldChief = (ResolvingChief<T>) typedChiefs.get(key);
        if (oldChief != null) {
            oldChief.forEachResolver((_, resolver) -> chief.addResolver(resolver));
        }
        typedChiefs.put(key, chief);
        return oldChief;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> ResolvingChief<T> getKeyedChief(@NotNull Key key, @NotNull Class<T> type) {
        return (ResolvingChief<T>) chiefs
                .computeIfAbsent(type, _ -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, _ -> new SimpleResolvingChief<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> ResolvingChief<T> getKeyedChief(@NotNull Key key, @NotNull Class<T> type, boolean create) {
        Map<Key, ResolvingChief<?>> typeMap = chiefs.get(type);

        if (typeMap == null) {
            return create ? getKeyedChief(key, type) : null;
        }

        ResolvingChief<?> chief = typeMap.get(key);
        return chief != null
                ? (ResolvingChief<T>) chief
                : (create ? getKeyedChief(key, type) : null);
    }

    @Override
    public @NotNull Platform getPlatform() {
        return platform;
    }
}
