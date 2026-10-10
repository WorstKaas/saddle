/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.map;

import com.crimsonwarpedcraft.cwcommons.config.bukkit.BukkitConfigManagerBuilder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Simple plugin that listens to the /map command and
 */
public class Map extends JavaPlugin {

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIPaperConfig(this));
    }

    @Override
    public void onEnable() {
        getLogger().info("Loading plugin...");
        CommandAPI.onEnable();
        suggestPaper();
        saveDefaultConfig();
        MapCommandPluginConfig config;

        // Load the configuration settings
        try {
            config = new BukkitConfigManagerBuilder().build().load(new File(getDataFolder(), "config.yml"), MapCommandPluginConfig.class);
            getLogger().info("Config loaded!");
        } catch (IOException | IllegalStateException e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Set up in-game /example command
        getLogger().info("Registering commands");
        new MapCommand(config).register();
        getLogger().info("Plugin loaded successfully!");
    }

    @Override
    public void onDisable() {
          CommandAPI.onDisable();
    }

    private void suggestPaper() {
        boolean result;
        try {
            Class.forName("io.papermc.paper.ServerBuildInfo");
            result = true;
        } catch (ClassNotFoundException e) {
            result = false;
        }
        if (result) {
            return;
        }

        getLogger().warning(getName() + " recommends using Paper.");
    }

}
