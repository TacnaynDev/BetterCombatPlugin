package me.tacnayn.bettercombat.Commands;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.DungeonGeneration.DungeonPaster;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TestPasteCommand implements CommandExecutor {

    private BetterCombat plugin;

    public TestPasteCommand(BetterCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        if(new DungeonPaster(plugin).testPaste(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Paste successful!");
        } else {
            sender.sendMessage(ChatColor.RED + "Paste failed!");
        }

        return true;
    }
}