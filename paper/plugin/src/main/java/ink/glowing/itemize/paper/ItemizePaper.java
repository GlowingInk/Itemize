package ink.glowing.itemize.paper;

import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.ResolvingChief;
import ink.glowing.itemize.SimpleResolvingChief;
import ink.glowing.itemize.paper.external.essentials.EssentialsItemResolver;
import ink.glowing.itemize.paper.item.ReferenceItemResolver;
import ink.glowing.itemize.paper.item.VanillaItemResolver;
import ink.glowing.itemize.text.CatchingTextResolver;
import ink.glowing.itemize.text.SimpleTextResolver;
import ink.glowing.text.InkyMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static ink.glowing.itemize.Itemize.itemizeKey;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

public class ItemizePaper extends JavaPlugin implements Itemize {
    private final Map<Class<?>, ResolvingChief<?>> chiefs = new ConcurrentHashMap<>();

    private final ResolvingChief<Component> textChief;
    private final ResolvingChief<ItemStack> itemChief;

    public ItemizePaper() {
        this.textChief = getChief(Component.class);
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

        this.itemChief = getChief(ItemStack.class);
        this.itemChief.addResolver(new ReferenceItemResolver());
        this.itemChief.addResolver(new VanillaItemResolver());
    }

    private void registerExternal() {
        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("Essentials")) {
            this.itemChief.addResolver(new EssentialsItemResolver(getServer()));
        }
    }

    @Override
    public void onLoad() {
        getServer().getServicesManager().register(Itemize.class, this, this, ServicePriority.Lowest);
    }

    @Override
    public void onEnable() {
        getServer().getGlobalRegionScheduler().run(this, (task) -> {
            try {
                registerExternal();
                reloadAll();
            } catch (ConfigurateException e) {
                throw new RuntimeException(e); // TODO Better handle
            }
        });
    }

    @Override
    public void reloadAll() throws ConfigurateException {
        for (var entry : chiefs.entrySet()) {
            entry.getValue().reloadResolvers(this);
        }
    }

    @Override
    public @NotNull File prepareFile(@NotNull String name, boolean resource) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            if (resource) {
                saveResource(name, false);
            } else try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
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

    @Override
    public boolean hasChief(@NotNull Class<?> type) {
        return chiefs.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T> ResolvingChief<T> getChief(@NotNull Class<T> type) {
        return (ResolvingChief<T>) chiefs.computeIfAbsent(type, (t) -> new SimpleResolvingChief<T>());
    }

    @Override
    @SuppressWarnings("unchecked")
    @Contract("_, true -> !null")
    public @Nullable <T> ResolvingChief<T> getChief(@NotNull Class<T> type, boolean create) {
        if (create) return getChief(type);
        ResolvingChief<?> chief = chiefs.get(type);
        return chief != null
                ? (ResolvingChief<T>) chief
                : null;
    }

    @Override
    public @NotNull Logger logger() {
        return getLogger();
    }
}
