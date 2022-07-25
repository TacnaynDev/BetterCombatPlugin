package me.tacnayn.bettercombat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LoadWorldCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /loadworld <worldname>");
            return true;
        }

        World world = Bukkit.createWorld(new WorldCreator(args[0]));
        if(world != null) sender.sendMessage(ChatColor.GREEN + "World loaded successfully!");
        else sender.sendMessage(ChatColor.RED + "World could not be loaded.");

        return true;
    }
}
