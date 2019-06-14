package com.zeshanaslam.cells.config;

import com.zeshanaslam.cells.Main;
import com.zeshanaslam.cells.config.configdata.RegionFlags;
import com.zeshanaslam.cells.config.configdata.auctionhouse.AuctionHouseData;
import com.zeshanaslam.cells.config.configdata.cells.CellData;
import com.zeshanaslam.cells.config.configdata.groups.GroupData;
import com.zeshanaslam.cells.config.configdata.signs.BuySignData;
import com.zeshanaslam.cells.config.configdata.signs.SignData;
import org.bukkit.ChatColor;

import java.util.HashMap;

public class ConfigStore {

    public static final String path = "plugins/Cells/";
    public HashMap<Messages, String> messages;
    public GroupData groupData;
    public CellData cellData;
    public SignData signData;
    public BuySignData buySignData;
    public AuctionHouseData auctionHouseData;
    public RegionFlags regionFlags;
    public int cellCounter;
    public int defaultRentDays;

    public ConfigStore(Main main) {
        messages = new HashMap<>();
        for (String key: main.getConfig().getConfigurationSection("Messages").getKeys(false)) {
            messages.put(Messages.valueOf(key), ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Messages." + key)));
        }

        groupData = new GroupData(main);
        cellData = new CellData(main);
        signData = new SignData(main);
        buySignData = new BuySignData(main);
        auctionHouseData = new AuctionHouseData(main);
        regionFlags = new RegionFlags(main);
        cellCounter = main.getConfig().getInt("CellCounter");
        defaultRentDays = main.getConfig().getInt("DefaultRentDays");
    }

    public void save(Main main) {
        cellData.save();
        signData.save();
        buySignData.save();
        auctionHouseData.save();

        main.getConfig().set("CellCounter", cellCounter);
        main.saveConfig();
    }

    public enum Messages {
        CellCreated,
        GroupNotFound,
        UnableToCreateRegion,
        CellNotFound,
        SetHome,
        RentDays,
        SetPrice,
        HomeNotSet,
        MustBeLookingAtSign,
        AddedSign,
        CannotRentInGroup,
        CannotRentAnyMore,
        NotEnoughMoney,
        RentedCell,
        RemovedSIgn,
        NotCellSign,
        AlreadyCellSign,
        UnRentMoreCells,
        UnRented,
        CellDeleted,
        ResetCells,
        HomeMoreCells,
        Reloaded,
        GroupAlreadyExists,
        GroupAdded,
        GroupRemoved,
        InvalidMaterial,
        CreatedAuctionHouse,
        AuctionHouseAlreadyExists,
        AuctionHouseNotFound,
        BoughtChest,
        Renewed
    }

    public boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }
}
