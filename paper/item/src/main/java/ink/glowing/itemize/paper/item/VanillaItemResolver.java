package ink.glowing.itemize.paper.item;

import ink.glowing.itemize.Itemize;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VanillaItemResolver implements ItemResolver {
    private static final int KEY_INDEX = 1;
    private static final int PARAMS_INDEX = 2;
    private static final int AMOUNT_INDEX = 3;

    private static final Key KEY = Itemize.itemizeKey("vanilla");
    private static final Pattern ITEM_PATTERN = Pattern.compile("((?:(?:[a-z0-9_\\-.]+:)?|:)[a-z0-9_\\-./]+)(\\[.*])?(?: (\\d{1,9}))?");

    @Override
    public @Nullable ItemStack resolve(@NotNull String params) {
        Matcher matcher = ITEM_PATTERN.matcher(params);
        if (!matcher.matches()) return null;
        @Subst("namespace:value") String keyStr = matcher.group(KEY_INDEX);
        Material type = Registry.MATERIAL.get(Key.key(keyStr));
        if (type == null || !type.isItem()) return null;
        if (type.isAir()) return ItemStack.empty();
        String amountStr = matcher.group(AMOUNT_INDEX);
        int amount;
        if (amountStr == null) {
            amount = 1;
        } else {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) return null;
        }
        ItemStack item = ItemStack.of(type, amount);
        String vanillaParams = matcher.group(PARAMS_INDEX);
        if (vanillaParams == null) return item;
        try {
            //noinspection deprecation
            return Bukkit.getUnsafe().modifyItemStack(item, keyStr + vanillaParams);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
