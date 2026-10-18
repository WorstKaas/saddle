/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.nether.portal.blocker;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import strelitzia.saddle.nether.portal.blocker.listener.PortalCreateListener;

/// Main plugin class for the Nether Portal Blocker
/// Blocks portal creation if they would be generated outside the world border or in claimed homestead chunks
public final class PortalBlockerPlugin extends JavaPlugin {

    @Setter
    @Getter
    private static PortalBlockerPlugin instance;

    /// Initializes the plugin on enable
    @Override
    public void onEnable() {
        PortalBlockerPlugin.setInstance(this);
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PortalCreateListener(this), this);

        getLogger().info("PortalBlockerPlugin has been enabled");
    }

    /// Cleans up on plugin disable
    @Override
    public void onDisable() {
        setInstance(null);
        getLogger().info("PortalBlockerPlugin has been disabled");
    }
}
