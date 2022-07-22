package me.tacnayn.bettercombat.DungeonGeneration;

import org.jglrxavpok.hephaistos.mca.AnvilException;
import org.jglrxavpok.hephaistos.mca.BlockState;
import org.jglrxavpok.hephaistos.mca.ChunkColumn;
import org.jglrxavpok.hephaistos.mca.RegionFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DungeonPaster {

    /**
     * Test method for writing directly to a world file.
     */
    public void fillArea(String world, BlockState blockType, int x1, int y1, int z1, int x2, int y2, int z2){

        int regionX1 = Math.min(x1, x2) >> 9;
        int regionZ1 = Math.min(z1, z2) >> 9;
        int regionX2 = Math.max(x1, x2) >> 9;
        int regionZ2 = Math.max(z1, z2) >> 9;

        RegionFile[][] regionFiles = new RegionFile[regionX2 - regionX1 + 1][regionZ2 - regionZ1 + 1];

        try {
            for (int x = 0; x < regionFiles.length; x++) {
                for (int z = 0; z < regionFiles[x].length; z++) {

                    File file = new File(world + "\\region\\r." + (regionX1 + x) + "." + (regionZ1 + z) + ".mca");
                    if (!file.exists()) {
                        throw new RuntimeException("Region file " + file.getPath() + " does not exist!");
                    }

                    regionFiles[x][z] = new RegionFile(new RandomAccessFile(file, "rw"), regionX1 + x, regionZ1 + z);
                }
            }
        } catch (AnvilException | IOException e) {
            e.printStackTrace();
            return;
        }

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                for (int y = y1; y <= y2; y++) {

                    RegionFile region = regionFiles[(x >> 9) - regionX1][(z >> 9) - regionZ1];

                    region.

                    region.setBlockState(x, y, z, blockType);
                }
            }
        }

        // save chunks that are in memory (automatically generated via setBlockState) to disk
        // without this line, your chunks will NOT be saved to disk
        region.flushCachedChunks();
    }
}
