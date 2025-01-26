package dev.zoid.mctiers;

import dev.zoid.mctiers.commands.ReloadCommand;
import dev.zoid.mctiers.commands.TierCommand;
import dev.zoid.mctiers.features.AutoWhitelist;
import dev.zoid.mctiers.features.TierPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class McTiers extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new AutoWhitelist(this).initialize();
        getCommand("tier").setExecutor(new TierCommand(this));
        getCommand("mctiers").setExecutor(new ReloadCommand(this));
        getCommand("mctiers").setTabCompleter(new ReloadCommand(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TierPlaceholder().register();
        }
    }

    @Override
    public void onDisable() {
    }
}