package me.tacnayn.bettercombat.dungeongenerator;

import java.io.File;
import java.util.*;

public class DungeonRoomFileManagerImpl implements DungeonRoomFileManager {

    // Singleton
    private final static DungeonRoomFileManagerImpl instance = new DungeonRoomFileManagerImpl();
    private DungeonRoomFileManagerImpl(){};
    public static DungeonRoomFileManagerImpl getInstance() { return instance; }

    List<File> emptyRooms;
    HashMap<DungeonFileType, List<File>> dungeonRoomCache; // Each index is a folder full of files

    @Override
    public Collection<File> getRoomFiles(DungeonFileType type) {
        return dungeonRoomCache.get(type);
    }

    @Override
    public File blankRoomFile(int height) {
        int index = height / 24;
        if(index >= emptyRooms.size()) return null;
        return emptyRooms.get(index);
    }

    /**
     * Grabs an updated copy of all the dungeon room schematics
     *
     * @param dungeonRoomsFolder The folder that contains all the dungeon rooms. Must have proper internal folder structure.
     */
    public void updateRoomCache(File dungeonRoomsFolder){

        DungeonFileType[] roomTypes = DungeonFileType.values();
        dungeonRoomCache = new HashMap<>(roomTypes.length);
        emptyRooms = new ArrayList<>();

        // Grab the direct path to all the files in the dungeon rooms folder
        for (DungeonFileType roomType : DungeonFileType.values()) {
            File subFolder = new File(dungeonRoomsFolder, roomType.toString()); // Grab subfolder

            // Add all the files to the cache
            List<File> roomFiles = List.of(Objects.requireNonNull(subFolder.listFiles()));
            dungeonRoomCache.put(roomType, roomFiles);
        }

        // Grab all the empty rooms
        File emptyRoomsFolder = new File(dungeonRoomsFolder, "empty_rooms");

        for(int i = 24; i < 192; i += 24){
            File emptyRoom = new File(emptyRoomsFolder, "empty_tile_" + i + ".nbt");
            emptyRooms.add(emptyRoom);
        }
    }
}
