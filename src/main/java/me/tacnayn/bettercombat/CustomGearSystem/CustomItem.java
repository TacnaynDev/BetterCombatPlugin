package me.tacnayn.bettercombat.CustomGearSystem;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.util.EntityFinders;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class CustomItem extends ItemStack {

    private final ItemStack handle;
    private final BetterCombat plugin;

    private final NamespacedKey damageKey;
    private final NamespacedKey attackSpeedKey;
    private final NamespacedKey rangeKey;
    private final NamespacedKey sweepWidthKey;

    // TacNayn Start
    public CustomItem(BetterCombat plugin, ItemStack item) {
        this.plugin = plugin;
        this.handle = item;

        damageKey = new NamespacedKey(plugin, "Damage");
        attackSpeedKey = new NamespacedKey(plugin, "AttackSpeed");
        rangeKey = new NamespacedKey(plugin, "Range");
        sweepWidthKey = new NamespacedKey(plugin, "SweepWidth");

        // Set flags
        ItemMeta itemMeta = getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        setItemMeta(itemMeta);
    }

    /**
     * Performs a custom attack using this item's stats.<br>
     * <br>
     * Range is how far enemies will be included (checks if enemy center is within sphere)<br>
     * Sweep width extends enemy hitboxes (IS EFFECTIVELY ONLY HORIZONTAL)
     *
     * @throws NullArgumentException
     * if range stat is null
     */
    public void performAttack(Player attacker){

        // Get player's crosshair vector
        Vec3 lookVector = ((CraftPlayer) attacker).getHandle().getLookAngle();

        // Get stats
        Integer damage;
        Double attackSpeed;
        Double range;
        Double sweepWidth;

        if((damage = (Integer) getStat(StatType.DAMAGE)) == null)
            throw new NullArgumentException("Undefined damage stat");
        if((attackSpeed = (Double) getStat(StatType.ATTACK_SPEED)) == null)
            throw new NullArgumentException("Undefined attack speed stat");
        if((range = (Double) getStat(StatType.RANGE)) == null)
            throw new NullArgumentException("Undefined range stat");
        if((sweepWidth = (Double) getStat(StatType.SWEEP_WIDTH)) == null)
            sweepWidth = 0.0; // Use default value for sweep width if none is present

        lookVector = lookVector.multiply(range, range, range);

        // Set weapon cooldown
        attacker.setCooldown(this.getType(), (int) ((1 / attackSpeed) * 20));

        // Capture variables
        Location vectorStart = attacker.getEyeLocation();
        Location vectorEnd = vectorStart.clone().add(lookVector.x, lookVector.y, lookVector.z);

        double finalSweepWidth = sweepWidth;
        EntityFinders.entitiesInSphere(attacker, range + 4)
                .filter(entity -> isCrosshairOnEntity(entity, finalSweepWidth, vectorStart, vectorEnd))
                .forEach(entity -> {
                    entity.damage(damage, attacker);
                    entity.setNoDamageTicks(0);
                });
    }

    /**
     * Performs an Axis-Aligned Bounding-Box Vector collision test on the given entity
     */
    private boolean isCrosshairOnEntity(LivingEntity entity, double hitboxExpandAmount, Location vectorStart, Location vectorEnd){
        {
            BoundingBox hitbox = entity.getBoundingBox().clone().expand(hitboxExpandAmount);

            // --- Test X axis ---
            Pair<Double, Double> percentXInBounds = new Pair<>(
                    (hitbox.getMinX() - vectorStart.getX()) / (vectorEnd.getX() - vectorStart.getX()),
                    (hitbox.getMaxX() - vectorStart.getX()) / (vectorEnd.getX() - vectorStart.getX()));

            // Swap pairs if backwards
            if(percentXInBounds.getA() > percentXInBounds.getB()){
                percentXInBounds = new Pair<>(percentXInBounds.getB(), percentXInBounds.getA());
            }

            if(percentXInBounds.getA() > 1) return false; // Entire vector doesn't reach box, return early to save resources
            if(percentXInBounds.getB() < 0) return false;

            // --- Test Y axis ---
            Pair<Double, Double> percentYInBounds = new Pair<>(
                    (hitbox.getMinY() - vectorStart.getY()) / (vectorEnd.getY() - vectorStart.getY()),
                    (hitbox.getMaxY() - vectorStart.getY()) / (vectorEnd.getY() - vectorStart.getY()));

            // Swap pairs if backwards
            if(percentYInBounds.getA() > percentYInBounds.getB()){
                percentYInBounds = new Pair<>(percentYInBounds.getB(), percentYInBounds.getA());
            }

            if(percentYInBounds.getA() > 1) return false;
            if(percentYInBounds.getB() < 0) return false;

            // --- Test Z axis ---
            Pair<Double, Double> percentZInBounds = new Pair<>(
                    (hitbox.getMinZ() - vectorStart.getZ()) / (vectorEnd.getZ() - vectorStart.getZ()),
                    (hitbox.getMaxZ() - vectorStart.getZ()) / (vectorEnd.getZ() - vectorStart.getZ()));

            // Swap pairs if backwards
            if(percentZInBounds.getA() > percentZInBounds.getB()){
                percentZInBounds = new Pair<>(percentZInBounds.getB(), percentZInBounds.getA());
            }

            if(percentZInBounds.getA() > 1) return false;
            if(percentZInBounds.getB() < 0) return false;

            Pair<Double, Double> percentEntireLineInBounds = new Pair<>(
                    Math.max(Math.max(percentXInBounds.getA(), percentYInBounds.getA()), percentZInBounds.getA()), // Highest lower bound
                    Math.min(Math.min(percentXInBounds.getB(), percentYInBounds.getB()), percentZInBounds.getB())); // Lowest upper bound

            // Return whether the vector is within the box
            return percentEntireLineInBounds.getB() >= percentEntireLineInBounds.getA();
        }
    }

    /**
     * Retrieves the requested stat from the item if it exists
     *
     * @return the requested stat; null if it does not exist
     */
    @Nullable
    public Object getStat(StatType statType) {
        PersistentDataContainer dataContainer = getItemMeta().getPersistentDataContainer();
        return dataContainer.has(getNamespacedKey(statType), statType.getDataType()) ? dataContainer.get(getNamespacedKey(statType), statType.getDataType()) : null;
    }

    public void setStat(StatType statType, Object value) {
        ItemMeta newItemMeta = getItemMeta();
        newItemMeta.getPersistentDataContainer().set(getNamespacedKey(statType), statType.getDataType(), value);
        setItemMeta(newItemMeta);

        updateLore();
    }

    public void updateLore(){

        ItemMeta itemMeta = getItemMeta();

        ArrayList<String> lore = new ArrayList<>();

        // Add stats to lore
        Arrays.stream(StatType.values())
                .filter(Objects::nonNull) // Only add stats that the item has
                .forEachOrdered(stat -> lore.add(ChatColor.GRAY + stat.getDisplayName() + ": " + ChatColor.RED + getStat(stat)));

        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Custom Weapon");

        itemMeta.setLore(lore);
        setItemMeta(itemMeta);
    }

    /**
     * Gets the corresponding namespaced key for a custom stat type
     *
     * @return the namespaced key for getting the stat
     */
    private NamespacedKey getNamespacedKey(StatType type){
        return switch (type) {
            case DAMAGE -> damageKey;
            case ATTACK_SPEED -> attackSpeedKey;
            case RANGE -> rangeKey;
            case SWEEP_WIDTH -> sweepWidthKey;
        };
    }
    // TacNayn end

    /**
     * Gets the type of this item
     *
     * @return Type of the items in this stack
     */
    public Material getType() {
        return handle.getType();
    }

    /**
     * Sets the type of this item
     * <p>
     * Note that in doing so you will reset the MaterialData for this stack.
     * <p>
     * <b>IMPORTANT: An <i>Item</i>Stack is only designed to contain
     * <i>items</i>. Do not use this class to encapsulate Materials for which
     * {@link Material#isItem()} returns false.</b>
     *
     * @param type New type to set the items in this stack to
     */
    public void setType(Material type) {
        handle.setType(type);
    }

    /**
     * Gets the amount of items in this stack
     *
     * @return Amount of items in this stack
     */
    public int getAmount() {
        return handle.getAmount();
    }

    /**
     * Sets the amount of items in this stack
     *
     * @param amount New amount of items in this stack
     */
    public void setAmount(int amount) {
        handle.setAmount(amount);
    }

    /**
     * Gets the MaterialData for this stack of items
     *
     * @return MaterialData for this item
     */
    public MaterialData getData() {
        return handle.getData();
    }

    /**
     * Sets the MaterialData for this stack of items
     *
     * @param data New MaterialData for this item
     */
    public void setData(MaterialData data) {
        handle.setData(data);
    }

    /**
     * Sets the durability of this item
     *
     * @param durability Durability of this item
     * @deprecated durability is now part of ItemMeta. To avoid confusion and
     * misuse, {@link #getItemMeta()}, {@link #setItemMeta(ItemMeta)} and
     * {@link Damageable#setDamage(int)} should be used instead. This is because
     * any call to this method will be overwritten by subsequent setting of
     * ItemMeta which was created before this call.
     */
    @Deprecated
    public void setDurability(short durability) {
        handle.setDurability(durability);
    }

    /**
     * Gets the durability of this item
     *
     * @return Durability of this item
     * @deprecated see {@link #setDurability(short)}
     */
    @Deprecated
    public short getDurability() {
        return handle.getDurability();
    }

    /**
     * Get the maximum stacksize for the material hold in this ItemStack.
     * (Returns -1 if it has no idea)
     *
     * @return The maximum you can stack this material to.
     */
    public int getMaxStackSize() {
        return handle.getMaxStackSize();
    }

    @Override
    public boolean equals(Object obj) {
        return handle.equals(obj);
    }

    /**
     * This method is the same as equals, but does not consider stack size
     * (amount).
     *
     * @param stack the item stack to compare to
     * @return true if the two stacks are equal, ignoring the amount
     */
    public boolean isSimilar(ItemStack stack) {
        return handle.isSimilar(stack);
    }

    /**
     * Checks if this ItemStack contains the given {@link Enchantment}
     *
     * @param ench Enchantment to test
     * @return True if this has the given enchantment
     */
    public boolean containsEnchantment(Enchantment ench) {
        return handle.containsEnchantment(ench);
    }

    /**
     * Gets the level of the specified enchantment on this item stack
     *
     * @param ench Enchantment to check
     * @return Level of the enchantment, or 0
     */
    public int getEnchantmentLevel(Enchantment ench) {
        return handle.getEnchantmentLevel(ench);
    }

    /**
     * Gets a map containing all enchantments and their levels on this item.
     *
     * @return Map of enchantments.
     */
    public Map<Enchantment, Integer> getEnchantments() {
        return handle.getEnchantments();
    }

    /**
     * Adds the specified enchantments to this item stack.
     * <p>
     * This method is the same as calling {@link
     * #addEnchantment(Enchantment, int)} for each
     * element of the map.
     *
     * @param enchantments Enchantments to add
     * @throws IllegalArgumentException if the specified enchantments is null
     * @throws IllegalArgumentException if any specific enchantment or level
     *                                  is null. <b>Warning</b>: Some enchantments may be added before this
     *                                  exception is thrown.
     */
    public void addEnchantments(Map<Enchantment, Integer> enchantments) {
        handle.addEnchantments(enchantments);
    }

    /**
     * Adds the specified {@link Enchantment} to this item stack.
     * <p>
     * If this item stack already contained the given enchantment (at any
     * level), it will be replaced.
     *
     * @param ench  Enchantment to add
     * @param level Level of the enchantment
     * @throws IllegalArgumentException if enchantment null, or enchantment is
     *                                  not applicable
     */
    public void addEnchantment(Enchantment ench, int level) {
        handle.addEnchantment(ench, level);
    }

    /**
     * Adds the specified enchantments to this item stack in an unsafe manner.
     * <p>
     * This method is the same as calling {@link
     * #addUnsafeEnchantment(Enchantment, int)} for
     * each element of the map.
     *
     * @param enchantments Enchantments to add
     */
    public void addUnsafeEnchantments(Map<Enchantment, Integer> enchantments) {
        handle.addUnsafeEnchantments(enchantments);
    }

    /**
     * Adds the specified {@link Enchantment} to this item stack.
     * <p>
     * If this item stack already contained the given enchantment (at any
     * level), it will be replaced.
     * <p>
     * This method is unsafe and will ignore level restrictions or item type.
     * Use at your own discretion.
     *
     * @param ench  Enchantment to add
     * @param level Level of the enchantment
     */
    public void addUnsafeEnchantment(Enchantment ench, int level) {
        handle.addUnsafeEnchantment(ench, level);
    }

    /**
     * Removes the specified {@link Enchantment} if it exists on this
     * ItemStack
     *
     * @param ench Enchantment to remove
     * @return Previous level, or 0
     */
    public int removeEnchantment(Enchantment ench) {
        return handle.removeEnchantment(ench);
    }

    public Map<String, Object> serialize() {
        return handle.serialize();
    }

    /**
     * Get a copy of this ItemStack's {@link ItemMeta}.
     *
     * @return a copy of the current ItemStack's ItemData
     */
    public ItemMeta getItemMeta() {
        return handle.getItemMeta();
    }

    /**
     * Checks to see if any meta data has been defined.
     *
     * @return Returns true if some meta data has been set for this item
     */
    public boolean hasItemMeta() {
        return handle.hasItemMeta();
    }

    /**
     * Set the ItemMeta of this ItemStack.
     *
     * @param itemMeta new ItemMeta, or null to indicate meta data be cleared.
     * @return True if successfully applied ItemMeta, see {@link
     * ItemFactory#isApplicable(ItemMeta, ItemStack)}
     * @throws IllegalArgumentException if the item meta was not created by
     *                                  the {@link ItemFactory}
     */
    public boolean setItemMeta(ItemMeta itemMeta) {
        return handle.setItemMeta(itemMeta);
    }
}
