package ink.glowing.itemize.paper.item;

import ink.glowing.itemize.Resolver;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ItemResolver extends Resolver<ItemStack> {
    @Override
    default @NotNull Supplier<@Nullable ItemStack> asSuppler(@NotNull String params) {
        ItemStack item = resolve(params);
        return item != null
                ? item::clone
                : Resolver.emptySuppler();
    }
}
