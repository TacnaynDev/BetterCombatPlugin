package me.tacnayn.bettercombat.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UnloadWorldCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unloadworld <worldname>");
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        if(world == null) {
            sender.sendMessage(ChatColor.RED + "World not found");
            return true;
        }

        world.getPlayers().forEach(player -> player.kickPlayer("Unloading world"));
        boolean successful = Bukkit.unloadWorld(world, false);

        if(successful) sender.sendMessage(ChatColor.GREEN + "World unloaded successfully!");
        else sender.sendMessage(ChatColor.RED + "World could not be unloaded.");

        return true;
    }
}
