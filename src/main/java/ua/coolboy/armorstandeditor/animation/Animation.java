package ua.coolboy.armorstandeditor.animation;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public class Animation implements Runnable {

    private UUID owner;
    private ConfigurationSection section;
    private List<ArmorStand> stands;
    private List<AnimationFrame> frames;
    private List<AnimationFrame> ticks;
    private ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();
    private boolean stop = false;
    private boolean pause = false;
    private String id;

    public Animation(String id, ConfigurationSection section) {
        this.id = id;
        this.section = section;
        stands = new ArrayList<>();
        owner = UUID.fromString(section.getString("creator"));
        frames = new ArrayList<>();
        ticks = new ArrayList<>();
        for (String key : section.getConfigurationSection("frames").getKeys(false)) {
            frames.add(new AnimationFrame(section.getConfigurationSection("frames." + key)));
        }

        for (int i = 0; i < frames.size(); i++) {
            int n = i + 1 == frames.size() ? 0 : i + 1;
            ticks.addAll(smooth(frames.get(i), frames.get(n)));
        }
        if (plugin.getAnimationRate() != 20) {
            List<AnimationFrame> remove = new ArrayList<>();
            for (int i = 0; i < frames.size(); i++) {
                if (i % plugin.getAnimationRate() == 0) {//Every n element should be true
                    remove.add(ticks.get(i));
                }
            }
            ticks.removeAll(remove);
        }
    }

    public void restart() {
        plugin.animManager.getExecutor().submit(this);
    }

    public void clearStands() {
        stands.clear();
    }

    public void addStand(ArmorStand stand) {
        if (stands.contains(stand)) {
            return;
        }
        plugin.animManager.removeStandFromAnimations(stand);
        stands.add(stand);
    }

    public boolean containsStand(ArmorStand stand) {
        for (ArmorStand stnd : stands) {
            if (stnd.getUniqueId().equals(stand.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        stop = true;
    }

    public void removeStand(ArmorStand stand) {
        stands.remove(stand);
    }
    
    public boolean isStopped() {
        return stop;
    }
    
    public void resetPose() {
        AnimationFrame first = frames.get(0);
        for (ArmorStand stand : stands) {
            first.applyToStand(stand);
        }
    }

    @Override
    public void run() {
        int currentFrame = 0;
        int wait = (int) (1000 / plugin.getAnimationRate());
        stop = false;
        List<ArmorStand> toRemove = new ArrayList<>();
        synchronized (this) {
            while (true) {
                if(stop) break;
                try {
                    this.wait(wait);
                } catch (InterruptedException ex) {
                    Bukkit.getLogger().info("Shutting down animation!");
                    stop();
                }
                
                if (stands.isEmpty()) {
                    continue;
                }

                if (!toRemove.isEmpty()) {
                    stands.removeAll(toRemove);
                }

                for (ArmorStand stand : stands) {
                    if (!stand.isDead()) {
                        ticks.get(currentFrame).applyToStand(stand);
                    } else {
                        toRemove.add(stand);
                    }
                }
                currentFrame++;
                if (currentFrame >= ticks.size()) {
                    currentFrame = 0;
                }
            }
        }
    }

    private List<AnimationFrame> smooth(AnimationFrame one, AnimationFrame two) {
        int n = one.getTime();
        List<EulerAngle> head = null, body = null, leftarm = null, rightarm = null, leftleg = null, rightleg = null;
        List<AnimationFrame> frames = new ArrayList<>();
        if (one.getHead() != null && two.getHead() != null) {
            head = smooth(one.getHead(), two.getHead(), n);
        }
        if (one.getBody() != null && two.getBody() != null) {
            body = smooth(one.getBody(), two.getBody(), n);
        }
        if (one.getLeftArm() != null && two.getLeftArm() != null) {
            leftarm = smooth(one.getLeftArm(), two.getLeftArm(), n);
        }
        if (one.getRightArm() != null && two.getRightArm() != null) {
            rightarm = smooth(one.getRightArm(), two.getRightArm(), n);
        }
        if (one.getLeftLeg() != null && two.getLeftLeg() != null) {
            leftleg = smooth(one.getLeftLeg(), two.getLeftLeg(), n);
        }
        if (one.getRightLeg() != null && two.getRightLeg() != null) {
            rightleg = smooth(one.getRightLeg(), two.getRightLeg(), n);
        }
        for (int i = 0; i < n; i++) {
            frames.add(new AnimationFrame(getOrNull(head, i), getOrNull(body, i), getOrNull(leftarm, i), getOrNull(rightarm, i), getOrNull(leftleg, i), getOrNull(rightleg, i)));
        }
        return frames;
    }

    private EulerAngle getOrNull(List<EulerAngle> list, int i) {
        if (list == null || list.size() <= i) {
            return null;
        }
        return list.get(i);
    }

    private List<EulerAngle> smooth(EulerAngle one, EulerAngle two, int count) {
        if (count == 0) {
            return Collections.EMPTY_LIST;
        }
        List<EulerAngle> angles = new ArrayList<>();
        EulerAngle diff = two.subtract(one.getX(), one.getY(), one.getZ());
        diff = divide(diff, count);
        for (int i = 0; i < count; i++) {
            angles.add(add(one, multiply(diff, i)));
        }

        return angles;
    }

    private EulerAngle add(EulerAngle one, EulerAngle two) {
        return new EulerAngle(one.getX() + two.getX(), one.getY() + two.getY(), one.getZ() + two.getZ());
    }

    private EulerAngle multiply(EulerAngle angle, double to) {
        return new EulerAngle(angle.getX() * to, angle.getY() * to, angle.getZ() * to);
    }

    private EulerAngle divide(EulerAngle angle, double to) {
        return new EulerAngle(angle.getX() / to, angle.getY() / to, angle.getZ() / to);
    }

    public List<AnimationFrame> getFrames() {
        return frames;
    }

    public UUID getCreator() {
        return owner;
    }

    public List<ArmorStand> getStands() {
        return stands;
    }

    public String getID() {
        return id;
    }

    public ConfigurationSection getConfiguration() {
        return section;
    }
}
