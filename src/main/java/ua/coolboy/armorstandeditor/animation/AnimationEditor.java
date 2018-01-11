package ua.coolboy.armorstandeditor.animation;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;

public class AnimationEditor {

    private Player player;
    private ArmorStandEditorPlugin plugin;
    private List<AnimationFrame> frames;
    private ArmorStand stand;

    public AnimationEditor(Player player, ArmorStand stand) {
        plugin = ArmorStandEditorPlugin.instance();
        this.player = player;
        frames = new ArrayList<>();
        this.stand = stand;
    }

    public List<AnimationFrame> getFrames() {
        return frames;
    }

    public Player getPlayer() {
        return player;
    }

    public ArmorStand getStand() {
        return stand;
    }

    public int framesCount() {
        return frames.size();
    }

    public void addFrame(int time) {
        frames.add(new AnimationFrame(stand, time));
    }

    public void removeLastFrame() {
        if (frames.isEmpty()) {
            return;
        }
        frames.remove(frames.size() - 1);
    }

    public void save(String id) {
        if (canAddAnimation(player)) {
            optimize();
            File cfg = new File(plugin.getAnimationFolder(),id);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(cfg);
            config.set(id, serialize());
            try {
                config.save(cfg);
            } catch (IOException ex) {
                Bukkit.getLogger().severe("Can't save animation!\n"+ex.getMessage());
            }
            plugin.reloadAnimations();
        }
    }

    private void optimize() {
        if (frames.size() < 2) {
            return;
        }
        List<EulerAngle> head = new ArrayList<>();
        List<EulerAngle> body = new ArrayList<>();
        List<EulerAngle> leftarm = new ArrayList<>();
        List<EulerAngle> rightarm = new ArrayList<>();
        List<EulerAngle> leftleg = new ArrayList<>();
        List<EulerAngle> rightleg = new ArrayList<>();
        for (AnimationFrame frame : frames) {
            head.add(frame.getHead());
            body.add(frame.getBody());
            leftarm.add(frame.getLeftArm());
            rightarm.add(frame.getRightArm());
            leftleg.add(frame.getLeftLeg());
            rightleg.add(frame.getRightLeg());
        }
        if (allElementsTheSame(head)) {
            for (int i = 1; i < frames.size(); i++) {
                frames.get(i).clearHead();
            }
        }
        if (allElementsTheSame(body)) {
            for (int i = 1; i < frames.size(); i++) {
                frames.get(i).clearBody();
            }
        }
        if (allElementsTheSame(leftarm)) {
            for (int i = 1; i < frames.size(); i++) {
                frames.get(i).clearLeftArm();
            }
        }
        if (allElementsTheSame(rightarm)) {
            for (int i = 1; i < frames.size(); i++) {
                frames.get(i).clearRightArm();
            }
        }
        if (allElementsTheSame(leftleg)) {
            for (int i = 1; i < frames.size(); i++) {
                frames.get(i).clearLeftLeg();
            }
        }
        if (allElementsTheSame(rightleg)) {
            for (int i = 1; i < frames.size(); i++) {
                frames.get(i).clearRightLeg();
            }
        }
    }

    private boolean allElementsTheSame(List list) {
        return list.stream().allMatch(e -> e.equals(list.get(0)));
    }

    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("creator", player.getUniqueId().toString());
        TreeMap<String, Object> frameMap = new TreeMap<>();
        int i = 0;
        for (AnimationFrame frame : frames) {
            frameMap.put(String.valueOf(i), frame.serialize());
            i++;
        }
        map.put("frames", frameMap);
        return map;
    }

    public static boolean canAddAnimation(Player player) {
        return player.hasPermission("asedit.animation.create");
    }
    
    public Player getCreator() {
        return player;
    }
    
}
