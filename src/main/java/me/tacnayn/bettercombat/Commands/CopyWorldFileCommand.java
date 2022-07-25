package me.tacnayn.bettercombat.commands;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;

public class CopyWorldFileCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Error checking
        if(args.length == 0) {
            sender.sendMessage("Usage: /loadworld <worldname>");
            return true;
        }

        // Kick players and unload if the world is already loaded
        World world = Bukkit.getWorld(args[0]);
        if(world != null) {
            world.getPlayers().forEach(p -> p.kickPlayer("World is being reloaded!"));
            Bukkit.unloadWorld(world, false);
        }

        // Copy world file to server folder
        try {
            FileUtils.copyDirectory(new File("plugins/BetterCombat/test_worlds/" + args[0]), new File(args[0]));
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "World could not be copied (did you spell the name correctly?)");
        }

        sender.sendMessage("World successfully loaded!");

        return true;
    }
}
