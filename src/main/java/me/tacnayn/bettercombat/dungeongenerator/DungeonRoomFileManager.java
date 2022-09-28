package me.tacnayn.bettercombat.dungeongenerator;

import me.tacnayn.bettercombat.dungeongenerator.DungeonFileType;

import java.io.File;
import java.util.Collection;
public interface DungeonRoomFileManager {
    Collection<File> getRoomFiles(DungeonFileType type);
    File blankRoomFile(int height);
}
