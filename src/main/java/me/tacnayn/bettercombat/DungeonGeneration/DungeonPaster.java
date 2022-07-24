package me.tacnayn.bettercombat.DungeonGeneration;

import com.comphenix.protocol.PacketType;
import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import me.tacnayn.bettercombat.BetterCombat;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.plaf.synth.Region;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class DungeonPaster {

    private BetterCombat plugin;

    public DungeonPaster(BetterCombat plugin) {
        this.plugin = plugin;
    }

    public void testPaste(CommandSender sender, Location loc) {
        StructureBlockLibApi.INSTANCE
                .loadStructure(plugin)
                .at(loc)
                .loadFromFile(new File("plugins/BetterCombat/dungeon_rooms/drip.nbt"))
                .onException(e -> {
                    sender.sendMessage(ChatColor.RED + "Structure failed to load.");
                    e.printStackTrace();
                })
                .onResult(result -> sender.sendMessage(ChatColor.GREEN + "Structure loaded successfully!"));
    }
}
