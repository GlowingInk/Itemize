package ink.glowing.itemize.paper.external.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.MetaItemStack;
import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.paper.item.ItemResolver;
import net.ess3.api.IItemDb;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class EssentialsItemResolver implements ItemResolver {
    private static final Key KEY = Itemize.itemizeKey("essentials");

    private final Essentials ess;
    private final IItemDb itemDb;
    private final CommandSource console;

    public EssentialsItemResolver(@NotNull Server server) {
        this.ess = (Essentials) Objects.requireNonNull(server.getPluginManager().getPlugin("Essentials"));
        this.itemDb = ess.getItemDb();
        this.console = new CommandSource(ess, server.getConsoleSender());
    }

    @Override
    public @Nullable ItemStack resolve(@NotNull String params) {
        try {
            String[] split = params.split(" ");
            ItemStack item = itemDb.get(split[0]);
            if (item == null) {
                return null;
            } else if (item.getType().isAir()) {
                return item; // No reason to parse further
            }

            if (split.length == 1) {
                int defAmount = ess.getSettings().getDefaultStackSize();
                if (defAmount > 0) item.setAmount(defAmount);
            } else {
                int offset = 1;
                try {
                    int amount = Integer.parseInt(split[1]);
                    if (amount > 0) item.setAmount(amount);
                    offset += 1;
                } catch (NumberFormatException ignored) {
                    int defAmount = ess.getSettings().getDefaultStackSize();
                    if (defAmount > 0) item.setAmount(defAmount);
                }
                if (split.length == 2) {
                    return item;
                } else  {
                    MetaItemStack essItem = new MetaItemStack(item);
                    essItem.parseStringMeta(console, true, split, offset, ess);
                    return essItem.getItemStack();
                }
            }
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
