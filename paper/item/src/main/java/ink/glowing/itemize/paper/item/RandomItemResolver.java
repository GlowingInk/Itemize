package ink.glowing.itemize.paper.item;

import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.util.rng.WeightedPool;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RandomItemResolver implements ItemResolver { // TODO
    private static final Key KEY = Itemize.itemizeKey("random");

    private WeightedPool<Supplier<ItemStack>> weightedPool;

    @Override
    public @Nullable ItemStack resolve(@NotNull String params) {
        return null;
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
