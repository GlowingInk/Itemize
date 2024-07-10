package ink.glowing.itemize.paper.item;

import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.ResolvingChief;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static ink.glowing.itemize.Resolver.emptySuppler;

public class ReferenceItemResolver implements ItemResolver {
    private static final Key KEY = Itemize.itemizeKey("reference");

    private Map<String, @NotNull Supplier<@Nullable ItemStack>> references = Map.of();

    @Override
    public void reload(@NotNull Itemize core, @NotNull ResolvingChief<ItemStack> registry) throws ConfigurateException {
        references = new HashMap<>();
        File cfgFile = core.prepareFile("item-references.yml", true);
        ConfigurationNode cfg = YamlConfigurationLoader.builder().path(cfgFile.toPath()).build().load();
        for (var entry : cfg.childrenMap().entrySet()) {
            String alias = (String) entry.getKey();
            ReferencedItem referencedItem = entry.getValue().get(ReferencedItem.class);
            if (referencedItem == null) continue; // TODO Log
            Supplier<ItemStack> supplier = registry.resolvingSupplier(referencedItem.key, referencedItem.value);
            if (supplier == null) continue;// TODO Log
            if (referencedItem.amount == 0) {
                references.put(alias, supplier);
            } else {
                references.put(
                        alias,
                        () -> {
                            ItemStack got = supplier.get();
                            if (got == null) return null;
                            got.setAmount(referencedItem.amount);
                            return got;
                        }
                );
            }
        }
    }

    @Override
    public @Nullable ItemStack resolve(@NotNull String params) {
        return references.getOrDefault(params, emptySuppler()).get();
    }

    @Override
    public @NotNull Supplier<@Nullable ItemStack> asSuppler(@NotNull String params) {
        return references.getOrDefault(params, emptySuppler());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @SuppressWarnings("unused")
    @ConfigSerializable
    private static class ReferencedItem {
        private @MonotonicNonNull String key;
        private @MonotonicNonNull String value;
        private @Range(from = 0, to = Integer.MAX_VALUE) int amount;
    }
}

