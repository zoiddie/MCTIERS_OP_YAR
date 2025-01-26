package dev.zoid.mctiers.features;

import dev.zoid.mctiers.utils.TierUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TierPlaceholder extends PlaceholderExpansion {
    public static final String[] TIER_COLORS = {
            "<#D3D3D3>LT5", "<#808080>HT5", "<#90EE90>LT4", "<#006400>HT4",
            "<#EEE8AA>LT3", "<#DAA520>HT3", "<#FFE4B5>LT2", "<#FFA500>HT2",
            "<#FFB6C1>LT1", "<#FF0000>HT1", "<#D3D3D3>N/A"
    };

    @Override
    public @NotNull String getIdentifier() {
        return "vanilla";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Zoid";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("tier")) {
            TierUtils.TierlistPlayer tierPlayer = TierUtils.requestFromAPI(player.getName()).join();
            return TIER_COLORS[tierPlayer.tier.tierValue == -1 ? 10 : tierPlayer.tier.tierValue - 1];
        }
        return null;
    }
}