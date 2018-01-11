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

import com.sun.management.OperatingSystemMXBean;
import ua.coolboy.armorstandeditor.animation.Animation;
import ua.coolboy.armorstandeditor.animation.AnimationEditor;
import ua.coolboy.armorstandeditor.animation.AnimationManager;
import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import java.lang.management.ManagementFactory;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.coolboy.armorstandeditor.menu.PlayerAnimationsMenu;

public class CommandEx implements CommandExecutor {

    ArmorStandEditorPlugin plugin;
    final String LISTMODE = ChatColor.GREEN + "/ase mode <" + Util.getEnumList(EditMode.class) + ">";
    final String LISTAXIS = ChatColor.GREEN + "/ase axis <" + Util.getEnumList(Axis.class) + ">";
    final String LISTADJUSTMENT = ChatColor.GREEN + "/ase adj <" + Util.getEnumList(AdjustmentMode.class) + ">";
    final String LISTSLOT = ChatColor.GREEN + "/ase slot <1-9>";
    final String HELP = ChatColor.GREEN + "/ase help";
    final String ANIMATION1 = ChatColor.GREEN + "/ase animation";
    final String ANIMATION2 = ChatColor.GREEN + "/ase animation new";
    final String ANIMATION3 = ChatColor.GREEN + "/ase animation add [ticks]>";
    final String ANIMATION4 = ChatColor.GREEN + "/ase animation save [name]";

