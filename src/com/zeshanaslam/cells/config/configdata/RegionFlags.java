package com.zeshanaslam.cells.config.configdata;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeshanaslam.cells.Main;
import com.zeshanaslam.cells.config.configdata.cells.Cell;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class RegionFlags {

    public int priority;
    public HashMap<String, String> flags;

    public RegionFlags(Main main) {
        priority = main.getConfig().getInt("Region.Priority");

        flags = new HashMap<>();
        for (String key: main.getConfig().getConfigurationSection("Region.DefaultFlags").getKeys(false)) {
            String value = main.getConfig().getString("Region.DefaultFlags." + key + ".Value");

            flags.put(key, value);
        }
    }

    public void addRegionFlags(ProtectedRegion protectedRegion, Cell cell, String world) {
        for (String flag: flags.keySet()) {
            String value = flags.get(flag);
            value = value.replace("%cellname%", cell.name);

            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "region flag " + protectedRegion.getId() + " -w " + world + " " + flag + " " + value);
        }
    }
}
