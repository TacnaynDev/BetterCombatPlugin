package me.tacnayn.bettercombat.customgearsystem;

import org.bukkit.persistence.PersistentDataType;

public enum StatType {
    DAMAGE("Damage", PersistentDataType.INTEGER),
    ATTACK_SPEED("Attack Speed", PersistentDataType.DOUBLE),
    RANGE("Range", PersistentDataType.DOUBLE),
    SWEEP_WIDTH("Sweep Width", PersistentDataType.DOUBLE);

    private final String displayName;
    private final PersistentDataType dataType;

    private StatType(final String string, final PersistentDataType type) {
        this.displayName = string;
        this.dataType = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PersistentDataType getDataType() {
        return dataType;
    }
}
