package me.tacnayn.bettercombat.commands;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import me.tacnayn.bettercombat.BetterCombat;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PasteSchematicCommand implements CommandExecutor {

    private BetterCombat plugin;

    public PasteSchematicCommand(BetterCombat plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player p){
            if(args.length != 4) {
                p.sendMessage(ChatColor.RED + "Usage: /paste <x> <y> <z>");
                return true;
            }

            Location loc;

            try{
                loc = new Location(p.getWorld(), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            } catch (NumberFormatException e){
                p.sendMessage(ChatColor.RED + "Invalid coordinates");
                return true;
            }

            File schematic = new File("plugins/BetterCombat/dungeon_rooms/" + args[3] + ".nbt");

            if(!schematic.exists()){
                p.sendMessage(ChatColor.RED + "Schematic not found");
                return true;
            }

            p.sendMessage(ChatColor.GREEN + "Pasting schematic...");
            StructureBlockLibApi.INSTANCE
                    .loadStructure(plugin)
                    .at(loc)
                    .loadFromFile(schematic)
                    .onException(e -> p.sendMessage(ChatColor.RED + "Schematic failed to paste: " + e.getMessage()))
                    .onResult(r -> p.sendMessage(ChatColor.GREEN + "Schematic pasted successfully!"));
        }

        return true;
    }
}
