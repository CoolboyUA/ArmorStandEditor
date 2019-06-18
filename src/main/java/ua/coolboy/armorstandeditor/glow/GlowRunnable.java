package ua.coolboy.armorstandeditor.glow;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GlowRunnable extends BukkitRunnable {

    private ArmorStandEditorPlugin plugin;
    private Map<UUID, Map<Entity,BukkitTask>> glowing;
    private List<String> supportedVersions = Arrays.asList("v1_12_R1", "v1_13_R1", "v1_13_R2", "v1_14_R1");
    boolean working = true;

    public GlowRunnable(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        glowing = new HashMap<>();
        
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        if(!supportedVersions.contains(version)) {
            Bukkit.getLogger().warning("Glowing feature is unsupported on this version!");
            working = false;
        }
    }

    @Override
    public void run() {
        if(!working) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("asedit.glow")) {
                continue;
            }
            if (plugin.isEditTool(player.getInventory().getItemInMainHand())) {
                for (Entity entity : player.getLocation().getWorld().getNearbyEntities(player.getLocation(), 4, 4, 4)) {//4 block radius to search stands
                    if (entity instanceof ArmorStand) {
                        if (((ArmorStand) entity).isVisible()) continue;
                        
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                setGlowing(player, entity, true);
                            }
                        }.runTaskLater(plugin, 0); //don't make async packets
                    }
                }
            }
        }
    }

    private void setGlowing(Player player, Entity entity, boolean glow) {
        new GlowPacket(entity, glow).send(player);
        Map<Entity,BukkitTask> stands = glowing.get(player.getUniqueId());
        if (stands == null) {
            stands = new HashMap<>();
        }
        if (glow) {
            if(stands.get(entity) != null) stands.get(entity).cancel();
            stands.put(entity,new BukkitRunnable() {//remove glowing
                                    @Override
                                    public void run() {
                                        setGlowing(player, entity, false);
                                    }
                                }.runTaskLater(plugin, 20)); //remove after 1 second
        } else {
            stands.remove(entity);
        }
        glowing.put(player.getUniqueId(), stands);
    }
    
    public boolean isGlowing(Entity entity, Player player) {
        return glowing.get(player.getUniqueId()) != null && glowing.get(player.getUniqueId()).containsKey(entity);
    }

}