    public CommandEx(ArmorStandEditorPlugin armorStandEditorPlugin) {
        this.plugin = armorStandEditorPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player
                && checkPermission((Player) sender, "basic", true))) {
            sender.sendMessage(plugin.getLang().getMessage("noperm", "warn"));
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(LISTMODE);
            player.sendMessage(LISTAXIS);
            player.sendMessage(LISTSLOT);
            player.sendMessage(LISTADJUSTMENT);
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "mode":
                    commandMode(player, args);
                    break;
                case "axis":
                    commandAxis(player, args);
                    break;
                case "adj":
                    commandAdj(player, args);
                    break;
                case "slot":
                    commandSlot(player, args);
                    break;
                case "animation":
                    if(plugin.isAnimationsEnabled()) {
                    commandAnim(player, args);
                    } else {
                        player.sendMessage("Animations disabled in config!");
                    }
                    break;
                case "debug":
                    player.sendMessage("Current memory: " + (Runtime.getRuntime().totalMemory() / 1024 / 1024 - Runtime.getRuntime().freeMemory() / 1024 / 1024));
                    ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
                    player.sendMessage("Active threads: " + rootGroup.activeCount());
                    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                            OperatingSystemMXBean.class);
                    player.sendMessage("CPU load: " + osBean.getProcessCpuLoad() + " of total: " +osBean.getSystemCpuLoad());
                    break;
                case "help":
                case "?":
                    commandHelp(player);
                    break;
                default:
                    sender.sendMessage(LISTMODE);
                    sender.sendMessage(LISTAXIS);
                    sender.sendMessage(LISTSLOT);
                    sender.sendMessage(LISTADJUSTMENT);
                    sender.sendMessage(HELP);
            }
            return true;
        }
        return true;
    }

    private void commandSlot(Player player, String[] args) {

        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noslotnumcom", "warn"));
            player.sendMessage(LISTSLOT);
        }

        if (args.length > 1) {
            try {
                byte slot = (byte) (Byte.parseByte(args[1]) - 0b1);
                if (slot >= 0 && slot < 9) {
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setCopySlot(slot);
                } else {
                    player.sendMessage(LISTSLOT);
                }

            } catch (NumberFormatException nfe) {
                player.sendMessage(LISTSLOT);
            }
        }
    }

    private void commandAdj(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noadjcom", "warn"));
            player.sendMessage(LISTADJUSTMENT);
        }

        if (args.length > 1) {
            for (AdjustmentMode adj : AdjustmentMode.values()) {
                if (adj.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAdjMode(adj);
                    return;
                }
            }
            player.sendMessage(LISTADJUSTMENT);
        }
    }

    private void commandAxis(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noaxiscom", "warn"));
            player.sendMessage(LISTAXIS);
        }

        if (args.length > 1) {
            for (Axis axis : Axis.values()) {
                if (axis.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAxis(axis);
                    return;
                }
            }
            player.sendMessage(LISTAXIS);
        }
    }

    private void commandMode(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("nomodecom", "warn"));
            player.sendMessage(LISTMODE);
        }

        if (args.length > 1) {
            for (EditMode mode : EditMode.values()) {
                if (mode.toString().toLowerCase().contentEquals(args[1].toLowerCase()) && checkPermission(player, args[1], true)) {
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(mode);
                    return;
                }
            }
        }
    }

    private void commandAnim(Player player, String[] args) {
        AnimationManager manager = plugin.animManager;
        if (args.length == 1) {
            plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(EditMode.ANIMATION);
            player.sendMessage("Click on stand to get animation menu.");
        }
        if (args.length == 2) {
            if (args[1].equals("help")) {

            }
            if (args[1].equals("new")) {
                manager.addPlayerToPreEdit(player);
                player.sendMessage("You entered animation edit mode. Click on stand with edit tool to choose it.");
            }
            if (args[1].equals("add")) {
                addFrame(player, -1);
            }
            if (args[1].equals("leave")) {
                manager.removePlayerFromPreEdit(player);
                manager.removePlayerEditor(player);
            }
            if (args[1].equals("save")) {
                manager.save(player);
            }
            if (args[1].equals("list")) {
                new PlayerAnimationsMenu(player).open();
            }
        }
        if (args.length == 3) {
            if (args[1].equals("add")) {
                try {
                    int i = Integer.valueOf(args[2]);
                    addFrame(player, i);
                } catch (NumberFormatException ex) {
                    player.sendMessage("Please, type a number");
                }
            }
            
            if (args[1].equals("remove")) {
                Animation animation = manager.getById(args[2]);
                if(animation != null) {
                    manager.stopAnimation(animation);
                    manager.deleteAnimation(animation);
                } else {
                    player.sendMessage("Can't find animation with id " + args[2]);
                }
            }

            /*if (args[1].equals("start")) {
                Animation animation = manager.getById(args[2]);
                if (animation != null) {
                    manager.runAnimation(animation);
                } else {
                    player.sendMessage("Can't find animation with id " + args[2]);
                }
            }*/

            if (args[1].equals("stop")) {
                if (args[2].equalsIgnoreCase("all")) {
                    for (Animation animation : plugin.animManager.getAnimations()) {
                        plugin.animManager.stopAnimation(animation);
                    }
                } else {
                    Animation animation = manager.getById(args[2]);
                    if (animation != null) {
                        manager.stopAnimation(animation);
                    } else {
                        player.sendMessage("Can't find animation: " + args[2]);
                    }
                }
            }

            if (args[1].equals("list")) {
                OfflinePlayer who = Bukkit.getOfflinePlayer(args[2]);
                if (who != null) {
                    new PlayerAnimationsMenu(who).open(player);
                } else {
                    player.sendMessage("Can't find player " + args[2]);
                }
            }

            if (args[1].equals("save")) {
                manager.save(player, args[2]);
            }
        }
        if (args.length == 4) {
            if (args[1].equals("rename")) {
                if (manager.rename(args[2], args[3])) {
                    player.sendMessage("Successfully renamed animation " + args[2]);
                } else {
                    player.sendMessage("Can't rename animation " + args[2]);
                }
            }
        }
    }

    private void addFrame(Player player, int time) {
        AnimationEditor editor = plugin.animManager.getEditor(player);
        if (editor == null) {
            player.sendMessage("You're not in edit mode!");
        } else {
            editor.addFrame(time);
            player.sendMessage("Frame " + editor.getFrames().size() + " added!");
        }
    }

    private void commandHelp(Player player) {
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.sendMessage(plugin.getLang().getMessage("help", "info", plugin.editTool.name()));
        player.sendMessage("");
        player.sendMessage(plugin.getLang().getMessage("helptips", "info"));
        player.sendMessage("");
        player.sendRawMessage(plugin.getLang().getMessage("helpurl", ""));
    }

    private boolean checkPermission(Player player, String permName, boolean sendMessageOnInvalidation) {
        if (permName.toLowerCase().equals("paste")) {
            permName = "copy";
        }
        if (player.hasPermission("asedit." + permName.toLowerCase())) {
            return true;
        } else {
            if (sendMessageOnInvalidation) {
                player.sendMessage(plugin.getLang().getMessage("noperm", "warn"));
            }
            return false;
        }
    }
}
