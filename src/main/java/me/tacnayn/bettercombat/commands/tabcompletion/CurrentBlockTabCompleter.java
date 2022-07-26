package me.tacnayn.bettercombat.commands.tabcompletion;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Autocompletes the provided argument with the current block's coordinates
 */
public class CurrentBlockTabCompleter implements TabCompleter {

    private int argNumber;

    /**
     * @param argNumber Which argument this should tab complete
     */
    public CurrentBlockTabCompleter(int argNumber) {
        this.argNumber = argNumber;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player p){
            Block block = p.getTargetBlockExact(4);

            if(args.length == argNumber + 1){
                return List.of(String.valueOf(block.getLocation().getBlockX()));
            }

            if(args.length == argNumber + 2){
                return List.of(String.valueOf(block.getLocation().getBlockY()));
            }

            if(args.length == argNumber + 3){
                return List.of(String.valueOf(block.getLocation().getBlockZ()));
            }
        }

        return null;
    }
}
