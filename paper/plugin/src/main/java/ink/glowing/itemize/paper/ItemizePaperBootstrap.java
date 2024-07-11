package ink.glowing.itemize.paper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import ink.glowing.itemize.Itemize;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static ink.glowing.itemize.paper.util.CommandUtils.hasPermission;
import static ink.glowing.itemize.paper.util.CommandUtils.node;
import static ink.glowing.text.InkyMessage.inkyMessage;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.*;

@SuppressWarnings("UnstableApiUsage")
@ApiStatus.Internal
public class ItemizePaperBootstrap implements PluginBootstrap {
    private final ItemizeItemKeyArgument itemKeyArg = new ItemizeItemKeyArgument();

    private ItemizePaper itemizePlugin;

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return (itemizePlugin = new ItemizePaper());
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext bootContext) {
        LifecycleEventManager<BootstrapContext> manager = bootContext.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(literal("itemize")
                    .requires(hasPermission("itemize.command"))
                    .executes(help())
                    .then(literal("help").executes(help()))
                    .then(literal("reload")
                            .requires(hasPermission("itemize.command.reload"))
                            .executes(reload()))
                    .then(literal("item")
                            .requires(hasPermission("itemize.command.item"))
                            .executes(itemHelp())
                            .then(literal("help").executes(itemHelp()))
                            .then(literal("list").executes(node((c, s) -> s.sendMessage("TODO")))) // TODO
                            .then(literal("get").then(itemArg("get", itemGet(true))))
                            .then(literal("sget").then(itemArg("sget", itemGet(false))))
                            .then(literal("give").then(itemGiveArg(true)))
                            .then(literal("sgive").then(itemGiveArg(false)))
                            .then(literal("fill").then(itemFillArg(true)))
                            .then(literal("sfill").then(itemFillArg(false)))
                            .then(literal("put").then(itemPutArg(true))) // TODO Slots
                            .then(literal("sput").then(itemPutArg(false))))
                    .build()
            );
        });
    }

    private Command<CommandSourceStack> help() {
        return node((context, sender) -> {
            sendInky(sender, "&6&lItemize " + itemizePlugin.getPluginMeta().getVersion());
            sendInky(sender, "&[&a/itemize item](click:suggest /itemize item) &7- item resolver subcommand");
        });
    }

    private Command<CommandSourceStack> reload() {
        return node((context, sender) -> {
            try {
                itemizePlugin.reloadAll();
            } catch (ConfigurateException e) {
                sendError(sender, "Couldn't reload the plugin, see console logs", true);
                itemizePlugin.logger().log(Level.SEVERE, "Couldn't reload the plugin", e);
            }
        });
    }

    private Command<CommandSourceStack> itemHelp() {
        return node((context, sender) -> {
            sendInky(sender, "&6&lItem Resolver");
            sendInky(sender, "&a/itemize item list &7- list registered resolvers&e TODO");
            sendInky(sender, "&[&a/itemize item get <key> <value>](click:suggest /itemize item get )&7- resolve item");
            sendInky(sender, "&[&a/itemize item give <player> <key> <value>](click:suggest /itemize item give )&7- resolve and give item");
            sendInky(sender, "&[&a/itemize item fill <world> <position> <key> <value>](click:suggest /itemize item fill )&7- fill a block using resolver");
            sendInky(sender, "&[&a/itemize item put <world> <position> <key> <value>](click:suggest /itemize item put )&7- put an item into a block using resolver");
            sendInky(sender, "&[&7&o(add \"s\" before action subcommand to disable text output)](hover:text &fE.g. /itemize item sget vanilla diamond");
        });
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> itemArg(String perm, Command<CommandSourceStack> action) {
        return argument("key", itemKeyArg).then(
                argument("params", greedyString())
                        .requires(hasPermission("itemize.command.item." + perm))
                        .executes(action)
        );
    }

    private Command<CommandSourceStack> itemGet(boolean output) {
        return node((context, sender) -> {
            Key key = context.getArgument("key", Key.class);
            if (!itemizePlugin.items().hasResolver(key)) {
                sendError(sender, "Unable to get item - no resolvers with the key &n" + key, output);
            }
            ItemStack item = itemizePlugin.items().resolve(key, context.getArgument("params", String.class));
            if (item == null) {
                sendError(sender, "Resolver couldn't generate item", output);
            } else if (sender instanceof Player player) {
                player.getScheduler().run(itemizePlugin, (task) -> {
                    player.getInventory().addItem(item);
                    sendFine(sender, "Gave &{lang:" + item.translationKey() + "} x " + item.getAmount(), output); // TODO Not enough space in inv
                }, null);
            }
        });
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> itemGiveArg(boolean output) {
        return argument("player", players()).then(itemArg("give", itemGive(output)));
    }

    private Command<CommandSourceStack> itemGive(boolean output) {
        return node((context, sender) -> {
            Key key = context.getArgument("key", Key.class);
            if (!itemizePlugin.items().hasResolver(key)) {
                sendError(sender, "Unable to get item - no resolvers with the key &n" + key, output);
                return;
            }
            Supplier<ItemStack> supplier = itemizePlugin.items().resolvingSupplier(key, context.getArgument("params", String.class));
            if (supplier == null) {
                sendError(sender, "Resolver couldn't generate item", output);
                return;
            }
            var players = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
            if (players.isEmpty()) {
                sendError(sender, "There's no players selected", output);
                return;
            }
            for (Player player : players) {
                player.getScheduler().run(
                        itemizePlugin,
                        (task) -> player.getInventory().addItem(supplier.get()), // TODO Handle when not enough space in inv
                        () -> sendError(sender, "Couldn't give " + player.getName() + " an item - they went offline", output)
                );
            }
            sendFine(sender, "Gave items to " + players.size() + " players", output);
        });
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> itemFillArg(boolean output) {
        return argument("world", world()).then(argument("position", blockPosition()).then(itemArg("fill", itemFill(output))));
    }

    private Command<CommandSourceStack> itemFill(boolean output) {
        return node((context, sender) -> {
            Key key = context.getArgument("key", Key.class);
            Supplier<ItemStack> supplier = itemizePlugin.items().resolvingSupplier(key, context.getArgument("params", String.class));
            if (supplier == null) {
                sendError(sender, "Unable to get item - no resolvers with the key &n" + key, output);
                return;
            }
            World world = context.getArgument("world", World.class);
            BlockPosition position = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());
            Block block = world.getBlockAt(position.blockX(), position.blockY(), position.blockZ());
            if (block.getState(false) instanceof InventoryHolder invHolder) {
                Inventory inv = invHolder.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    inv.setItem(i, supplier.get());
                }
                sendFine(sender, "Filled block inventory with items", output);
            } else {
                sendError(sender, "Block doesn't have an inventory", output);
            }
        });
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> itemPutArg(boolean output) {
        var putArg = itemArg("put", itemPut(output));
        return argument("world", world()).then(argument("position", blockPosition())
                .then(argument("slots", integerRange()).then(putArg))
                .then(argument("slot", integer()).then(putArg))
                .then(literal("add").then(putArg))
        );
    }

    private Command<CommandSourceStack> itemPut(boolean output) {
        return node((context, sender) -> {
            Key key = context.getArgument("key", Key.class);
            Supplier<ItemStack> supplier = itemizePlugin.items().resolvingSupplier(key, context.getArgument("params", String.class));
            if (supplier == null) {
                sendError(sender, "Unable to get item - no resolvers with the key &n" + key, output);
                return;
            }
            World world = context.getArgument("world", World.class);
            BlockPosition position = context.getArgument("position", BlockPositionResolver.class).resolve(context.getSource());
            Block block = world.getBlockAt(position.blockX(), position.blockY(), position.blockZ());
            if (block.getState(false) instanceof InventoryHolder invHolder) {
                invHolder.getInventory().addItem(supplier.get());
                sendFine(sender, "Added item into a block inventory", output);
            } else {
                sendError(sender, "Block doesn't have an inventory", output);
            }
        });
    }

    private static void sendInky(Audience audience, String msg) {
        audience.sendMessage(inkyMessage().deserialize(msg));
    }

    private static void sendFine(Audience audience, String msg, boolean output) {
        if (output) audience.sendMessage(inkyMessage().deserialize("&6Itemize>&r " + msg));
    }

    private static void sendError(Audience audience, String msg, boolean output) {
        if (output) audience.sendMessage(inkyMessage().deserialize("&cItemize>&r " + msg));
    }

    private class ItemizeItemKeyArgument implements CustomArgumentType.Converted<Key, Key> {
        @Override
        public @NotNull Key convert(@NotNull Key key) {
            return key.namespace().equals(Key.MINECRAFT_NAMESPACE)
                    ? Itemize.itemizeKey(key.value())
                    : key;
        }

        @Override
        public @NotNull ArgumentType<Key> getNativeType() {
            return ArgumentTypes.key();
        }

        @Override
        public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
            Set<String> addedShort = new HashSet<>(itemizePlugin.items().resolversCount());
            itemizePlugin.items().forEachResolver((key, resolver) -> {
                builder.suggest(key.toString());
                if (addedShort.add(key.value())) builder.suggest(key.value());
            });
            return CompletableFuture.completedFuture(builder.build());
        }
    }
}
