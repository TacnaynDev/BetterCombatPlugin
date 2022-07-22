package me.tacnayn.bettercombat.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.stream.Stream;

public class EntityFinders {

    private EntityFinders(){
    }

    /**
     * Takes in an entity and returns a list of all the mobs within a certain spherical radius
     */
    public static Stream<LivingEntity> entitiesInSphere(Entity sourceEntity, double radius){

        // Get enemies in a sphere
        return sourceEntity.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> entity.getBoundingBox().getCenter().distanceSquared(sourceEntity.getLocation().toVector()) < radius * radius);

    }

    /**
     * Takes in a location and returns a list of all the mobs within a certain spherical radius
     */
    public static Stream<LivingEntity> entitiesInSphere(Location location, double radius){

        Entity marker = location.getWorld().spawnEntity(location, EntityType.MARKER);

        // Get enemies in a sphere
        return entitiesInSphere(marker, radius);
    }

    /**
     * Takes in an entity and returns a stream of all the mobs in a cone shape
     */
    public static Stream<LivingEntity> entitiesInCone(LivingEntity sourceEntity, double distance, double width) {

        if(width >= 360 || width <= 0){
            throw new IllegalArgumentException("Cone width must be a value between 0 and 360!");
        }

        return entitiesInSphere(sourceEntity, distance)
                .filter(target -> {
                    Vector vectorBetween = target.getBoundingBox().getCenter().subtract(sourceEntity.getEyeLocation().toVector());
                    double dotProduct = vectorBetween.normalize().dot(sourceEntity.getEyeLocation().getDirection());
                    return dotProduct > (360 - width) * 0.00277777777777777777777777777778; // Convert width from degrees to a scalar from 0 to 1
                });
    }

}
