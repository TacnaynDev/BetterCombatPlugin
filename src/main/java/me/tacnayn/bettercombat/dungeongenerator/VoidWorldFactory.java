package me.tacnayn.bettercombat.dungeongenerator;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class VoidWorldFactory
{
    // Singleton
    private static final VoidWorldFactory instance = new VoidWorldFactory();
    private VoidWorldFactory() {}
    public static VoidWorldFactory getInstance(){
        return instance;
    }

    public World createWorld(String name){
        WorldCreator creator = new WorldCreator(name)
                .generator(new EmptyChunkGenerator());

        World world = Bukkit.createWorld(creator);
        prepareWorld(world);
        return world;
    }

    private void prepareWorld(World target){
        target.setAutoSave(false);
        target.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        target.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        target.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        target.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        target.setGameRule(GameRule.MOB_GRIEFING, false);
        target.setGameRule(GameRule.SPAWN_RADIUS, 0);
        target.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
    }
}
