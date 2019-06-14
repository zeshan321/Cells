package com.zeshanaslam.cells;

import app.ashcon.intake.bukkit.BukkitIntake;
import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph;
import app.ashcon.intake.fluent.DispatcherNode;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.zeshanaslam.cells.commands.CellsCommands;
import com.zeshanaslam.cells.config.ConfigStore;
import com.zeshanaslam.cells.listeners.BlockListener;
import com.zeshanaslam.cells.listeners.CellListener;
import com.zeshanaslam.cells.listeners.SignListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    public WorldGuardPlugin worldGuardPlugin;
    public WorldEditPlugin worldEditPlugin;
    public Economy economy;
    public ConfigStore configStore;
    public RentHandler rentHandler;
    public AuctionHouseHandler auctionHouseHandler;

    @Override
    public void onEnable() {
        super.onEnable();

        saveDefaultConfig();

        // Cells directory
        File cellsDirectory = new File("plugins/Cells/cells/");
        if (!cellsDirectory.exists()) {
            cellsDirectory.mkdir();
        }

        // Register config loader
        configStore = new ConfigStore(this);

        // Register worldguard
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            this.worldGuardPlugin = ((WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard"));
        }

        // Register worldedit
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            this.worldEditPlugin = ((WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit"));
        }

        // Register intake commands
        BasicBukkitCommandGraph basicBukkitCommandGraph = new BasicBukkitCommandGraph();
        DispatcherNode dispatcherNode = basicBukkitCommandGraph.getRootDispatcherNode().registerNode("cells");
        dispatcherNode.registerCommands(new CellsCommands(this));

        BukkitIntake bukkitIntake = new BukkitIntake(this, basicBukkitCommandGraph);
        bukkitIntake.register();

        // Hook into vault
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }

        // Rent handler
        rentHandler = new RentHandler(this);

        // Auction house
        auctionHouseHandler = new AuctionHouseHandler(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new CellListener(this), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        configStore.save(this);
    }
}
