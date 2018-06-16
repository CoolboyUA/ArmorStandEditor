package ua.coolboy.armorstandeditor.glow;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import java.util.HashMap;
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

    public GlowRunnable(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        glowing = new HashMap<>();
        
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        if(!version.equals("1_12_R1")) Bukkit.getLogger().warning("Glowing feature tested only on 1.12!");
    }

    @Override
    public void run() {
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
