package ua.coolboy.armorstandeditor.animation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

public class AnimationFrame {

    private EulerAngle head, body, leftarm, rightarm, leftleg, rightleg;
    int time;

    public AnimationFrame(ConfigurationSection section) {
        this.time = section.getInt("time", 20);//default time - 1s
        if (section.isConfigurationSection("head")) {
            head = getAngle(section.getConfigurationSection("head"));
        }
        if (section.isConfigurationSection("body")) {
            body = getAngle(section.getConfigurationSection("body"));
        }
        if (section.isConfigurationSection("left_arm")) {
            leftarm = getAngle(section.getConfigurationSection("left_arm"));
        }
        if (section.isConfigurationSection("right_arm")) {
            rightarm = getAngle(section.getConfigurationSection("right_arm"));
        }
        if (section.isConfigurationSection("left_leg")) {
            leftleg = getAngle(section.getConfigurationSection("left_leg"));
        }
        if (section.isConfigurationSection("right_leg")) {
            rightleg = getAngle(section.getConfigurationSection("right_leg"));
        }
    }

    public AnimationFrame(ArmorStand stand, int time) {
        this.time = time < 0 ? 20 : time;
        head = stand.getHeadPose();
        body = stand.getBodyPose();
        leftarm = stand.getLeftArmPose();
        rightarm = stand.getRightArmPose();
        leftleg = stand.getLeftLegPose();
        rightleg = stand.getRightLegPose();
    }

    public AnimationFrame(EulerAngle head, EulerAngle body, EulerAngle leftarm, EulerAngle rightarm, EulerAngle leftleg, EulerAngle rightleg) {
        this.head = head;
        this.body = body;
        this.leftarm = leftarm;
        this.rightarm = rightarm;
        this.leftleg = leftleg;
        this.rightleg = rightleg;
        this.time = 1;
    }

    public int getTime() {
        return time;
    }

    public boolean isEmpty() {
        if (head == null
                && body == null
                && leftarm == null
                && rightarm == null
                && leftleg == null
                && rightleg == null) {
            return true;
        }
        return false;
    }

    public void clearHead() {
        head = null;
    }

    public void clearBody() {
        body = null;
    }

    public void clearLeftArm() {
        leftarm = null;
    }

    public void clearRightArm() {
        rightarm = null;
    }

    public void clearLeftLeg() {
        leftleg = null;
    }

    public void clearRightLeg() {
        rightleg = null;
    }

    public EulerAngle getHead() {
        return head;
    }

    public EulerAngle getBody() {
        return body;
    }

    public EulerAngle getLeftArm() {
        return leftarm;
    }

    public EulerAngle getRightArm() {
        return rightarm;
    }

    public EulerAngle getLeftLeg() {
        return leftleg;
    }

    public EulerAngle getRightLeg() {
        return rightleg;
    }

    public void applyToStand(ArmorStand stand) {
        //stand.teleport(stand.getLocation().add(0,1, 0));
        if (head != null) {
            stand.setHeadPose(head);
        }
        if (body != null) {
            stand.setBodyPose(body);
        }
        if (leftarm != null) {
            stand.setLeftArmPose(leftarm);
        }
        if (rightarm != null) {
            stand.setRightArmPose(rightarm);
        }
        if (leftleg != null) {
            stand.setLeftLegPose(leftleg);
        }
        if (rightleg != null) {
            stand.setRightLegPose(rightleg);
        }
    }

    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("time", time);
        if (head != null) {
            map.put("head", serializeAngle(head));
        }
        if (body != null) {
            map.put("body", serializeAngle(body));
        }
        if (leftarm != null) {
            map.put("left_arm", serializeAngle(leftarm));
        }
        if (rightarm != null) {
            map.put("right_arm", serializeAngle(rightarm));
        }
        if (leftleg != null) {
            map.put("left_leg", serializeAngle(leftleg));
        }
        if (rightleg != null) {
            map.put("right_leg", serializeAngle(rightleg));
        }
        return map;
    }

    private Map<String, Object> serializeAngle(EulerAngle angle) {
        Map<String, Object> map = new HashMap<>();
        map.put("x", angle.getX());
        map.put("y", angle.getY());
        map.put("z", angle.getZ());
        return map;
    }

    private EulerAngle getAngle(ConfigurationSection section) {
        double x = section.getDouble("x", 0);
        double y = section.getDouble("y", 0);
        double z = section.getDouble("z", 0);
        return new EulerAngle(x, y, z);
    }

}
