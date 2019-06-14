package com.zeshanaslam.cells.config.configdata.signs;

import com.google.gson.Gson;
import com.zeshanaslam.cells.Main;
import com.zeshanaslam.cells.config.ConfigStore;
import com.zeshanaslam.cells.config.configdata.cells.Cell;
import com.zeshanaslam.cells.utils.FileHandler;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class BuySignData {

    private Main main;
    private final Gson gson;
    private final String path;
    public HashMap<Location, BuySign> signs;
    private List<String> ahSignUnclaimed;
    private List<String> ahSignClaimed;
    private String ahFormat;

    public BuySignData(Main main) {
        this.main = main;
        this.signs = new HashMap<>();
        this.gson = new Gson();
        this.path = ConfigStore.path + "buysigns.yml";

        FileHandler fileHandler = new FileHandler(this.path);
        if (fileHandler.contains("Signs")) {
            List<String> signData = fileHandler.getStringList("Signs");
            for (String json: signData) {
                BuySign sign = gson.fromJson(json, BuySign.class);

                signs.put(sign.location.getLocation(), sign);
            }
        }

        System.out.println("Loaded " + signs.size() + " buy signs!");

        ahSignUnclaimed = main.getConfig().getStringList("Signs.Auction.Unclaimed");
        ahSignClaimed = main.getConfig().getStringList("Signs.Auction.Claimed");
        ahFormat = main.getConfig().getString("Signs.Auction.Format");

        signUpdate(main);
    }

    public void save() {
        FileHandler fileHandler = new FileHandler(this.path);

        List<String> json = new ArrayList<>();
        for (BuySign sign: signs.values()) {
            json.add(gson.toJson(sign));
        }

        fileHandler.createNewStringList("Signs", json);
        fileHandler.save();

        System.out.println("Saved " + json.size() + " buy signs!");
    }

    public List<String> getBuySign(BuySign buySign) {
        List<String> sign = new ArrayList<>();

        LocalDateTime localDateTime = LocalDateTime.now();

        LocalDateTime triggerTime;
        Duration duration;
        OfflinePlayer offlinePlayer = null;
        String formattedTimeLeft = null;
        if (buySign.owner != null) {
            offlinePlayer = Bukkit.getOfflinePlayer(buySign.owner);
            triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(buySign.bought), TimeZone.getDefault().toZoneId())
                    .plusSeconds(main.configStore.auctionHouseData.chestTime);

            duration = getDuration(localDateTime, triggerTime);
            formattedTimeLeft = getTimeLeft(duration, ahFormat);

            // Time is over for cell. Notify rent handler.
            if (duration.isZero() || duration.isNegative()) {
               main.rentHandler.stopRentChest(buySign);
            }
        }

        OfflinePlayer originalOwner = Bukkit.getOfflinePlayer(buySign.cellOwner);

        for (String line: buySign.owner == null ? ahSignUnclaimed : ahSignClaimed) {
            LocalDateTime later = localDateTime.plusSeconds(main.configStore.auctionHouseData.chestTime);

            line = ChatColor.translateAlternateColorCodes('&', line)
                    .replace("%time%", getTimeLeft(getDuration(localDateTime, later), ahFormat))
                    .replace("%price%", String.valueOf(buySign.price))
                    .replace("%cellowner%", originalOwner.getName());

            if (buySign.owner != null) {
                line = line.replace("%auctiontimeleft%", formattedTimeLeft)
                        .replace("%owner%", offlinePlayer.getName());
            }

            sign.add(line);
        }

        return sign;
    }

    private Duration getDuration(LocalDateTime current, LocalDateTime later) {
        return Duration.between(current, later);
    }

    private String getTimeLeft(Duration duration, String format) {
        return DurationFormatUtils.formatDuration(duration.toMillis(), format, true);
    }

    private void signUpdate(Main main) {
        main.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
            Iterator<Location> locationIterator = signs.keySet().iterator();
            while (locationIterator.hasNext()) {
                Location location = locationIterator.next();
                BuySign buySign = signs.get(location);

                Block block = location.getBlock();
                if (!(block.getState() instanceof Sign)) {
                    locationIterator.remove();
                    continue;
                }

                Sign sign = (Sign) block.getState();

                List<String> signData = main.configStore.buySignData.getBuySign(buySign);
                String[] signDataArray = signData.toArray(new String[0]);
                if (Arrays.equals(signDataArray, sign.getLines()))
                    continue;

                for (int i = 0; i < signData.size(); i++) {
                    sign.setLine(i, signData.get(i));
                }

                sign.update();
            }
        }, 0, 20L);
    }
}
