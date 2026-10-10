/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.config;

import com.crimsonwarpedcraft.cwcommons.config.bukkit.BukkitConfigManagerBuilder;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static org.bukkit.Bukkit.getServer;

/// Utility class for managing portal configuration loading and access
/// Thread-safe configuration provider with reload support
@UtilityClass
public class PortalConfigProvider {

    /// Lock object for thread-safe config reloading
    private static final Object RELOAD_LOCK = new Object();

    /// The plugin instance (set during initialization)
    @Setter(AccessLevel.PRIVATE)
    private static @Nullable JavaPlugin plugin;
    /// The loaded configuration (volatile for thread visibility)
    private volatile @Nullable PortalConfig config;

    /// Gets the current portal configuration
    /// @return a copy of the current configuration
    /// @throws IllegalStateException if config has not been initialized
    public static PortalConfig get() {
        PortalConfig current = config;
        if (current == null) {
            throw new IllegalStateException("Config has not been initialized");
        }
        return current.copy();
    }

    /// Initializes the config provider with a plugin instance
    /// Loads the configuration and disables the plugin if loading fails
    /// @param plugin the plugin instance
    public static void init(JavaPlugin plugin) {
        setPlugin(plugin);
        @Nullable String error = reload();
        if (error != null) {
            plugin.getLogger().severe("Failed to load config: " + error);
            getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /// Reloads the configuration from the config.yml file
    /// Thread-safe operation that updates the cached config
    /// @return null on success, error message on failure
    public static @Nullable String reload() {
        synchronized (RELOAD_LOCK) {
            JavaPlugin currentPlugin = plugin;
            if (currentPlugin == null) {
                return "Config provider has not been initialized";
            }
            try {
                var file = new File(currentPlugin.getDataFolder(), "config.yml");
                config = new BukkitConfigManagerBuilder().build().load(file, PortalConfig.class);
                return null;
            } catch (IOException | IllegalStateException e) {
                return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            }
        }
    }
}
