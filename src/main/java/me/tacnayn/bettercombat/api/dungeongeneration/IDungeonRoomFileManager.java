package me.tacnayn.bettercombat.api.dungeongeneration;

import me.tacnayn.bettercombat.dungeongeneration.DungeonFileType;

import java.io.File;
import java.util.Collection;
public interface IDungeonRoomFileManager {
    Collection<File> getRoomFiles(DungeonFileType type);
}
