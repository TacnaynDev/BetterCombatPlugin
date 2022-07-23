package me.tacnayn.bettercombat.DungeonGeneration;

import com.comphenix.protocol.PacketType;
import me.tacnayn.bettercombat.BetterCombat;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jglrxavpok.hephaistos.mca.*;

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

    public boolean testPaste(CommandSender sender){
        try {

            // create the region from a given DataSource. 0,0 is the region coordinates (a region is 32x32 chunks)
            RegionFile region = new RegionFile(new RandomAccessFile(new File("world2\\region\\r.0.0.mca"), "rw"), 0, 0);
            BlockState stone = new BlockState("minecraft:stone");

            ChunkColumn chunk = region.getOrCreateChunk(0, 0);

            sender.sendMessage(String.valueOf(chunk.getBlockState(7, -61, 12)));
            sender.sendMessage(String.valueOf(region.getBlockState(1, 1, 1)));

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = -64; y < 256; y++) {
                        chunk.setBlockState(x, y, z, new BlockState("minecraft:stone"));
                    }
                }
            }

            region.writeColumn(chunk, SupportedVersion.MC_1_18_PRE_4);

            sender.sendMessage(String.valueOf(region.getBlockState(1, 1, 1)));

            // save chunks that are in memory (automatically generated via setBlockState) to disk
            // without this line, your chunks will NOT be saved to disk
            //region.flushCachedChunks();

            sender.sendMessage(String.valueOf(region.getBlockState(1, 1, 1)));

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        region.flushCachedChunks();
                        sender.sendMessage(String.valueOf(region.getBlockState(1, 1, 1)));
                    } catch (AnvilException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.runTaskLater(plugin, 50);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Test method for writing directly to a world file.
     *
     * @return whether method succeeded
     */
    public boolean fillArea(String world, BlockState blockType, int x1, int y1, int z1, int x2, int y2, int z2){

        // Ensure smaller variables are first
        if(x1 > x2) {int temp = x1; x1 = x2; x2 = temp;}
        if(y1 > y2) {int temp = y1; y1 = y2; y2 = temp;}
        if(z1 > z2) {int temp = z1; z1 = z2; z2 = temp;}

        int regionX1 = x1 >> 9;
        int regionZ1 = z1 >> 9;
        int regionX2 = x2 >> 9;
        int regionZ2 = z2 >> 9;

        int chunkX1 = x1 >> 4;
        int chunkZ1 = z1 >> 4;
        int chunkX2 = x2 >> 4;
        int chunkZ2 = z2 >> 4;

        RegionFile[][] regionFiles = new RegionFile[regionX2 - regionX1 + 1][regionZ2 - regionZ1 + 1];
        ChunkColumn[][] chunks = new ChunkColumn[chunkX2 - chunkX1 + 1][chunkZ2 - chunkZ1 + 1];

        try {

            // Open files
            for (int x = 0; x < regionFiles.length; x++) {
                for (int z = 0; z < regionFiles[x].length; z++) {

                    File file = new File(world + "\\region\\r." + (regionX1 + x) + "." + (regionZ1 + z) + ".mca");
                    if (!file.exists())
                        throw new RuntimeException("Region file " + file.getPath() + " does not exist!");

                    regionFiles[x][z] = new RegionFile(new RandomAccessFile(file, "rw"), regionX1 + x, regionZ1 + z);
                }
            }

            // Grab chunks
            for (int x = chunkX1; x <= chunkX2; x++) {
                for (int z = chunkZ1; z <= chunkZ2; z++) {
                    ChunkColumn chunk = regionFiles[x >> 5][z >> 5].getChunk(x, z);
                    if (chunk == null)
                        throw new RuntimeException("Chunk " + x + "," + z + " does not exist!");

                    chunks[x - chunkX1][z - chunkZ1] = chunk;
                }
            }

            // Write blocks to files
            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    for (int y = y1; y <= y2; y++) {
                        chunks[x >> 5][z >> 5].setBlockState(x % 16, y, z % 16, blockType);
                    }
                }
            }

            // Save modified chunks to files
            for (int x = chunkX1; x <= chunkX2; x++) {
                for (int z = chunkZ1; z <= chunkZ2; z++) {
                    regionFiles[x >> 5][z >> 5].writeColumn(chunks[x - chunkX1][z - chunkZ1], SupportedVersion.MC_1_18_PRE_4);
                }
            }

            return true;

        } catch (AnvilException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
