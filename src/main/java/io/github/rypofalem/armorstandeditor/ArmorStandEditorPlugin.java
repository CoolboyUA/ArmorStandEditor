/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.github.rypofalem.armorstandeditor;

import ua.coolboy.armorstandeditor.animation.AnimationManager;
import io.github.rypofalem.armorstandeditor.language.Language;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.coolboy.armorstandeditor.animation.Animation;

public class ArmorStandEditorPlugin extends JavaPlugin {

    private static ArmorStandEditorPlugin instance;
    private CommandEx execute;
    private Language lang;

    private File animationsFolder;
    private File playersFile;
    private YamlConfiguration players;
    public AnimationManager animManager;
    public boolean animations;
    public boolean allAnimations;
    public double animationUpdateRate;
    public int animationLimit;
    public int maxFrames = 20;

    public PlayerEditorManager editorManager;
    public Material editTool = Material.FLINT;
    boolean requireToolData = false;
    boolean sendToActionBar = true;
    int editToolData = Integer.MIN_VALUE;
    boolean requireToolLore = false;
    String editToolLore = null;
    boolean debug = false; //weather or not to broadcast messages via print(String message)
    double coarseRot;
    double fineRot;

    @Override
    public void onEnable() {
        this.instance = this;
        //saveResource doesn't accept File.seperator on windows, need to hardcode unix seperator "/" instead
        updateConfig("", "config.yml");
        updateConfig("lang/", "test_NA.yml");
        updateConfig("lang/", "nl_NL.yml");
        updateConfig("lang/", "uk_UA.yml");
        updateConfig("lang/", "zh.yml");
        updateConfig("lang/", "fr_FR.yml");
        updateConfig("lang/", "ro_RO.yml");
        updateConfig("lang/", "ja_JP.yml");
        //English is the default language and needs to be unaltered to so that there is always a backup message string
        saveResource("lang/en_US.yml", true);
        lang = new Language(getConfig().getString("lang"), this);

        coarseRot = getConfig().getDouble("coarse");
        fineRot = getConfig().getDouble("fine");
        String toolType = getConfig().getString("tool", "FLINT");
        editTool = Material.getMaterial(toolType);
        requireToolData = getConfig().getBoolean("requireToolData", false);
        if (requireToolData) {
            editToolData = getConfig().getInt("toolData", 0);
        }
        requireToolLore = getConfig().getBoolean("requireToolLore", false);
        if (requireToolLore) {
            editToolLore = getConfig().getString("toolLore", "");
        }
        debug = getConfig().getBoolean("debug", true);
        sendToActionBar = getConfig().getBoolean("sendMessagesToActionBar", isSpigot());

        animations = getConfig().getBoolean("animations.enabled", false);
        animationLimit = getConfig().getInt("animations.limit", 3);
        animationUpdateRate = Math.min(getConfig().getDouble("animations.updateRate", 20),20); //limit to 20
        allAnimations = getConfig().getBoolean("animations.allAnimations", false);

        if (animations) {
            playersFile = new File(this.getDataFolder(), "players.yml");
            animationsFolder = new File(this.getDataFolder(), "animations");
            if (!playersFile.exists() || !animationsFolder.exists()) {
                try {
                    playersFile.createNewFile();
                    animationsFolder.mkdir();
                } catch (IOException ex) {
                    Bukkit.getLogger().warning("Can't create file!\n" + ex.getMessage());
                }
            }
            loadAnimations();
        }
        editorManager = new PlayerEditorManager(this);
        execute = new CommandEx(this);
        getCommand("ase").setExecutor(execute);
        getServer().getPluginManager().registerEvents(editorManager, this);
    }
    
    public boolean isAnimationsEnabled() {
        return animations;
    }
    
    public double getAnimationRate() {
        return animationUpdateRate;
    }
    
    private void loadAnimations() {
        players = YamlConfiguration.loadConfiguration(playersFile);
        animManager = new AnimationManager();
    }

    private void updateConfig(String folder, String config) {
        if (!new File(getDataFolder() + File.separator + folder + config).exists()) {
            saveResource(folder + config, false);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() == null) {
                continue;
            }
            if (player.getOpenInventory().getTopInventory().getHolder() == editorManager.getPluginHolder()) {
                player.closeInventory();
            }
        }
        for (Animation animation : animManager.getAnimations()) {
            animation.resetPose();
        }
        stopAnimations();
    }

    private void stopAnimations() {
        animManager.getAnimations().stream().forEach((animation) ->animation.stop());
        animManager.getExecutor().shutdownNow();
    }

    public boolean isSpigot() {
        try {
            Class.forName("org.bukkit.entity.Player.Spigot"); //Checking for Player.spigot()
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public YamlConfiguration getPlayers() {
        return players;
    }

    public void savePlayers() {
        try {
            players.save(playersFile);
        } catch (IOException ex) {
            log("Can't save animations!");
        }
    }

    public void reloadPlayers() {
        players = YamlConfiguration.loadConfiguration(playersFile);
    }

    public void reloadAnimations() {
        animManager = new AnimationManager();
    }

    public File getAnimationFolder() {
        return animationsFolder;
    }

    public void log(String message) {
        this.getServer().getLogger().info("[ArmorStandEditor] " + message);
    }

    public void print(String message) {
        if (debug) {
            this.getServer().broadcastMessage(message);
        }
    }

    public String listPlugins() {
        Plugin[] plugins = getServer().getPluginManager().getPlugins();
        String list = "";
        for (Plugin p : plugins) {
            if (p != null) {
                list = list + " :" + p.getName() + " " + p.getDescription().getVersion() + ": ";
            }
        }
        return list;
    }

    public static ArmorStandEditorPlugin instance() {
        return instance;
    }

    public Language getLang() {
        return lang;
    }

    public boolean isEditTool(ItemStack item) {
        if (item == null) {
            return false;
        }
        if (editTool != item.getType()) {
            return false;
        }
        if (requireToolData && item.getDurability() != (short) editToolData) {
            return false;
        }
        if (requireToolLore && !editToolLore.isEmpty()) {
            if (!item.hasItemMeta()) {
                return false;
            }
            if (!item.getItemMeta().hasLore()) {
                return false;
            }
            print("has Lore");
            if (item.getItemMeta().getLore().isEmpty()) {
                return false;
            }
            print("lore not empty");
            if (!item.getItemMeta().getLore().get(0).equals(editToolLore)) {
                return false;
            }
        }
        return true;
    }
}
//todo: 
//Access to "DisabledSlots" data (probably simplified just a toggle enable/disable)
//Access to the "Marker" switch (so you can make the hitbox super small)
