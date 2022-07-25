package me.tacnayn.bettercombat;

import me.tacnayn.bettercombat.commands.*;
import me.tacnayn.bettercombat.commands.tabcompletion.CurrentBlockTab;
import me.tacnayn.bettercombat.dungeongeneration.DungeonRoomFileManager;
import me.tacnayn.bettercombat.listeners.OnPlayerAttackListener;
import me.tacnayn.bettercombat.listeners.OnSwingListener;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class BetterCombat extends JavaPlugin {

    // TODO: Add particle effects to the attack

    @Override
    public void onEnable() {

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new OnSwingListener(this), this);

        new OnPlayerAttackListener(this).register();

        getCommand("setstat").setExecutor(new SetStatCommand(this));
        getCommand("copyworldfile").setExecutor(new CopyWorldFileCommand());
        getCommand("switchworlds").setExecutor(new SwitchWorldsCommand());
        getCommand("testdungeongen").setExecutor(new TestDungeonGenCommand(this));
        getCommand("loadworld").setExecutor(new LoadWorldCommand());
        getCommand("unloadworld").setExecutor(new UnloadWorldCommand());
        getCommand("generatedungeon").setExecutor(new GenerateDungeonCommand(this));

        getCommand("testpaste").setExecutor(new TestPasteCommand(this));
        getCommand("testpaste").setTabCompleter(new CurrentBlockTab(0));

        DungeonRoomFileManager.getInstance().updateRoomCache(new File("plugins/BetterCombat/dungeon_rooms"));
    }
}
