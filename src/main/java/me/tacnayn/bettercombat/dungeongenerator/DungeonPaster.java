package me.tacnayn.bettercombat.dungeongenerator;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.entity.ProgressToken;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import me.tacnayn.bettercombat.BetterCombat;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class DungeonPaster {

    public final static int SMALL_ROOM_SIZE = 24;
    public final static int DUNGEON_Y_LEVEL = 64;

    BetterCombat plugin;
    DungeonRoomFileManager fileManager;
    DungeonGenerator generator;

    public DungeonPaster(BetterCombat plugin, DungeonRoomFileManager fileManager, DungeonGenerator generator) {
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

    public void generateAndPasteDungeon(World world, Random random){

        System.out.println("Done generating world!");

        // Generate floorplan
        DungeonGenerator.DungeonTile[][] dungeon = generator.generateFloorPlan(0.5, 0.12, 13, 24, 5);

        System.out.println("Generating dungeon that should look like this:");
        generator.printDungeonPlan(dungeon); // DEBUG

        if(dungeon == null) throw new RuntimeException("Dungeon generation failed!");

        pasteDungeon(world, random, dungeon);
    }

    private void pasteDungeon(World world, Random random, DungeonGenerator.DungeonTile[][] dungeon) {

        for(int x = 0; x < dungeon.length; x++) {
            for(int z = 0; z < dungeon[x].length; z++) {

                DungeonGenerator.DungeonTile tile = dungeon[x][z];

                DungeonFileType fileType = null;
                int rotation = 0;

                DungeonGenerator.RoomType roomType = tile.getRoomType();

                // Determine room type
                List<DungeonGenerator.Direction> neighbors = tile.neighboringRooms().map(tile::directionTo).toList();

                switch(roomType) {
                    case EMPTY -> {continue;}
                    case REGULAR, DEAD_END, START -> {
                        // Determine room type
                        fileType = switch (neighbors.size()) {
                            case 1 -> DungeonFileType.SMALL_1_DOOR;
                            case 2 -> tile.isCornerOrAcross() ? DungeonFileType.SMALL_2_DOORS_ACROSS : DungeonFileType.SMALL_2_DOORS_CORNER;
                            case 3 -> DungeonFileType.SMALL_3_DOORS;
                            case 4 -> DungeonFileType.SMALL_4_DOORS;
                            default -> throw new RuntimeException("Room has invalid number of neighbors!");
                        };
                    }
                    case LARGE -> {
                        // Determine large room type
                        fileType = switch (tile.largeRoomParts().length) {
                            case 2 -> DungeonFileType.LARGE_1X2;
                            case 3 -> DungeonFileType.LARGE_R_SHAPED;
                            case 4 -> DungeonFileType.LARGE_2X2;
                            default -> throw new RuntimeException("Room at " + tile.getX() + "," + tile.getY() + " is labelled large room but has invalid number of parts!");
                        };
                    }
                    case TREASURE -> { fileType = DungeonFileType.SPECIAL_TREASURE; }
                    case PUZZLE -> { fileType = DungeonFileType.SPECIAL_PUZZLE; }
                    case BOSS -> { fileType = DungeonFileType.SPECIAL_BOSS; }
                    case SHOP -> { fileType = DungeonFileType.SPECIAL_SHOP; }
                }

                // Determine room rotation
                switch (fileType) {
                    case SMALL_1_DOOR, SMALL_2_DOORS_ACROSS, SPECIAL_BOSS, SPECIAL_PUZZLE, SPECIAL_SHOP, SPECIAL_TREASURE -> {
                        // Grab any door and rotate the room that way
                        rotation = neighbors.get(0).toDegrees();
                    }
                    case SMALL_2_DOORS_CORNER -> { // Rotate based on both doors
                        if(neighbors.contains(DungeonGenerator.Direction.SOUTH)){
                            rotation = neighbors.contains(DungeonGenerator.Direction.EAST) ?
                                    270 : // SOUTHEAST
                                    0; // SOUTHWEST
                        } else {
                            rotation = neighbors.contains(DungeonGenerator.Direction.EAST) ?
                                    180: // NORTHEAST
                                    90; // NORTHWEST
                        }
                    }
                    case SMALL_3_DOORS -> { // Grab the empty space and rotate the room that way
                        if(!neighbors.contains(DungeonGenerator.Direction.SOUTH)) rotation = 90;
                        else if(!neighbors.contains(DungeonGenerator.Direction.WEST)) rotation = 180;
                        else if(!neighbors.contains(DungeonGenerator.Direction.NORTH)) rotation = 270;
                    }
                    case SMALL_4_DOORS -> { // Rotate randomly for variation
                        rotation = random.nextInt(0, 4) * 90;
                    }
                    case LARGE_1X2 -> {
                        rotation = tile.largeRoomParts()[1].directionTo(tile.largeRoomParts()[0]).toDegrees();

                        if(isRoomHallway(neighbors, rotation)){
                            fileType = DungeonFileType.LARGE_1X2_HALLWAY;
                        }
                    }
                    case LARGE_R_SHAPED, LARGE_2X2 -> {
                        rotation = tile.largeRoomParts()[1].directionTo(tile.largeRoomParts()[0]).toDegrees();
                    }
                }

                // Choose room
                Collection<File> roomFolder = fileManager.getRoomFiles(fileType);
                List<File> possibleRooms = roomFolder.stream().toList();

                if(possibleRooms.isEmpty()) {
                    System.out.println("No rooms found for type " + fileType.name());
                    continue;
                }

                File roomChoice = possibleRooms.get(random.nextInt(0, possibleRooms.size()));

                StructureRotation structureRot = switch (rotation) {
                    case 0 -> StructureRotation.NONE;
                    case 90 -> StructureRotation.ROTATION_90;
                    case 180 -> StructureRotation.ROTATION_180;
                    case 270 -> StructureRotation.ROTATION_270;
                    default -> throw new RuntimeException("Rotation isn't a valid direction!");
                };
                boolean isLarge = roomType == DungeonGenerator.RoomType.LARGE;

                // Moves 47 blocks for 1x2 rooms in certain situations, 23 for small rooms
                int offset1x2 = fileType == DungeonFileType.LARGE_1X2 ? SMALL_ROOM_SIZE * 2 - 1 : SMALL_ROOM_SIZE - 1;

                Vector offsetVec = switch (structureRot) {
                    case NONE -> new Vector();
                    case ROTATION_90 -> new Vector(SMALL_ROOM_SIZE - 1, 0, 0);
                    case ROTATION_180 -> new Vector(SMALL_ROOM_SIZE - 1, 0, SMALL_ROOM_SIZE - 1);
                    case ROTATION_270 -> new Vector(0, 0, SMALL_ROOM_SIZE - 1);
                };

                int locX = (isLarge ? tile.largeRoomParts()[0].getX() : x) * SMALL_ROOM_SIZE;
                int locZ = (isLarge ? tile.largeRoomParts()[0].getY() : z) * SMALL_ROOM_SIZE;
                Location loc = new Location(world, locX, DUNGEON_Y_LEVEL, locZ).add(offsetVec);

                // Paste room
                StructureBlockLibApi.INSTANCE
                        .loadStructure(plugin)
                        .at(loc)
                        .rotation(structureRot)
                        .onProcessBlock(block -> block.getSourceBlock().getBlockData().getMaterial() != Material.AIR)
                        .loadFromFile(roomChoice)
                        .onException(Throwable::printStackTrace);

                if(roomType == DungeonGenerator.RoomType.START)
                    world.setSpawnLocation(loc.getBlockX() - SMALL_ROOM_SIZE / 2, loc.getBlockY(), loc.getBlockZ() - SMALL_ROOM_SIZE / 2);

                tile.setPasted(true);
                if(roomType == DungeonGenerator.RoomType.LARGE) {
                    for(DungeonGenerator.DungeonTile part : tile.largeRoomParts()) {
                        part.setPasted(true);
                    }
                }
            }
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(ChatColor.GREEN + "Dungeon generated!"));
    }

    private boolean isRoomHallway(List<DungeonGenerator.Direction> neighbors, int rotation) {
        return neighbors.size() <= 2 &&
                neighbors.stream().filter(Objects::nonNull)
                    .allMatch(dir -> dir.toDegrees() == rotation || dir.toDegrees() == (rotation + 180) % 360);
    }
}
