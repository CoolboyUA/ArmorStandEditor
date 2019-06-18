package ua.coolboy.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import ua.coolboy.armorstandeditor.animation.Animation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerAnimationsMenu {
    
    private Inventory inventory;
    private OfflinePlayer player;
    private ArmorStandEditorPlugin plugin;
    private ItemStack empty;
    private static String inventoryName;
    
    public PlayerAnimationsMenu(OfflinePlayer player) {
        this.player = player;
        plugin = ArmorStandEditorPlugin.instance();
        empty = new ItemStack(Material.BARRIER);
        empty = editMeta(empty, "Empty", Arrays.asList("There is no animations"));
        inventoryName = "Animations";//TODO Language
    }
    
    public void open() {
        if (player.isOnline()) {
            open((Player) player);
        }
    }
    
    public void open(Player player) {
        List<Animation> playerAnimations = new ArrayList<>();
        ConfigurationSection section = plugin.getPlayers().getConfigurationSection(player.getUniqueId().toString());
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (section.getString(key,"").equals(player.getUniqueId().toString())) {
                    playerAnimations.add(plugin.animManager.getById(key));
                }
            }
        }
        if (playerAnimations.isEmpty()) {
            inventory = Bukkit.createInventory(plugin.editorManager.getAnimationHolder(), 9, inventoryName);
            inventory.setItem(4, empty);
        } else {
            int size = (int) (Math.floor(playerAnimations.size() / 9) * 9) + 9;
            inventory = Bukkit.createInventory(plugin.editorManager.getAnimationHolder(), size, inventoryName);
            for (Animation animation : playerAnimations) {
                inventory.addItem(createIcon(animation));
            }
        }
        player.openInventory(inventory);
    }
    
    private ItemStack createIcon(Animation animation) {
        ItemStack stack = new ItemStack(Material.STRUCTURE_VOID);
        List<String> lore = new ArrayList<>();
        String status = !animation.isStopped()? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
        lore.add(ChatColor.GRAY + "Status: " + status);
        lore.add(ChatColor.DARK_AQUA + "Click to toggle");
        lore.add(ChatColor.GRAY+"Stands: "+animation.getStands().size());
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
