package ink.glowing.itemize.paper.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.ResolvingChief;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record ItemizeChiefKeyArgument(@NotNull Supplier<ResolvingChief<?>> chiefSupplier) implements CustomArgumentType.Converted<@NotNull Key, @NotNull Key> {
    public ItemizeChiefKeyArgument(@NotNull ResolvingChief<?> chief) {
        this(() -> chief);
    }

    @Override
    public @NotNull Key convert(@NotNull Key key) {
        return key.namespace().equals(Key.MINECRAFT_NAMESPACE)
                ? Itemize.itemizeKey(key.value())
                : key;
    }

    @Override
    public @NotNull ArgumentType<Key> getNativeType() {
        return ArgumentTypes.key();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        ResolvingChief<?> chief = chiefSupplier.get();
        Set<String> addedShort = new HashSet<>(chief.resolversCount());
        chief.forEachResolver((key, resolver) -> {
            builder.suggest(key.toString());
            if (addedShort.add(key.value())) builder.suggest(key.value());
        });
        return CompletableFuture.completedFuture(builder.build());
    }
}
