package me.tacnayn.bettercombat.dungeongeneration;

public enum DungeonFileType {
    LARGE_1X2("large_1x2"),
    LARGE_1X2_HALLWAY("large_1x2_hallway"),
    LARGE_2X2("large_2x2"),
    LARGE_R_SHAPED("large_r_shaped"),
    SMALL_1_DOOR("small_1_door"),
    SMALL_2_DOORS_ACROSS("small_2_doors_across"),
    SMALL_2_DOORS_CORNER("small_2_doors_corner"),
    SMALL_3_DOORS("small_3_doors"),
    SMALL_4_DOORS("small_4_doors");
/*    SPECIAL_BOSS("special_boss"),
    SPECIAL_PUZZLE("special_boss"),
    SPECIAL_SHOP("special_shop"),
    SPECIAL_TREASURE("special_treasure");*/

    final String stringRepresentation;

    DungeonFileType(String stringRepresentation){
        this.stringRepresentation = stringRepresentation;
    }

    public String toString(){
        return stringRepresentation;
    }
}
