package dev.zoid.mctiers.features;

import dev.zoid.mctiers.McTiers;
import dev.zoid.mctiers.utils.TierUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import java.util.concurrent.CompletableFuture;

public class AutoWhitelist implements Listener {
    private final McTiers plugin;
    private boolean enabled;
    private int tierCriteria;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public AutoWhitelist(McTiers plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("auto-whitelist.enabled", true);
        tierCriteria = parseTierCriteria(config.getString("auto-whitelist.tier-criteria", ">=LT3"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private int parseTierCriteria(String criteria) {
        if (criteria.startsWith(">=")) {
            String tier = criteria.substring(2);
            return TierUtils.PlayerTier.valueOf(tier).tierValue;
        }
        return -1;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (!enabled) return;
        CompletableFuture.supplyAsync(() -> TierUtils.requestFromAPI(e.getPlayer().getName()))
                .thenAccept(f -> f.thenAcceptAsync(r -> {
                    String tierDisplay = TierPlaceholder.TIER_COLORS[r.tier.tierValue == -1 ? 10 : r.tier.tierValue - 1];
                    boolean isWhitelisted = r.tier.tierValue >= tierCriteria;

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (isWhitelisted) {
                            e.getPlayer().setWhitelisted(true);
                            Bukkit.reloadWhitelist();
                        }
                        e.setResult(isWhitelisted ? PlayerLoginEvent.Result.ALLOWED : PlayerLoginEvent.Result.KICK_WHITELIST);
                    });
                }));
    }
}