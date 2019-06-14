package com.zeshanaslam.cells.config.configdata.auctionhouse;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.zeshanaslam.cells.Main;
import com.zeshanaslam.cells.config.ConfigStore;
import com.zeshanaslam.cells.utils.FileHandler;

import java.util.HashMap;
import java.util.List;

public class AuctionHouseData {

    private Main main;
    private Gson gson;
    public HashMap<String, AuctionHouse> auctionHouses;
    public double lowest;
    public double highest;
    public int perChestMin;
    public int perChestMax;
    public int chestTime;

    public AuctionHouseData(Main main) {
        this.main = main;
        this.gson = new Gson();
        this.auctionHouses = new HashMap<>();

        lowest = main.getConfig().getDouble("AuctionHouse.Lowest");
        highest = main.getConfig().getDouble("AuctionHouse.Highest");
        perChestMin = main.getConfig().getInt("AuctionHouse.PerChest.Min");
        perChestMax = main.getConfig().getInt("AuctionHouse.PerChest.Max");
        chestTime = main.getConfig().getInt("AuctionHouse.ChestTime");

        FileHandler auctionConfig = new FileHandler(ConfigStore.path + "auctions.yml");
        if (auctionConfig.contains("AuctionHouses")) {
            for (String json: auctionConfig.getStringList("AuctionHouses")) {
                AuctionHouse auctionHouse = gson.fromJson(json, AuctionHouse.class);

                auctionHouses.put(auctionHouse.name, auctionHouse);
            }
        }

        System.out.println("Loaded " + auctionHouses.size() + " auction houses!");
    }

    public void save() {
        List<String> json = Lists.newArrayList();
        for (AuctionHouse auctionHouse: auctionHouses.values()) {
            json.add(gson.toJson(auctionHouse));
        }

        FileHandler auctionConfig = new FileHandler(ConfigStore.path + "auctions.yml");
        auctionConfig.createNewStringList("AuctionHouses", json);
        auctionConfig.save();

        System.out.println("Saved " + auctionHouses.size() + " auction houses!");
    }
}
