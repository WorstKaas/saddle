/*
 * Copyright (c) ${year} Strelitzia. All rights reserved.
 *
 */

package worstkaas.saddle.command.rules.config;

import com.crimsonwarpedcraft.cwcommons.config.bukkit.BukkitConfigManagerBuilder;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static org.bukkit.Bukkit.getServer;

/**
 * Provider for the rules configuration.
 */
@UtilityClass
public class RulesConfigProvider {

    private static final Object RELOAD_LOCK = new Object();

    @Setter(AccessLevel.PRIVATE)
    private static @Nullable JavaPlugin plugin;
    private volatile @Nullable RulesConfig config;

    /**
     * Returns a copy of the currently loaded configuration.
     *
     * @throws IllegalStateException if the configuration has not been loaded
     */
    public static RulesConfig getConfig() {
        RulesConfig current = config;

        if (current == null) {
            throw new IllegalStateException("Config has not been initialized");
        }

        return current.copy();
    }

    /**
     * Initializes the provider and loads its configuration.
     */
    public static void init(JavaPlugin plugin) {
        setPlugin(plugin);
        @Nullable String error = reload();
        if (error != null) {
            plugin.getLogger().severe("Failed to load config: " + error);
            getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Reloads the configuration.
     *
     * @return null if successful, otherwise an error message
     */
    public static @Nullable String reload() {
        synchronized (RELOAD_LOCK) {
            JavaPlugin currentPlugin = plugin;

            if (currentPlugin == null) {
                return "Config provider has not been initialized";
            }

            try {
                var file = new File(currentPlugin.getDataFolder(), "config.yml");
                config = new BukkitConfigManagerBuilder().build().load(file, RulesConfig.class);
                return null;
            } catch (IOException | IllegalStateException e) {
                return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            }
        }
    }
}