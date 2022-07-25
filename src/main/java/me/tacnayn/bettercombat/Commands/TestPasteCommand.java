package me.tacnayn.bettercombat.commands;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.dungeongeneration.DungeonPaster;
import me.tacnayn.bettercombat.dungeongeneration.DungeonRoomFileManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestPasteCommand implements CommandExecutor {

    private BetterCombat plugin;

    public TestPasteCommand(BetterCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player p) {
            if (args.length != 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /testpaste <x> <y> <z>");
                return true;
            }

            int x, y, z;
            try {
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Coordinates must be integers");
                return true;
            }

            Location loc = new Location(p.getWorld(), x, y, z);

            new DungeonPaster(plugin, DungeonRoomFileManager.getInstance()).testPaste(sender, loc);
        }

        return true;
    }
}