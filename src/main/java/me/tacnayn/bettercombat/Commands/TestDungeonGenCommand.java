package me.tacnayn.bettercombat.Commands;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.DungeonGeneration.DungeonGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class TestDungeonGenCommand implements CommandExecutor {

    BetterCombat plugin;
    final String usage = ChatColor.RED + "/testdungeongen <count>";

    public TestDungeonGenCommand(BetterCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        int dungeonsToGenerate;

        if(args.length != 1){
            sender.sendMessage(usage);
            return true;
        }

        try {
            dungeonsToGenerate = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Count must be an integer");
            return true;
        }

        System.out.println("Generating " + dungeonsToGenerate + " dungeons");

        new BukkitRunnable() {
            @Override
            public void run() {
                for(int i = 0; i < dungeonsToGenerate; i++) {
                    new DungeonGenerator().printDungeonPlan(sender);
                }

                System.out.println("Done!");
            }
        }.runTaskAsynchronously(plugin);

        System.out.println("Task started!");

        return true;
    }
}