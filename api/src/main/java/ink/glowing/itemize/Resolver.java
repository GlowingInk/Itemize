package ink.glowing.itemize;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.util.function.Supplier;

public interface Resolver<T> extends Keyed {
    Supplier<?> EMPTY_SUPPLER = () -> null;

    default void reload(@NotNull Itemize core, @NotNull ResolvingChief<T> registry) throws ConfigurateException { }

    @Nullable T resolve(@NotNull String params);

    default @NotNull Supplier<@Nullable T> asSuppler(@NotNull String params) {
        return () -> resolve(params);
    }

    @SuppressWarnings("unchecked")
    static <T> @NotNull Supplier<@Nullable T> emptySuppler() {
        return (Supplier<T>) EMPTY_SUPPLER;
    }
}
