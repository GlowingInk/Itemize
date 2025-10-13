package ink.glowing.itemize.paper.item;

import ink.glowing.itemize.Resolver;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class ItemResolver implements Resolver<ItemStack>, Keyed {
    protected final Key key;

    protected ItemResolver(@NotNull Key key) {
        this.key = key;
    }

    @Override
    public @NotNull Supplier<@Nullable ItemStack> asSuppler(@NotNull String params) {
        ItemStack item = resolve(params);
        return item != null
                ? item::clone
                : Resolver.emptySuppler();
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
