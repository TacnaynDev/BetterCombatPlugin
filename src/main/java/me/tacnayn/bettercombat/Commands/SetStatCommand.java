package me.tacnayn.bettercombat.commands;

import me.tacnayn.bettercombat.BetterCombat;
import me.tacnayn.bettercombat.customitems.CustomItem;
import me.tacnayn.bettercombat.customitems.StatType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SetStatCommand implements TabExecutor {

    private BetterCombat plugin;
    private final String usage = ChatColor.RED + "/setcustomstat <stat> <value>";

    public SetStatCommand(BetterCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player p){

            CustomItem item = new CustomItem(plugin, p.getInventory().getItemInMainHand());
            ItemMeta itemMeta = item.getItemMeta();

            // Parse arguments
            StatType statType;
            Object statValue;

            try {
                statType = StatType.valueOf(args[0].toUpperCase());
                statValue = convert(statType.getDataType(), args[1]);
            } catch(Exception e) {

                // Invalid argument
                p.sendMessage(usage);
                return true;
            }

            // Additional error management
            if(item.getType().isAir()){

                // Empty hand
                p.sendMessage(ChatColor.RED + "No item held!");
                return true;
            }

            // Execute command
            item.setStat(statType, statValue);
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0){
            return Stream.of(StatType.values()).map(StatType::name).toList();
        }

        return null;
    }

    // Converts a string to any primitive type
    private Object convert(PersistentDataType type, String value){
        if( PersistentDataType.BYTE == type ) return Byte.parseByte( value );
        if( PersistentDataType.SHORT == type ) return Short.parseShort( value );
        if( PersistentDataType.INTEGER == type ) return Integer.parseInt( value );
        if( PersistentDataType.LONG == type ) return Long.parseLong( value );
        if( PersistentDataType.FLOAT == type ) return Float.parseFloat( value );
        if( PersistentDataType.DOUBLE == type ) return Double.parseDouble( value );
        return value;
    }
}
