package ua.coolboy.armorstandeditor.glow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GlowPacket {

    private Entity entity;
    private boolean glow;
    private boolean supported;
    private Object packet;

    public GlowPacket(Entity entity, boolean glow) {
        this(entity, glow, Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
    }

    public GlowPacket(Entity entity, boolean glow, String version) {
        this.entity = entity;
        this.glow = glow;
        switch (version) {
            case "v1_12_R1":
                init(58);
                break;
            case "v1_13_R1":
                init(61);
                break;
            default:
                supported = false;
        }
    }
    private void init(int dataWatcherObjectPos) {
        try {
            Class<?> packetClass = getNMSClass("PacketPlayOutEntityMetadata");

            Object nmsEntity = invokeMethod(entity.getClass().getMethod("getHandle"), entity);

            Object dataWatcher = invokeMethod(nmsEntity.getClass().getMethod("getDataWatcher"), nmsEntity);

            Field d = dataWatcher.getClass().getDeclaredField("d");
            Object item = ((Map<Integer, Object>) getField(d, dataWatcher)).get(0);

            Method mb = item.getClass().getMethod("b");

            Field field = getNMSClass("Entity").getDeclaredFields()[dataWatcherObjectPos]; //magic from GlowAPI, may change in every version
            Object dataWatcherObject = getField(field, nmsEntity);

            byte prev = (byte) mb.invoke(item);
            byte b = (byte) (glow ? (prev | 1 << 6) : (prev & ~(1 << 6)));//6 = glowing index, also magic from GlowAPI

            List list = new ArrayList<>();
            list.add(item.getClass().getConstructors()[0].newInstance(dataWatcherObject, b));

            packet = packetClass.newInstance();
            setField(packetClass.getDeclaredField("a"), packet, entity.getEntityId());
            setField(packetClass.getDeclaredField("b"), packet, list);

            supported = true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to prepare glow packet!", e);
        }
    }

    public void send(Player player) {
        if (!supported) return;
        if (!player.isOnline()) return;
        sendPacket(player, packet);
    }

    public Entity getEntity() {
        return entity;
    }

    public Object getPacket() {
        return packet;
    }

    public boolean shouldGlow() {
        return glow;
    }

    private void setField(Field field, Object object, Object value) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        field.set(object, value);
    }

    private Object getField(Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        return field.get(object);
    }

    private Object invokeMethod(Method method, Object object, Object... arguments) throws IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return method.invoke(object, arguments);
    }

    private Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get NMS class!", e);
        }
        return null;
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to send packet!", e);
        }
    }

}
