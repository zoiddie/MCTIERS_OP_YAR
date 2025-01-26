package dev.zoid.mctiers.commands;

import dev.zoid.mctiers.McTiers;
import dev.zoid.mctiers.utils.TierUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

public class TierCommand implements CommandExecutor {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String[] TIER_COLORS = {
            "<#D3D3D3>LT5", "<#808080>HT5", "<#90EE90>LT4", "<#006400>HT4",
            "<#EEE8AA>LT3", "<#DAA520>HT3", "<#FFE4B5>LT2", "<#FFA500>HT2",
            "<#FFB6C1>LT1", "<#FF0000>HT1", "<#D3D3D3>N/A"
    };

    private final McTiers plugin;

    public TierCommand(McTiers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MINI_MESSAGE.deserialize("<red>This command can only be used by players."));
            return true;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("mctiers.tier")) {
            p.sendMessage(MINI_MESSAGE.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            fetchAndSendTier(p.getName(), p);
        } else if (args.length == 1) {
            fetchAndSendTier(args[0], p);
        } else {
            p.sendMessage(MINI_MESSAGE.deserialize("<gray>Usage: /tier [player]"));
        }
        return true;
    }

    private void fetchAndSendTier(String username, Player sender) {
        CompletableFuture.supplyAsync(() -> TierUtils.requestFromAPI(username))
                .thenAccept(f -> f.thenAcceptAsync(r -> {
                    String tierDisplay = TIER_COLORS[r.tier.tierValue == -1 ? 10 : r.tier.tierValue - 1];
                    Component message = MINI_MESSAGE.deserialize(
                            username.equalsIgnoreCase(sender.getName()) ?
                                    "<gray>Your tier is, " + tierDisplay :
                                    "<gray>" + username + "'s tier is, " + tierDisplay
                    );
                    sender.sendMessage(message);
                }));
    }
}