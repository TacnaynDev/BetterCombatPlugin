package me.tacnayn.bettercombat.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SwitchWorldsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player p) {
            World target = Bukkit.getWorld(args[0]);

            if(target == null) {
                p.sendMessage("World not found!");
                return true;
            }

            p.teleport(target.getSpawnLocation());
        }
        return true;
    }
}
