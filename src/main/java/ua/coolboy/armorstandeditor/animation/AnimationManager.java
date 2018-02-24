package ua.coolboy.armorstandeditor.animation;

import ua.coolboy.armorstandeditor.animation.AnimationEditor;
import ua.coolboy.armorstandeditor.animation.Animation;
import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class AnimationManager {

    private ExecutorService executor;
    private List<Animation> animations;
    private List<Player> preEdit;
    private List<AnimationEditor> editors;
    private ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();

    public AnimationManager() {
        preEdit = new ArrayList<>();
        editors = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
        animations = loadAnimations();
    }

    /*private List<Animation> asyncInit() {
        List<Animation> list = new ArrayList<>();
        ExecutorService exe = Executors.newCachedThreadPool();

        Future<List<Animation>> future = exe.submit(() -> {
            List<Animation> animations = new ArrayList<>();
            int id = 1;
            for (World world : Bukkit.getWorlds()) {
                for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                    for (UUID uuid : stands.keySet()) {
                        if (stand.getUniqueId().equals(uuid)) {
                            String str = String.valueOf(id);
                            animations.add(new Animation(stand, config.getConfigurationSection(stand.getUniqueId().toString()),str));
                        }
                    }
                }
            }
            return animations;
        });

        try {
            if (future.get() != null) {
                list = future.get();
            }
        } catch (Exception ex) {
            Bukkit.getLogger().severe("Oops! Something goes wrong while loading animations!\n" + ex.getMessage());
        }
        return list;
    }*/
    private List<Animation> loadAnimations() {
        List<Animation> list = new ArrayList<>();
        File[] files = plugin.getAnimationFolder().listFiles();
        if (files != null) {
            for (File file : files) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                for (String key : config.getKeys(false)) {
                    Animation anim = new Animation(key, config.getConfigurationSection(key));
                    list.add(anim);
                    executor.submit(anim);
                }
            }
        }
        return list;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }
    
    public List<Animation> getAnimations() {
        return animations;
    }

    public void removeStandFromAnimations(ArmorStand stand) {
        for (Animation animation : animations) {
            animation.removeStand(stand);
        }
    }

    public Animation getById(String id) {
        for (Animation animation : animations) {
            if (animation.getID().equals(id)) {
                return animation;
            }
        }
        return null;
    }

    public Animation getByStandUUID(UUID uuid) {
        for (Animation animation : animations) {
            for (ArmorStand stand : animation.getStands()) {
                if (stand.getUniqueId().equals(uuid)) {
                    return animation;
                }
            }
        }
        return null;
    }

    public void addPlayerToPreEdit(Player player) {
        preEdit.add(player);
    }

    public void removePlayerFromPreEdit(Player player) {
        preEdit.remove(player);
        AnimationEditor toDelete = null;
        for (AnimationEditor editor : editors) {
            if (editor.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                toDelete = editor;
            }
        }
        if (toDelete != null) {
            editors.remove(toDelete);
        }
    }

    public List<Player> getPreEdits() {
        return preEdit;
    }

    public void removeEditor(AnimationEditor editor) {
        editors.remove(editor);
    }

    public void removePlayerEditor(Player player) {
        AnimationEditor editor = getEditor(player);
        if (editor != null) {
            editors.remove(editor);
        }
    }

    public boolean rename(String from, String to) {
        Animation animation = getById(from);
        if (animation != null) {
            File file = new File(plugin.getAnimationFolder(), to + ".yml");
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            conf.set(to, animation.getConfiguration());
            try {
                conf.save(file);
            } catch (IOException ex) {
                Bukkit.getLogger().severe("Can't save animation");
                return false;
            }
            plugin.reloadAnimations();
            return true;
        }
        return false;
    }

    public void save(Player player) {
        save(player, String.valueOf(getNextFreeId()));
    }

    public void save(Player player, String id) {
        AnimationEditor editor = getEditor(player);
        id = id.replace(" ", "_");
        if (editor != null) {
            if (!isAnimationWithIdExist(id)) {
                editor.save(id.toLowerCase());
                ArmorStandEditorPlugin.instance().reloadAnimations();
                editors.remove(editor);
                player.sendMessage("Saved animation with id: "+id);
            } else {
                player.sendMessage("There is already animation with that id!");
            }
        } else {
            player.sendMessage("You're not in editor mode!");
        }
    }

    public File getAnimationFile(Animation animation) {
        return new File(plugin.getAnimationFolder(), animation.getID());
    }

    public void deleteAnimation(Animation animation) {
        animations.remove(animation);
        getAnimationFile(animation).delete();
    }

    public boolean isAnimationWithIdExist(String id) {
        return Arrays.asList(plugin.getAnimationFolder().list()).contains(id.toLowerCase());
    }

    public int getNextFreeId() {
        List<String> list = Arrays.asList(plugin.getAnimationFolder().list());
        if (list != null) {
            int i = 0;
            while (true) {
                i++;
                if (!list.contains(String.valueOf(i))) {
                    return i;
                }
            }
        }
        return 0;
    }

    public static boolean canAddAnimation(Player player) {
        YamlConfiguration players = ArmorStandEditorPlugin.instance().getPlayers();
        String uuid = player.getUniqueId().toString();
        if (!players.isConfigurationSection(uuid)) {
            return true;
        }
        if(ArmorStandEditorPlugin.instance().animationLimit < 0) return true;

        if (players.getConfigurationSection(uuid).getKeys(false).size() < ArmorStandEditorPlugin.instance().animationLimit) {
            return true;
        } else {
            return player.hasPermission("asedit.animation.unlimit");
        }
    }

    public AnimationEditor getEditor(Player player) {
        for (AnimationEditor editor : editors) {
            if (editor.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return editor;
            }
        }
        return null;
    }

    public void addEditor(AnimationEditor editor) {
        removePlayerFromPreEdit(editor.getPlayer());
        editors.add(editor);
    }

    public List<AnimationEditor> getEditors() {
        return editors;
    }

    public void addStand(Player player, Animation animation, ArmorStand stand) {
        if (canAddAnimation(player)) {
            animation.addStand(stand);
            /*TODO starting animations after restart, uncomment this code
            plugin.getPlayers().set(player.getUniqueId() + "." + stand.getUniqueId(), animation.getID());
            plugin.savePlayers();*/
        } else {
            player.sendMessage("You can't add more animations!");
        }
    }

    public void removeStand(Player player, Animation animation, ArmorStand stand) {
        animation.removeStand(stand);
        removeStandFromPlayers(stand);
        plugin.savePlayers();
    }

    private void removeStandFromPlayers(ArmorStand stand) {
        for (String string : plugin.getPlayers().getKeys(false)) {
            plugin.getPlayers().getConfigurationSection(string).set(stand.getUniqueId().toString(), null);
        }
    }

    public void stopAnimation(Animation animation) {
        animation.clearStands();
    }
}
