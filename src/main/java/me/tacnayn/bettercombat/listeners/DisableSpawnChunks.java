package me.tacnayn.bettercombat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class DisableSpawnChunks implements Listener {

    @EventHandler(priority= EventPriority.HIGHEST)
    public void worldInit(WorldInitEvent e)
    {
        e.getWorld().setKeepSpawnInMemory(false);
    }
}
