package me.tacnayn.bettercombat.Listeners;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.CustomGearSystem.CustomItem;
import me.tacnayn.bettercombat.CustomGearSystem.StatType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class OnSwingListener implements Listener {

    private BetterCombat plugin;

    private final HashMap<UUID, Boolean> ignoreCurrentSwing; // True if player swing animation was caused by something that shouldn't cause them to attack, false otherwise

    public OnSwingListener(BetterCombat plugin) {
        this.plugin = plugin;
        this.ignoreCurrentSwing = new HashMap<>();
    }

    // Invalid swing checking
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e){

        // If player placed a block, ignore swing
        ignoreCurrentSwing.put(e.getPlayer().getUniqueId(), true);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e){

        // If player broke a block, ignore swing
        ignoreCurrentSwing.put(e.getPlayer().getUniqueId(), true);
    }

    @EventHandler
    public void onDropItemEvent(PlayerDropItemEvent e){

        // If player dropped an item, ignore swing
        ignoreCurrentSwing.put(e.getPlayer().getUniqueId(), true);
    }

    // Run swing
    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent e){

        Player p = e.getPlayer();
        ItemStack handItem = p.getInventory().getItemInMainHand();

        // Check if the player has swung before
        if(!ignoreCurrentSwing.containsKey(p.getUniqueId())){
            ignoreCurrentSwing.put(p.getUniqueId(), false);
        }

        // If the swing was invalid, don't attack anything
        if(e.getAnimationType() != PlayerAnimationType.ARM_SWING || p.hasCooldown(handItem.getType()) ||
        ignoreCurrentSwing.get(p.getUniqueId()) || handItem.getType() == Material.AIR){
            ignoreCurrentSwing.put(e.getPlayer().getUniqueId(), false);
            return;
        }

        // Parse item stats
        CustomItem item = new CustomItem(plugin, handItem);
        ItemMeta itemMeta = handItem.getItemMeta();

        Integer damage = (Integer) item.getStat(StatType.DAMAGE);
        Double attackSpeed = (Double) item.getStat(StatType.ATTACK_SPEED);
        Double range = (Double) item.getStat(StatType.RANGE);
        Double sweepWidth = (Double) item.getStat(StatType.SWEEP_WIDTH);

        // Cancel attack if a required stat is missing
        if(damage == null || attackSpeed == null || range == null){
            return;
        }

        // Otherwise, do an attack
        item.performAttack(p);
    }
}
