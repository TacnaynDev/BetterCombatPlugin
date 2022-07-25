package me.tacnayn.bettercombat.dungeongeneration;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.api.dungeongeneration.IDungeonRoomFileManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;

import java.io.File;

public class DungeonPaster {

    BetterCombat plugin;
    IDungeonRoomFileManager fileManager;
    DungeonGenerator generator;

    public final static int SMALL_ROOM_SIZE = 24;
    public DungeonPaster(BetterCombat plugin, IDungeonRoomFileManager fileManager, DungeonGenerator generator) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.generator = generator;
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

    public void pasteDungeon(){

        // Make world
        World world = new WorldCreator("dungeon")
                .generator(new EmptyChunkGenerator())
                .createWorld();

        System.out.println("Done generating world!");

        // Generate floorplan
        DungeonGenerator.DungeonTile[][] dungeon = generator.generateFloorPlan(0.5, 0.12, 13, 24, 5);
        if(dungeon == null) throw new RuntimeException("Dungeon generation failed!");

        // Paste dungeon
        for(int x = 0; x < dungeon.length; x++) {
            for(int z = 0; z < dungeon[x].length; z++) {

                DungeonGenerator.DungeonTile tile = dungeon[x][z];
                DungeonFileType fileType;
                int rotation;

                DungeonGenerator.RoomType roomType = tile.getRoomType();

                // Determine room type
                if(tile.isNotRoom()) continue;
                if(roomType == DungeonGenerator.RoomType.LARGE){
                    continue; // TODO: Implement large room pasting
                }
                if(roomType == DungeonGenerator.RoomType.BOSS || roomType == DungeonGenerator.RoomType.PUZZLE ||
                        roomType == DungeonGenerator.RoomType.SHOP || roomType == DungeonGenerator.RoomType.TREASURE){
                    fileType = DungeonFileType.SMALL_1_DOOR; // TODO: Implement special rooms
                }
                if(roomType == DungeonGenerator.RoomType.REGULAR || roomType == DungeonGenerator.RoomType.START){

                    // Determine number of neighbors
                    switch (tile.realNeighborsCount()) {
                        case 1 -> {
                            fileType = DungeonFileType.SMALL_1_DOOR;



                        }
                        case 2 -> fileType = tile.isCornerOrAcross() ? DungeonFileType.SMALL_2_DOORS_ACROSS : DungeonFileType.SMALL_2_DOORS_CORNER;
                        case 3 -> fileType = DungeonFileType.SMALL_3_DOORS;
                        case 4 -> fileType = DungeonFileType.SMALL_4_DOORS;
                        default -> throw new RuntimeException("Room has invalid number of neighbors!");
                    }
                }


                // Choose room
                File file = fileManager.getRoomFiles();

                Location loc = new Location(world, x * SMALL_ROOM_SIZE, 64, z * SMALL_ROOM_SIZE);
                StructureBlockLibApi.INSTANCE
                    .loadStructure(plugin)
                    .at(loc)
                    .loadFromFile(file)
                    .onException(e -> {
                        e.printStackTrace();
                    })
                    .onResult(result -> {
                        System.out.println("Structure loaded successfully!");
                    });
            }
        }
    }
}
