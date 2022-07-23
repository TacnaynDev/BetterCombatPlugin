package me.tacnayn.bettercombat.Commands;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.DungeonGeneration.DungeonPaster;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.mca.BlockState;

import java.io.File;
import java.nio.file.Files;

public class FillBlocksThroughFileCommand implements CommandExecutor {

    private final BetterCombat plugin;
    private final String usage = ChatColor.RED + "/fillblocksthroughfile <world> <x1> <y1> <z1> <x2> <y2> <z2>";

    public FillBlocksThroughFileCommand(BetterCombat plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length != 7) {
            sender.sendMessage(usage);
            return true;
        }

        int x1, y1, z1, x2, y2, z2;

        try {
            x1 = Integer.parseInt(args[1]);
            y1 = Integer.parseInt(args[2]);
            z1 = Integer.parseInt(args[3]);
            x2 = Integer.parseInt(args[4]);
            y2 = Integer.parseInt(args[5]);
            z2 = Integer.parseInt(args[6]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Coordinates must be integers");
            return true;
        }

        if(!new File(args[0]).exists()) {
            sender.sendMessage(ChatColor.RED + "World could not be found!");
            return true;
        }

/*        World world = Bukkit.getWorld(args[0]);
        if(world != null) {
            sender.sendMessage(ChatColor.RED + "World is loaded!");
            return true;
        }*/

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                boolean result = new DungeonPaster(plugin).fillArea(args[0], new BlockState("minecraft:diamond_block"), x1, y1, z1, x2, y2, z2);

                if(result) System.out.println("Success!");
                else System.out.println("Failed!");
            }
        }.runTaskAsynchronously(plugin);



        return true;
    }
}
