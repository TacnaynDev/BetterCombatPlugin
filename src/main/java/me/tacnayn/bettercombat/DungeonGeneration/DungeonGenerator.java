package me.tacnayn.bettercombat.dungeongeneration;

import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class DungeonGenerator {
    private final Random random;
    private DungeonTile startTile;
    private DungeonTile[][] dungeonGrid;
    private ArrayList<DungeonTile> deadEnds;
    private LinkedList<DungeonTile> tilesToIterate;

    public DungeonGenerator() {
        this.random = new Random();
    }
    public DungeonGenerator(long seed) {
        this.random = new Random(seed);
    }

    // DEBUGGING ONLY
    public void printDungeonPlan(CommandSender sender){
        DungeonTile[][] dungeonGrid = generateFloorPlan(0.5, 0.12, 13, 24, 5);

        StringBuilder message = new StringBuilder(13*13 + 1);
        message.append('\n');
        for (DungeonTile[] row : dungeonGrid) {
            for (DungeonTile dungeonTile : row) {
                char icon = switch (dungeonTile.getRoomType()) {
                    case EMPTY -> ' ';
                    case REGULAR -> '⬛';
                    case START -> 'S';
                    case DEAD_END -> 'X';
                    case LARGE -> 'L';
                    case TREASURE -> 'T';
                    case PUZZLE -> 'P';
                    case BOSS -> 'B';
                    case SHOP -> '$';
                };

                message.append(icon);
            }
            message.append('\n');
        }

        sender.sendMessage(message.toString());
    }

    /**
     * Generates a randomized dungeon floor plan
     *
     * @param roomChance The chance from 0 to 1 that a room will be generated in front of each door
     * @param largeRoomChance The chance from 0 to 1 that a large room will be generated instead of a regular room
     * @param dungeonBorders The maximum X and Y of the dungeon
     * @param targetRoomCount The target number of rooms to generate. Will usually overshoot, but rarely undershoots.
     * @param attempts The maximum number of attempts to generate a dungeon if it fails. If this number is exceeded, the generator will throw an exception
     *
     * @return A dungeon floor plan for use with room placement methods.
     */
    public DungeonTile[][] generateFloorPlan(double roomChance, double largeRoomChance, int dungeonBorders, int targetRoomCount, int attempts){
        dungeonGrid = new DungeonTile[dungeonBorders][dungeonBorders];
        tilesToIterate = new LinkedList<>();
        deadEnds = new ArrayList<>(10);

        // Fill the grid with empty tiles
        for(int x = 0; x < dungeonBorders; x++){
            for(int y = 0; y < dungeonBorders; y++){
                dungeonGrid[x][y] = new DungeonTile(x, y);
            }
        }

        // Set tile neighbors
        for(int x = 0; x < dungeonBorders; x++){
            for(int y = 0; y < dungeonBorders; y++){
                dungeonGrid[x][y].addNeighbors(dungeonGrid);
            }
        }

        // Place a starting tile
        int centerTile = dungeonBorders / 2;;
        startTile = dungeonGrid[centerTile][centerTile];
        startTile.roomType = RoomType.START;
        startTile.distanceToStart = 0;
        tilesToIterate.addLast(startTile);

        // Begin placing rooms
        for(int tilesPlaced = 0; tilesPlaced < targetRoomCount; tilesPlaced++){

            boolean guaranteeRoom = false;

            // Get the next tile
            DungeonTile currentTile = tilesToIterate.removeFirst();

            if(tilesToIterate.isEmpty()){
                // If we haven't finished the dungeon yet, we need to guarantee at least 1 room is placed
                guaranteeRoom = true;
            }

            tilesPlaced += tryToPlaceRoomsOnEmptyNeighbors(guaranteeRoom ? 1.0 : roomChance, largeRoomChance, currentTile);

            // If we failed to place any rooms with a guaranteed chance, we need to re-roll all the other rooms
            if(tilesToIterate.isEmpty()){
                addAllRoomsToIterationList();
                tilesPlaced++; // Increment tilesPlaced to avoid an infinite loop
            }
        }

        // Check if there are enough dead ends
        final int requiredDeadEnds = 4;
        if(deadEnds.size() < requiredDeadEnds){

            // If there are no rooms left to iterate, add them all again
            if(tilesToIterate.isEmpty()) addAllRoomsToIterationList();

            // If not, add more dead ends
            while (deadEnds.size() < requiredDeadEnds && !tilesToIterate.isEmpty()) {
                tryToPlaceRoomsOnEmptyNeighbors(1, 0, tilesToIterate.removeFirst());
            }

            // If still not enough, start over
            if(deadEnds.size() < requiredDeadEnds){

                if(--attempts <= 0)
                    throw new RuntimeException("Failed to generate dungeon.");

                return generateFloorPlan(roomChance, largeRoomChance, dungeonBorders, targetRoomCount, attempts);
            }
        }

        // Mark special rooms
        Collections.shuffle(deadEnds, random);

        DungeonTile bossRoom = findFurthestDeadEnd();
        deadEnds.remove(bossRoom);

        bossRoom.roomType = RoomType.BOSS;
        deadEnds.remove(0).roomType = RoomType.TREASURE;
        deadEnds.remove(0).roomType = RoomType.PUZZLE;
        deadEnds.remove(0).roomType = RoomType.SHOP;

        return dungeonGrid;
    }

    /**
     * Loops through all the neighbors of this room and attempts to place new rooms around it
     *
     * @return The number of tiles this method successfully placed
     */
    private int tryToPlaceRoomsOnEmptyNeighbors(double roomChance, double largeRoomChance, DungeonTile currentTile) {
        int tilesPlaced = 0;

        for (DungeonTile newRoomSpot : currentTile.neighbors) {

            if (!isRoomSpotValid(newRoomSpot)) continue; // Is already room

            if (random.nextDouble(0.0, 1.0) > roomChance) continue; // Room chance failed

            if(currentTile.roomType == RoomType.DEAD_END){
                currentTile.roomType = RoomType.REGULAR;
                deadEnds.remove(currentTile);
            }

            // Set the new room's distance to the start tile
            newRoomSpot.distanceToStart = currentTile.distanceToStart + 1;

            // Roll to place a large room
            if (random.nextDouble(0.0, 1.0) < largeRoomChance) {

                int largeRoomSize = tryPlaceLargeRoom(newRoomSpot);

                if (largeRoomSize != -1) // Large room failed to place
                    tilesPlaced += largeRoomSize - 1;
                else
                    placeSmallRoom(newRoomSpot);

            } else {
                placeSmallRoom(newRoomSpot);
            }
        }

        return tilesPlaced;
    }

    /**
     * Checks that a tile is not already a room, and won't form any loops
     *
     * @return True if a room may be placed there
     */
    private boolean isRoomSpotValid(DungeonTile newRoomSpot) {
        if (newRoomSpot.isRoom()) return false;

        return newRoomSpot.neighbors.stream()
                .filter(DungeonTile::isRoom)
                .count() < 2;
    }

    /**
     * Updates any neighbors of this large room telling them that they are not dead ends anymore
     */
    private void updateLargeRoomNeighbors(DungeonTile newRoomSpot) {
        newRoomSpot.neighbors.forEach(neighbor -> {
            if(neighbor.getRoomType() == RoomType.DEAD_END){
                deadEnds.remove(neighbor);
                neighbor.roomType = RoomType.REGULAR;
            }
        });
    }

    private void placeSmallRoom(DungeonTile newRoomSpot) {
        newRoomSpot.roomType = RoomType.DEAD_END;
        deadEnds.add(newRoomSpot);
        tilesToIterate.addLast(newRoomSpot);
    }

    /**
     * Adds all the rooms in the dungeon to the iteration list in a random order
     */
    private void addAllRoomsToIterationList() {
        ArrayList<DungeonTile> tempList = Arrays.stream(dungeonGrid).flatMap(Arrays::stream)
                .filter(tile -> tile.getRoomType() != RoomType.EMPTY)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(tempList, random);
        tilesToIterate = new LinkedList<>(tempList);
    }

    /**
     * Finds the furthest dead end in the dungeon
     */
    private DungeonTile findFurthestDeadEnd(){
        DungeonTile furthest = deadEnds.get(0);
        for(DungeonTile deadEnd : deadEnds){
            if(deadEnd.distanceToStart > furthest.distanceToStart){
                furthest = deadEnd;
            }
        }
        return furthest;
    }

    /**
     * Places a random large room at the given tile.
     *
     * @return The size of the large room, or -1 if the room couldn't be placed.
     */
    private int tryPlaceLargeRoom(DungeonTile newRoomSpot) {

        // Check if a large room is allowed in the current space
        boolean isEdgeRoom =
            newRoomSpot.x == 0 ||
            newRoomSpot.x == dungeonGrid.length - 1 ||
            newRoomSpot.y == 0 ||
            newRoomSpot.y == dungeonGrid.length - 1;

        if(isEdgeRoom) return -1; // TODO: Allow edge rooms later

        // Choose room size
        int roomSize = random.nextInt(2, 5);

        // Choose directions for the extra tiles
        Direction secondTileDirection = Direction.fromInt(random.nextInt(0, 4));
        Direction thirdTileDirection;
        if (secondTileDirection.getX() == 0) // Third tile must go perpendicular to second tile
            thirdTileDirection = random.nextBoolean() ? Direction.WEST : Direction.EAST;
        else
            thirdTileDirection = random.nextBoolean() ? Direction.NORTH : Direction.SOUTH;

        // Add the extra tiles to the large room
        DungeonTile[] parts = switch (roomSize) {
            case 2 -> new DungeonTile[]{ // 1x2 Room
                    dungeonGrid[newRoomSpot.x][newRoomSpot.y],
                    dungeonGrid[newRoomSpot.x + secondTileDirection.x][newRoomSpot.y + secondTileDirection.y]};
            case 3 -> new DungeonTile[]{ // L Shaped Room
                    dungeonGrid[newRoomSpot.x][newRoomSpot.y],
                    dungeonGrid[newRoomSpot.x + secondTileDirection.x][newRoomSpot.y + secondTileDirection.y],
                    dungeonGrid[newRoomSpot.x + secondTileDirection.x + thirdTileDirection.x][newRoomSpot.y + secondTileDirection.y + thirdTileDirection.y]};
            case 4 -> new DungeonTile[]{ // 2x2 Room
                    dungeonGrid[newRoomSpot.x][newRoomSpot.y],
                    dungeonGrid[newRoomSpot.x + secondTileDirection.x][newRoomSpot.y + secondTileDirection.y],
                    dungeonGrid[newRoomSpot.x + secondTileDirection.x + thirdTileDirection.x][newRoomSpot.y + secondTileDirection.y + thirdTileDirection.y],
                    dungeonGrid[newRoomSpot.x + thirdTileDirection.x][newRoomSpot.y + thirdTileDirection.y]};
            default -> throw new IllegalStateException("Unexpected value: " + roomSize);
        };

        // Make sure there's space for the large room
        if(Arrays.stream(parts).allMatch(tile -> tile.getRoomType() == RoomType.EMPTY)){
            Arrays.stream(parts).forEach(part -> {
                part.turnIntoLargeRoom(parts);
                part.distanceToStart = newRoomSpot.distanceToStart;
            });

            tilesToIterate.addLast(newRoomSpot);
            updateLargeRoomNeighbors(newRoomSpot);

            return roomSize;
        } else {
            return -1;
        }
    }

    /**
     * Locates positions for special rooms
     *
     * @param deadEnds A list of all the dead ends in the dungeon
     */
    private void populateSpecialRooms(ArrayList<DungeonTile> deadEnds){

    }

    // Breadth-first search to find the farthest tile from the start tile
    private DungeonTile locateFarthestRoom(){
        LinkedList<DungeonTile> roomsToCheck = new LinkedList<>();

        // Enqueue the start tile
        roomsToCheck.addLast(startTile);
        startTile.distanceToStart = 0;

        // Iterate through all the tiles
        while(true){
            DungeonTile currentTile = roomsToCheck.removeFirst();

            // Iterate through all the existing rooms in the dungeon
            currentTile.neighbors.stream()
                    .filter(neighbor ->
                            neighbor.getRoomType() != RoomType.EMPTY && // Don't count empty tiles
                            neighbor.distanceToStart == null) // Only rooms that haven't been visited
                    .forEach(neighbor -> {

                        // Add it to the queue
                        neighbor.distanceToStart = currentTile.distanceToStart + 1;
                        roomsToCheck.addLast(neighbor);
                    });

            // If all the rooms are gone, the last room is the farthest room
            if(roomsToCheck.isEmpty()){
                return currentTile;
            }
        }
    }

    /**
     * Tile that represents a spot for a dungeon room.
     * NOT necessarily a room, use isRoom() to check.
     */
    static class DungeonTile{
        private int x;
        private int y;
        private Integer distanceToStart;
        private RoomType roomType;
        private DungeonTile[] largeRoomParts; // Includes this tile in the large room; null if not a large room
        private HashSet<DungeonTile> neighbors; // If this is a large room, includes all the neighbors of the entire large room
        private String layoutFileName;

        public DungeonTile(int x, int y) {
            roomType = RoomType.EMPTY;
            this.x = x;
            this.y = y;
            distanceToStart = null;
            neighbors = new HashSet<>(4);
        }

        /**
         * Converts this tile into a large room
         *
         * @implNote parts must include this tile
         */
        private void turnIntoLargeRoom(DungeonTile[] parts){
            largeRoomParts = parts;
            roomType = RoomType.LARGE;

            // Add all the neighbors of the other parts
            for(DungeonTile part : parts){
                neighbors.addAll(part.neighbors);
            }

            // Remove any neighbors that are part of the large room
            Arrays.asList(parts).forEach(neighbors::remove);
        }
        private void addNeighbors(DungeonTile[][] tileset) {
            if (x > 0) {
                neighbors.add(tileset[x - 1][y]);
            }
            if (x < tileset.length - 1) {
                neighbors.add(tileset[x + 1][y]);
            }
            if (y > 0) {
                neighbors.add(tileset[x][y - 1]);
            }
            if (y < tileset.length - 1) {
                neighbors.add(tileset[x][y + 1]);
            }
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
        public boolean isRoom() {
            return roomType != RoomType.EMPTY;
        }
        public boolean isNotRoom() {
            return roomType == RoomType.EMPTY;
        }
        public RoomType getRoomType() {
            return roomType;
        }
        public int realNeighborsCount() {
            return (int) neighbors.stream()
                    .filter(DungeonTile::isRoom)
                    .count();
        }
        public int directionTo(DungeonTile other) {
            // TODO: Calculate direction to get to other tile
        }
        /**
         * Checks whether this tile's neighbors are across, or diagonal from each other
         *
         * @return true if this tile's neighbors are across, false if this tile's neighbors are diagonal
         * @throws IllegalStateException if this tile has more or less than 2 neighbors.
         */
        public boolean isCornerOrAcross(){

            DungeonTile[] roomNeighbors = this.neighbors.stream()
                    .filter(DungeonTile::isRoom)
                    .toArray(DungeonTile[]::new);

            if(roomNeighbors.length != 2) throw new IllegalStateException("Tile has " + roomNeighbors.length + " neighbors. Expected: 2");

            // Check if the neighbors are across. If they are not, they must be diagonal.
            return roomNeighbors[0].getX() == roomNeighbors[1].getX() || roomNeighbors[0].getY() == roomNeighbors[1].getY();
        }
        public DungeonTile[] getLargeRoomParts() {
            return largeRoomParts;
        }
    }

    enum Direction{
        NORTH(0, -1),
        EAST(1, 0),
        SOUTH(0, 1),
        WEST(-1, 0);

        private final int x;
        private final int y;

        private Direction(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        public static Direction fromInt(int direction){
            return switch (direction) {
                case 0 -> NORTH;
                case 1 -> EAST;
                case 2 -> SOUTH;
                case 3 -> WEST;
                default -> null;
            };
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    enum RoomType{
        EMPTY,
        REGULAR,
        START,
        DEAD_END,
        LARGE,
        TREASURE,
        PUZZLE,
        BOSS,
        SHOP
    }


}
