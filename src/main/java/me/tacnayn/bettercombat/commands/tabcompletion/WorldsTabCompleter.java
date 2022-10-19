package me.tacnayn.bettercombat.commands.tabcompletion;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;

public class WorldsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }
        return null;
    }
}
