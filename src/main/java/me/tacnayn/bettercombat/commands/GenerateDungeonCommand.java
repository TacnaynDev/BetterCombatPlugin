package me.tacnayn.bettercombat.commands;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.api.dungeongeneration.IDungeonRoomFileManager;
import me.tacnayn.bettercombat.dungeongeneration.DungeonPaster;
import me.tacnayn.bettercombat.dungeongeneration.DungeonRoomFileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GenerateDungeonCommand implements CommandExecutor {

    private BetterCombat plugin;

    public GenerateDungeonCommand(BetterCombat plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        new DungeonPaster(plugin, DungeonRoomFileManager.getInstance()).pasteDungeon();

        return true;
    }
}
