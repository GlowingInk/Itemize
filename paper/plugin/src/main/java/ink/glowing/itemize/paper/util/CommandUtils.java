package ink.glowing.itemize.paper.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class CommandUtils {
    @Contract(pure = true)
    public static @NotNull Predicate<CommandSourceStack> hasPermission(@NotNull String permission) {
        return sourceStack -> sourceStack.getSender().hasPermission(permission);
    }

    @Contract(pure = true)
    public static @NotNull Predicate<CommandSourceStack> isPlayer() {
        return sourceStack -> sourceStack.getSender() instanceof Player;
    }

    @Contract(pure = true)
    public static @NotNull Command<CommandSourceStack> node(@NotNull CommandAction action) {
        return cmdContext -> {
            action.accept(cmdContext, cmdContext.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        };
    }

    public interface CommandAction {
        void accept(@NotNull CommandContext<CommandSourceStack> cmdContext, @NotNull CommandSender sender) throws CommandSyntaxException;
    }
}
