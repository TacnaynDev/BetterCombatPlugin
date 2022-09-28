package me.tacnayn.bettercombat.commands;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRestriction;
import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.dungeongenerator.DungeonGenerator;
import me.tacnayn.bettercombat.dungeongenerator.DungeonPaster;
import me.tacnayn.bettercombat.dungeongenerator.DungeonRoomFileManagerImpl;
import me.tacnayn.bettercombat.dungeongenerator.VoidWorldFactory;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Random;

public class GenerateDungeonCommand implements CommandExecutor {

    private BetterCombat plugin;

    public GenerateDungeonCommand(BetterCombat plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /generatedungeon <world>");
            return true;
        }

        World world = Bukkit.getWorld(args[0]);

        if(world == null){
            sender.sendMessage(ChatColor.RED + "World not found, generating new one! Server may lag for a bit...");

            world = VoidWorldFactory.getInstance().createWorld(args[0]);

            sender.sendMessage(ChatColor.GREEN + "World created!");
        }

        Random random = new Random();
        DungeonGenerator generator = new DungeonGenerator(random);
        DungeonPaster paster = new DungeonPaster(plugin, DungeonRoomFileManagerImpl.getInstance(), generator);

        paster.generateAndPasteDungeon(world, random);

        return true;
    }
}
