package dev.zoid.mctiers.commands;

import dev.zoid.mctiers.McTiers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    private final McTiers plugin;

    public ReloadCommand(McTiers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mctiers.reload")) {
            sender.sendMessage("<red>You do not have permission to use this command.");
            return true;
        }
        plugin.reloadConfig();
        sender.sendMessage("<gray>McTiers configuration reloaded.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? Collections.singletonList("reload") : Collections.emptyList();
    }
}