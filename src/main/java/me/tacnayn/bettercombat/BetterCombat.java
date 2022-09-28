package me.tacnayn.bettercombat;

import me.tacnayn.bettercombat.commands.*;
import me.tacnayn.bettercombat.commands.tabcompletion.CurrentBlockTabCompleter;
import me.tacnayn.bettercombat.commands.tabcompletion.WorldsTabCompleter;
import me.tacnayn.bettercombat.dungeongenerator.DungeonRoomFileManagerImpl;
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
        getCommand("pasteschematic").setExecutor(new PasteSchematicCommand(this));
        getCommand("cleardungeon").setExecutor(new ClearDungeonCommand(this));

        getCommand("switchworlds").setTabCompleter(new WorldsTabCompleter());
        getCommand("unloadworld").setTabCompleter(new WorldsTabCompleter());
        getCommand("generatedungeon").setTabCompleter(new WorldsTabCompleter());
        getCommand("cleardungeon").setTabCompleter(new WorldsTabCompleter());
        getCommand("pasteschematic").setTabCompleter(new CurrentBlockTabCompleter(0));

        DungeonRoomFileManagerImpl.getInstance().updateRoomCache(new File("plugins/BetterCombat/dungeon_rooms"));
    }
}
