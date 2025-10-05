package ink.glowing.itemize.paper;

import ink.glowing.itemize.paper.command.ItemizeCommand;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
@ApiStatus.Internal
public class ItemizePaperBootstrap implements PluginBootstrap {
    private ItemizePaper itemizePlugin;

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return (itemizePlugin = new ItemizePaper());
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext bootContext) {
        LifecycleEventManager<BootstrapContext> manager = bootContext.getLifecycleManager();
        manager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> new ItemizeCommand(itemizePlugin).register(event.registrar())
        );
    }
}
