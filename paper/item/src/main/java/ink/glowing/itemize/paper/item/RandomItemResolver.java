package ink.glowing.itemize.paper.item;

import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.util.random.weight.WeightedPool;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RandomItemResolver extends ItemResolver { // TODO
    private WeightedPool<Supplier<ItemStack>> weightedPool;

    protected RandomItemResolver() {
        super(Itemize.itemizeKey("random"));
    }

    @Override
    public @Nullable ItemStack resolve(@NotNull String params) {
        return null;
    }
}
