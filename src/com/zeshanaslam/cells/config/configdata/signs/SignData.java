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

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SignData {

    public HashMap<Location, CellSign> signs;
    private final Gson gson;
    private final String path;
    private List<String> cellSignUnclaimed;
    private List<String> cellSignClaimed;
    private String cellFormat;
    private Main main;

    public SignData(Main main) {
        this.main = main;
        this.signs = new HashMap<>();
        this.gson = new Gson();
        this.path = ConfigStore.path + "signs.yml";

        FileHandler fileHandler = new FileHandler(this.path);
        if (fileHandler.contains("Signs")) {
            List<String> signData = fileHandler.getStringList("Signs");
            for (String json: signData) {
                CellSign sign = gson.fromJson(json, CellSign.class);

                signs.put(sign.location.getLocation(), sign);
            }
        }

        System.out.println("Loaded " + signs.size() + " signs!");

        cellSignUnclaimed = main.getConfig().getStringList("Signs.Cell.Unclaimed");
        cellSignClaimed = main.getConfig().getStringList("Signs.Cell.Claimed");
        cellFormat = main.getConfig().getString("Signs.Cell.Format");

        // Start updating signs
        signUpdate(main);
    }

    public void save() {
        FileHandler fileHandler = new FileHandler(this.path);

        List<String> json = new ArrayList<>();
        for (CellSign sign: signs.values()) {
            json.add(gson.toJson(sign));
        }

        fileHandler.createNewStringList("Signs", json);
        fileHandler.save();

        System.out.println("Saved " + json.size() + " signs!");
    }

    public List<String> getCellSign(Cell cell) {
        List<String> sign = new ArrayList<>();

        LocalDateTime localDateTime = LocalDateTime.now();

        LocalDateTime triggerTime;
        Duration duration;
        OfflinePlayer offlinePlayer = null;
        String formattedTimeLeft = null;
        if (cell.tenant != null) {
            offlinePlayer = Bukkit.getOfflinePlayer(cell.tenant);
            triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(cell.rentTimestamp), TimeZone.getDefault().toZoneId())
                    .plusDays(cell.rentDays);

            duration = getDuration(localDateTime, triggerTime);
            formattedTimeLeft = getTimeLeft(duration, cellFormat);

            // Time is over for cell. Notify rent handler.
            if (duration.isZero() || duration.isNegative()) {
                main.rentHandler.onComplete(cell);
            }
        }

        for (String line: cell.tenant == null ? cellSignUnclaimed : cellSignClaimed) {
            LocalDateTime later = localDateTime.plusDays(cell.rentDays);


            line = ChatColor.translateAlternateColorCodes('&', line)
                    .replace("%time%", getTimeLeft(getDuration(localDateTime, later), cellFormat))
                    .replace("%price%", String.valueOf(cell.price))
                    .replace("%rentcost%", String.valueOf(getRenewAmount(cell)));

            if (cell.tenant != null) {
                line = line.replace("%celltimeleft%", formattedTimeLeft)
                        .replace("%owner%", offlinePlayer.getName());
            }

            sign.add(line);
        }

        return sign;
    }

    public double getRenewAmount(Cell cell) {
        LocalDateTime rentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(cell.rentTimestamp), TimeZone.getDefault().toZoneId());

        Duration difference = getDuration(LocalDateTime.now(), rentTime.plusDays(cell.rentDays));
        Duration difference1 = getDuration(rentTime, rentTime.plusDays(cell.rentDays));

        long minutes = difference1.toMinutes() - difference.toMinutes();
        double amount = cell.price / 24 / 60;

        return Math.round((minutes * amount) * 100.0) / 100.0;
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
                CellSign cellSign = signs.get(location);

                Cell cell = main.configStore.cellData.cells.get(cellSign.cellId);
                if (cell == null) {
                    locationIterator.remove();
                } else {
                    Block block = location.getBlock();
                    Sign sign = (Sign) block.getState();

                    List<String> signData = main.configStore.signData.getCellSign(cell);
                    String[] signDataArray = signData.toArray(new String[0]);
                    if (Arrays.equals(signDataArray, sign.getLines()))
                        continue;

                    for (int i = 0; i < signData.size(); i++) {
                        sign.setLine(i, signData.get(i));
                    }

                    sign.update();
                }
            }
        }, 0, 20L);
    }
}
