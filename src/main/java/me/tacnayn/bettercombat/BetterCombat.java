package me.tacnayn.bettercombat;

import me.tacnayn.bettercombat.Commands.*;
import me.tacnayn.bettercombat.Listeners.OnPlayerAttackListener;
import me.tacnayn.bettercombat.Listeners.OnSwingListener;

import org.bukkit.plugin.java.JavaPlugin;

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
        getCommand("swapchunks").setExecutor(new SwapChunksCommand());
        getCommand("testdungeongen").setExecutor(new TestDungeonGenCommand(this));
        getCommand("loadworld").setExecutor(new LoadWorldCommand());
        getCommand("unloadworld").setExecutor(new UnloadWorldCommand());
        getCommand("testpaste").setExecutor(new TestPasteCommand(this));
        getCommand("fillblocksthroughfile").setExecutor(new FillBlocksThroughFileCommand(this));

    }
}
