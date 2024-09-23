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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static ink.glowing.itemize.Resolver.emptySuppler;

public class RedirectItemResolver implements ItemResolver {
    private static final Key KEY = Itemize.itemizeKey("redirect");

    private final Itemize itemize;
    private Map<String, @NotNull Supplier<@Nullable ItemStack>> references = Map.of();

    public RedirectItemResolver(@NotNull Itemize itemize) {
        this.itemize = itemize;
    }

    @Override
    public void reload(@NotNull ResolvingChief<ItemStack> chief) throws ConfigurateException {
        references = new HashMap<>();
        File cfgFile;
        try {
            cfgFile = itemize.prepareFile("item-redirects.yml", true);
        } catch (IOException ex) {
            throw new ConfigurateException(ex);
        }
        ConfigurationNode cfg = YamlConfigurationLoader.builder().path(cfgFile.toPath()).build().load();
        for (var entry : cfg.childrenMap().entrySet()) {
            String alias = (String) entry.getKey();
            RedirectedItem redirectedItem = entry.getValue().get(RedirectedItem.class);
            if (redirectedItem == null) continue; // TODO Log
            Supplier<ItemStack> supplier = chief.resolvingSupplier(redirectedItem.value);
            if (supplier == null) continue;// TODO Log
            if (redirectedItem.overrideAmount == 0) {
                references.put(alias, supplier);
            } else {
                references.put(
                        alias,
                        () -> {
                            ItemStack got = supplier.get();
                            if (got == null) return null;
                            got.setAmount(redirectedItem.overrideAmount);
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

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    @ConfigSerializable
    private static class RedirectedItem {
        private @MonotonicNonNull String value;
        private @Range(from = 0, to = Integer.MAX_VALUE) int overrideAmount = 0;
    }
}

