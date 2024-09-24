package ink.glowing.itemize.paper;

import ink.glowing.itemize.Itemize;
import ink.glowing.itemize.KeyedType;
import ink.glowing.itemize.ResolvingChief;
import ink.glowing.itemize.SimpleResolvingChief;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static ink.glowing.itemize.Itemize.itemizeKey;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

public class ItemizePaper extends JavaPlugin implements Itemize {
    private final Map<KeyedType<?>, ResolvingChief<?>> chiefs;

    private final ResolvingChief<Component> textChief;
    private final ResolvingChief<ItemStack> itemChief;

    public ItemizePaper() {
        this.chiefs = new ConcurrentHashMap<>();

        this.textChief = getChief(Component.class);
        registerTextResolvers();
        this.itemChief = getChief(ItemStack.class);
        registerItemResolvers();
    }

    @Override
    public void onLoad() {
        getServer().getServicesManager().register(Itemize.class, this, this, ServicePriority.Lowest);
    }

    @Override
    public void onEnable() {
        getServer().getGlobalRegionScheduler().run(this, (task) -> {
            registerExternal();
            try {
                reload();
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
        this.itemChief.addResolver(new RedirectItemResolver(this));
        this.itemChief.addResolver(new VanillaItemResolver());
    }

    private void registerExternal() {
        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("Essentials")) {
            this.itemChief.addResolver(new EssentialsItemResolver(getServer()));
        }
    }

    @Override
    public void reload() throws ConfigurateException {
        try {
            for (var entry : chiefs.entrySet()) {
                entry.getValue().reload();
            }
        } catch (ConfigurateException cfgEx) {
            throw cfgEx;
        } catch (Exception ex) {
            throw new ConfigurateException(ex);
        }
    }

    @Override
    public @NotNull File prepareFile(@NotNull String name, boolean resource) throws IOException {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            if (resource) {
                saveResource(name, false);
            } else {
                file.createNewFile();
            }
        }
        return file;
    }

    @Override
    public boolean hasKeyedChief(@NotNull KeyedType<?> keyedType) {
        return chiefs.containsKey(keyedType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> ResolvingChief<T> enforceChief(@NotNull KeyedType<T> keyedType, @NotNull ResolvingChief<T> chief) {
        ResolvingChief<T> oldChief = (ResolvingChief<T>) chiefs.get(keyedType);
        if (oldChief != null) {
            oldChief.forEachResolver((_k, resolver) -> chief.addResolver(resolver));
        }
        chiefs.put(keyedType, chief);
        return oldChief;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> ResolvingChief<T> getKeyedChief(@NotNull KeyedType<T> keyedType) {
        return (ResolvingChief<T>) chiefs.computeIfAbsent(keyedType, (t) -> new SimpleResolvingChief<T>(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> ResolvingChief<T> getKeyedChief(@NotNull KeyedType<T> keyedType, boolean create) {
        if (create) return getKeyedChief(keyedType);
        ResolvingChief<?> chief = chiefs.get(keyedType);
        return chief != null
                ? (ResolvingChief<T>) chief
                : null;
    }

    public @NotNull ResolvingChief<Component> texts() {
        return textChief;
    }

    public @NotNull ResolvingChief<ItemStack> items() {
        return itemChief;
    }
}
