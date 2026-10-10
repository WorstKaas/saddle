// Copyright (c) 2026 WorstKaas. All rights reserved.

package worstkaas.saddle.command.rules;

import com.crimsonwarpedcraft.cwcommons.store.DataStore;
import com.crimsonwarpedcraft.cwcommons.store.KeySerializers;
import com.crimsonwarpedcraft.cwcommons.store.bukkit.BukkitDataStoreBuilder;
import com.crimsonwarpedcraft.cwcommons.store.bukkit.PlayerDataManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;
import worstkaas.saddle.command.rules.command.RulesCommand;
import worstkaas.saddle.command.rules.config.RulesConfigProvider;
import worstkaas.saddle.command.rules.listener.PlayerListener;

import java.io.IOException;

public final class RulesPlugin extends JavaPlugin {

    private DataStore store;
    private ScheduledTask autoFlushTask;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIPaperConfig(this));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RulesConfigProvider.init(this);
        try {
            store = new BukkitDataStoreBuilder(getName(), getDataFolder()).build();


        } catch (IOException e) {
            getLogger().severe("Failed to open data store: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

        autoFlushTask = new FoliaFlushTask(store, this).start();
        var playerDataRepository = store.repository("has-accepted", Boolean.class, KeySerializers.forUuid());
        var manager = new PlayerDataManager<>(playerDataRepository, this).registerEvents();

        getServer().getPluginManager().registerEvents(new PlayerListener(this, manager), this);

        new RulesCommand(this, playerDataRepository).register();

        getLogger().info("RulesPlugin has been enabled");
    }

    @Override
    public void onDisable() {
        if (autoFlushTask != null) {
            autoFlushTask.cancel();
        }

        if (store != null) {
            try {
                store.close();
            } catch (Exception e) {
                getLogger().severe("Failed to close data store: " + e.getMessage());
            }
        }
        getLogger().info("RulesPlugin has been disabled");
    }
}
