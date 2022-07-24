package me.tacnayn.bettercombat.Commands.TabCompletors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Autocompletes the provided argument with the current block's coordinates
 */
public class CurrentBlockTab implements TabCompleter {

    private int argNumber;

    /**
     * @param argNumber Which argument this should tab complete
     */
    public CurrentBlockTab(int argNumber) {
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
