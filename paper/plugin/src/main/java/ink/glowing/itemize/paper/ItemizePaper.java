package ink.glowing.itemize.paper;

import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.ResolvingChief;
import ink.glowing.itemize.SimpleItemize;
import ink.glowing.itemize.paper.external.essentials.EssentialsItemResolver;
import ink.glowing.itemize.paper.item.RedirectItemResolver;
import ink.glowing.itemize.paper.item.VanillaItemResolver;
import ink.glowing.itemize.text.CatchingTextResolver;
import ink.glowing.itemize.text.SimpleTextResolver;
import ink.glowing.text.InkyMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static ink.glowing.itemize.Itemize.itemizeKey;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

public class ItemizePaper extends JavaPlugin implements Itemize.Platform {
    private final Itemize itemize;

    private final ResolvingChief<Component> textChief;
    private final ResolvingChief<ItemStack> itemChief;

    @ApiStatus.Internal
    ItemizePaper() {
        this.itemize = new SimpleItemize(this);

        this.textChief = itemize.getChief(Component.class);
        registerTextResolvers();
        this.itemChief = itemize.getChief(ItemStack.class);
        registerItemResolvers();
    }

    @Override
    public @NotNull Itemize getItemize() {
        return itemize;
    }

    @Override
    @ApiStatus.Internal
    public void onLoad() {
        getServer().getServicesManager().register(Itemize.class, itemize, this, ServicePriority.Lowest);
    }

    @Override
    @ApiStatus.Internal
    public void onEnable() {
        getServer().getGlobalRegionScheduler().run(this, _ -> {
            registerExternal();
            try {
                itemize.reload();
            } catch (ConfigurateException ex) {
                getLogger().log(Level.WARNING, "Got an error while reloading Itemize resolvers", ex);
            }
        });
    }

    private void registerTextResolvers() {
        this.textChief.addResolver(new SimpleTextResolver(
                itemizeKey("legacy"),
                (str) -> legacyAmpersand().deserialize(str.replace(SECTION_CHAR, AMPERSAND_CHAR))
        ));
        this.textChief.addResolver(new SimpleTextResolver(
                itemizeKey("inkymessage"),
                InkyMessage.inkyMessage()
        ));
        this.textChief.addResolver(new CatchingTextResolver(
                itemizeKey("minimessage"),
                MiniMessage.miniMessage()
        ));
    }

    private void registerItemResolvers() {
        this.itemChief.addResolver(new RedirectItemResolver(itemize));
        this.itemChief.addResolver(new VanillaItemResolver());
    }

    private void registerExternal() {
        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("Essentials")) {
            this.itemChief.addResolver(new EssentialsItemResolver(getServer()));
        }
    }

    @Override
    public @NotNull File prepareFile(@NotNull String name, boolean resource) throws IOException {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            if (resource) {
                saveResource(name, false);
            } else {
                if (!file.createNewFile() && !file.exists()) {
                    throw new IOException("Failed to create file: " + file.getPath());
                }
            }
        }
        return file;
    }

    public @NotNull ResolvingChief<Component> texts() {
        return textChief;
    }

    public @NotNull ResolvingChief<ItemStack> items() {
        return itemChief;
    }
}
