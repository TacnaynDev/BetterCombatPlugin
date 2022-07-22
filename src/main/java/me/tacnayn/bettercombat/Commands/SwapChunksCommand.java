package me.tacnayn.bettercombat.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jglrxavpok.hephaistos.data.DataSource;
import org.jglrxavpok.hephaistos.mca.AnvilException;
import org.jglrxavpok.hephaistos.mca.ChunkColumn;
import org.jglrxavpok.hephaistos.mca.RegionFile;
import org.jglrxavpok.hephaistos.mca.SupportedVersion;

import javax.xml.crypto.Data;
import java.io.*;

public class SwapChunksCommand implements CommandExecutor {

    String usage = ChatColor.RED + "/swapchunks <sourceWorld> <targetWorld> <chunkX> <chunkZ>";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Error checking
        if(args.length != 4) {
            sender.sendMessage(usage);
            return true;
        }

        File sourceWorld;
        File targetWorld;

        // Get chunk coordinates
        int chunkX;
        int chunkZ;
        try {
            chunkX = Integer.parseInt(args[2]);
            chunkZ = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Chunk coordinates must be integers");
            return true;
        }

        // Get region file coords
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;

        // Locate files
        try{
            sourceWorld = new File(args[0] + "\\region\\r." + regionX + "." + regionZ + ".mca");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Source world could not be found!");
            return true;
        }

        try{
            targetWorld = new File(args[1] + "\\region\\r." + regionX + "." + regionZ + ".mca");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Target world could not be found!");
            return true;
        }

        // Get region files
        // Region file looks like this: r.<x>.<z>.mca
        /*String sourceRegionPath = sourceWorld.getWorldFolder().getAbsolutePath() + File.separator + "region" + File.separator + "r." + regionX + "." + regionZ + ".mca";
        String targetRegionPath = targetWorld.getWorldFolder().getAbsolutePath() + File.separator + "region" + File.separator + "r." + regionX + "." + regionZ + ".mca";
*/
        RegionFile sourceRegion;
        RegionFile targetRegion;

        try {
            sourceRegion = new RegionFile(new RandomAccessFile(sourceWorld, "rw"), regionX, regionZ);
            targetRegion = new RegionFile(new RandomAccessFile(targetWorld, "rw"), regionX, regionZ);

            ChunkColumn sourceChunk = sourceRegion.getChunk(chunkX, chunkZ);

            if(sourceChunk == null) {
                sender.sendMessage(ChatColor.RED + "Source chunk not found");
                return true;
            }

            targetRegion.writeColumn(sourceChunk, SupportedVersion.MC_1_18_PRE_4);

        } catch (AnvilException | IOException e) {
            sender.sendMessage(ChatColor.RED + "Error while swapping chunks");
            throw new RuntimeException(e);
        }

        sender.sendMessage(ChatColor.GREEN + "Chunk swapping successful!");

        return true;
    }
}
