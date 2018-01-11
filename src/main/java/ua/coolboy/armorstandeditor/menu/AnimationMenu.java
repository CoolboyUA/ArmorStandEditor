package ua.coolboy.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import ua.coolboy.armorstandeditor.animation.Animation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnimationMenu {

    private Inventory inventory;
    private ArmorStandEditorPlugin plugin;
    private ArmorStand stand;
    private ItemStack empty;
    private static String inventoryName;

    public AnimationMenu(ArmorStand stand) {
        this.stand = stand;
        plugin = ArmorStandEditorPlugin.instance();
        empty = new ItemStack(Material.BARRIER);
        empty = editMeta(empty, "Empty", Arrays.asList("You don't have animations"));
        inventoryName = "ArmorStand Animation";//TODO Language
    }

    public void open(Player player) {
        List<Animation> playerAnimations = new ArrayList<>();
        if (plugin.allAnimations) {
            playerAnimations = plugin.animManager.getAnimations();
        } else {
            for (Animation animation : plugin.animManager.getAnimations()) {
                if (animation.getCreator().equals(player.getUniqueId())) {
                    playerAnimations.add(animation);
                }
            }
        }

        Animation current = plugin.animManager.getByStandUUID(stand.getUniqueId());
        if (playerAnimations.isEmpty()) {
            inventory = Bukkit.createInventory(plugin.editorManager.getPluginHolder(), 9, inventoryName);
            inventory.setItem(4, empty);
        } else {
            int size = (int) (Math.floor((playerAnimations.size() + 1) / 9) * 9) + 9;
            inventory = Bukkit.createInventory(plugin.editorManager.getPluginHolder(), size, inventoryName);
            for (Animation animation : playerAnimations) {
                if (current != null && animation.equals(current)) {
                    inventory.addItem(createCurrentIcon(animation));
                } else {
                    inventory.addItem(createIcon(animation));
                }
            }
        }
        inventory.setItem(inventory.getSize() - 1, createInfo());
        player.openInventory(inventory);
    }

    private ItemStack createInfo() {
        ItemStack stack = new ItemStack(Material.SIGN);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stand UUID: " + stand.getUniqueId().toString());
        lore.add(ChatColor.GRAY + "World: " + stand.getWorld().getName());
        return editMeta(stack, ChatColor.GRAY + "Info", lore);
    }

    private ItemStack createCurrentIcon(Animation animation) {
        ItemStack stack = createIcon(animation);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Selected");
        lore.add(ChatColor.DARK_AQUA + "Click to stop");
        lore.add(ChatColor.GRAY + "Frames: " + ChatColor.GOLD + animation.getFrames().size());
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LUCK, 0, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createIcon(Animation animation) {
        ItemStack stack = new ItemStack(Material.STRUCTURE_VOID);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_AQUA + "Click to use");
        lore.add(ChatColor.GRAY + "Frames: " + ChatColor.GOLD + animation.getFrames().size());
        stack = editMeta(stack, ChatColor.AQUA + "Animation " + ChatColor.DARK_AQUA + animation.getID(), lore);
        return stack;
    }

    private ItemStack editMeta(ItemStack stack, String name, List<String> lore) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        stack.setItemMeta(meta);
        return stack;
    }

    public static String getName() {
        return inventoryName;
    }
}
