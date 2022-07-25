package me.tacnayn.bettercombat.dungeongeneration;

import me.tacnayn.bettercombat.api.dungeongeneration.IDungeonRoomFileManager;

import java.io.File;
import java.util.*;

public class DungeonRoomFileManager implements IDungeonRoomFileManager {

    // Singleton
    private final static DungeonRoomFileManager instance = new DungeonRoomFileManager();
    private DungeonRoomFileManager(){};
    public static DungeonRoomFileManager getInstance() { return instance; }

    HashMap<DungeonFileType, List<File>> dungeonRoomCache; // Each index is a folder full of files
    @Override
    public Collection<File> getRoomFiles(DungeonFileType type) {
        return dungeonRoomCache.get(type.ordinal());
    }

    /**
     * Grabs an updated copy of all the dungeon room schematics
     *
     * @param dungeonRoomsFolder The folder that contains all the dungeon rooms. Must have proper internal folder structure.
     */
    public void updateRoomCache(File dungeonRoomsFolder){

        DungeonFileType[] roomTypes = DungeonFileType.values();
        dungeonRoomCache = new HashMap<>(roomTypes.length);

        // Grab the direct path to all the files in the dungeon rooms folder
        for (DungeonFileType roomType : DungeonFileType.values()) {
            File subFolder = new File(dungeonRoomsFolder, roomType.toString()); // Grab subfolder

            // Add all the files to the cache
            List<File> roomFiles = List.of(Objects.requireNonNull(subFolder.listFiles()));
            dungeonRoomCache.put(roomType, roomFiles);
        }
    }
}
