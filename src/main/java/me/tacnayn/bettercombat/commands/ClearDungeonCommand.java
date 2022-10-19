package me.tacnayn.bettercombat.commands;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.entity.ProgressToken;
import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.dungeongenerator.DungeonPaster;
import me.tacnayn.bettercombat.dungeongenerator.DungeonRoomFileManagerImpl;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ClearDungeonCommand implements CommandExecutor {
    private final BetterCombat plugin;

    public ClearDungeonCommand(BetterCombat plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /cleardungeon <world>");
            return true;
        }

        World world = Bukkit.getWorld(args[0]);

        if(world == null) {
            sender.sendMessage(ChatColor.RED + "World not found!");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Clearing dungeon...");

        clearDungeon(world, 13);

        return true;
    }

    private void clearDungeon(World world, int dungeonSize){
        for(int x = 0; x < dungeonSize; x++) {
            for(int z = 0; z < dungeonSize; z++) {
                clearTile(world, x, z);
            }
        }
    }

    private void clearTile(World world, int tileX, int tileZ){

        File blankTile = DungeonRoomFileManagerImpl.getInstance().blankRoomFile(24);

        int locX = tileX * DungeonPaster.SMALL_ROOM_SIZE;
        int locZ = tileZ * DungeonPaster.SMALL_ROOM_SIZE;
        Location loc = new Location(world, locX, DungeonPaster.DUNGEON_Y_LEVEL, locZ);

        ProgressToken<Void> progressToken = StructureBlockLibApi.INSTANCE
                .loadStructure(plugin)
                .at(loc)
                .onProcessBlock(part -> part.getTargetBlock().getBlockData().getMaterial() != Material.AIR) // Don't paste over air blocks
                .loadFromFile(blankTile)
                .onException(Throwable::printStackTrace);
    }
}
