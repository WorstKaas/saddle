/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import com.crimsonwarpedcraft.cwcommons.store.DataStore;
import com.crimsonwarpedcraft.cwcommons.store.KeySerializers;
import com.crimsonwarpedcraft.cwcommons.store.bukkit.BukkitDataStoreBuilder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import strelitzia.saddle.portal.command.PortalCommand;
import strelitzia.saddle.portal.config.PortalConfigProvider;
import strelitzia.saddle.portal.effect.ParticleEffectManager;
import strelitzia.saddle.portal.gui.PortalGuiFactory;
import strelitzia.saddle.portal.integration.BlueMapIntegration;
import strelitzia.saddle.portal.listener.PlayerListener;

import java.io.IOException;

/// Main plugin class for the Portal command module
/// Handles initialization of data stores, particle effects, BlueMap integration, and event listeners
public final class PortalPlugin extends JavaPlugin {

    /// Data store for persisting portal and player data
    private DataStore store;
    /// Flush task for auto-flushing data store
    private FoliaFlushTask flushTask;
    /// Scheduled task for displaying particle effects
    private ScheduledTask particleTask;

    @Getter
    @Setter
    private static PortalPlugin instance;

    /// Initializes CommandAPI on plugin load
    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIPaperConfig(this));
    }

    /// Initializes all plugin components on enable
    /// Sets up data store, repositories, particle effects, BlueMap integration, and event listeners
    @Override
    public void onEnable() {
        PortalPlugin.setInstance(this);
        saveDefaultConfig();
        PortalConfigProvider.init(this);

        try {
            store = new BukkitDataStoreBuilder(getName(), getDataFolder()).build();
        } catch (IOException e) {
            getLogger().severe("Failed to open data store: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

        flushTask = new FoliaFlushTask(store, this);
        flushTask.start();

        var portalRepository = store.repository("saddle.portal.portals", Portal.class, KeySerializers.forString());
        var favouriteRepository = store.repository("saddle.portal.favourites", FavouritePortals.class,
                KeySerializers.forUuid());

        var blueMapIntegration = new BlueMapIntegration(this, portalRepository);
        blueMapIntegration.initialize();

        var portalManager = new PortalManager(portalRepository, favouriteRepository, blueMapIntegration);
        PortalGuiFactory.init(portalManager);

        var particleEffectManager = new ParticleEffectManager(this, portalRepository);
        particleTask = particleEffectManager.start();


        getServer().getPluginManager().registerEvents(new PlayerListener(portalManager, this), this);

        new PortalCommand(portalManager).register();

        getLogger().info("PortalPlugin has been enabled");
    }

    /// Cleans up resources on plugin disable
    /// Cancels scheduled tasks and closes data store
    @Override
    public void onDisable() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        if (flushTask != null) {
            flushTask.stop();
        }
        setInstance(null);
        if (store != null) {
            try {
                store.close();
            } catch (Exception e) {
                getLogger().severe("Failed to close data store: " + e.getMessage());
            }
        }
        getLogger().info("PortalPlugin has been disabled");
    }
}
