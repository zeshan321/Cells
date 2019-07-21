package com.zeshanaslam.cells.utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.zeshanaslam.cells.Main;
import com.zeshanaslam.cells.config.ConfigStore;
import com.zeshanaslam.cells.config.configdata.cells.Cell;
import com.zeshanaslam.cells.config.configdata.cells.CellDataHelpers;
import com.zeshanaslam.cells.config.configdata.groups.Group;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CellHelpers {

    private Main main;
    private Gson gson;
    private final String path;
    private WorldUtils worldUtils;

    public CellHelpers(Main main) {
        this.main = main;
        this.gson = new Gson();
        this.path = "plugins/Cells/cells/";
        this.worldUtils = new WorldUtils(main);
    }

    public int getNextId() {
        return main.configStore.cellCounter++;
    }

    public boolean createRegion(Player player, Cell cell) {
        try {
            Region selection = worldUtils.getRegionSelection(player);
            if (selection == null) {
                return false;
            }

            ProtectedRegion region = new ProtectedCuboidRegion("cellsplugin" + cell.id, selection.getMinimumPoint(), selection.getMaximumPoint());
            region.setPriority(main.configStore.regionFlags.priority);

            DefaultDomain defaultDomain = new DefaultDomain();
            defaultDomain.addPlayer(WorldGuardPlugin.inst().wrapPlayer(player));
            region.setMembers(defaultDomain);

            RegionManager regionManager = worldUtils.getRegionManager(player);
            if (regionManager == null) {
                return false;
            }

            ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(region);
            for (ProtectedRegion protectedRegion: applicableRegionSet.getRegions()) {
                if (protectedRegion.getId().startsWith("cellsplugin")) {
                    return false;
                }
            }

            regionManager.addRegion(region);
            main.configStore.regionFlags.addRegionFlags(region, cell, selection.getWorld().getName());
            return true;
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Cell getCellAtLocation(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));

        if (regions == null) {
            return null;
        }

        BlockVector3 min = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ProtectedRegion region = new ProtectedCuboidRegion("temp", min, min);
        ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(region);
        for (ProtectedRegion protectedRegion: applicableRegionSet.getRegions()) {
            if (protectedRegion.getId().startsWith("cellsplugin")) {
                return getCell(Integer.valueOf(protectedRegion.getId().replace("cellsplugin", "")));
            }
        }

        return null;
    }

    public void createOrUpdateCell(Cell cell) {
        main.configStore.cellData.cells.put(cell.id, cell);
    }

    public Cell getCell(int id) {
        return main.configStore.cellData.cells.get(id);
    }

    public List<Cell> getCellsByGroup(String groupType) {
        List<Cell> cells = new ArrayList<>();

        for (Cell cell: main.configStore.cellData.cells.values()) {
            if (cell.group.type.equalsIgnoreCase(groupType))
                cells.add(cell);
        }

        return cells;
    }

    public List<Cell> getCellsByType(CommandSender sender, String type) {
        List<Cell> cells = new ArrayList<>();

        if (main.configStore.isNumeric(type)) {
            Cell cell = getCell(Integer.valueOf(type));
            if (cell == null) {
                sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.CellNotFound));
                return cells;
            }

            cells.add(cell);
        } else {
            if (type.equalsIgnoreCase("all")) {
                cells = new ArrayList<>(main.configStore.cellData.cells.values());
            } else {
                if (!main.configStore.groupData.groups.containsKey(type)) {
                    sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.GroupNotFound));
                    return cells;
                }

                cells = getCellsByGroup(type);
            }
        }

        return cells;
    }

    public List<Cell> getPlayerCells(Player player) {
        List<Cell> cells = Lists.newArrayList();

        for (Cell cell: main.configStore.cellData.cells.values()) {
            if (cell.tenant != null && cell.tenant.equals(player.getUniqueId()))
                cells.add(cell);
        }

        return cells;
    }

    public boolean canRent(Player player, String group) {
        if (player.hasPermission("cells.rent.*"))
            return true;

        // Starts at 1 because player is checking if they can create one.
        int rented = 1;
        for (Cell cell: main.configStore.cellData.cells.values()) {
            if (cell.tenant != null && cell.tenant.equals(player.getUniqueId()))
                rented++;
        }

        PermissionAttachmentInfo[] playerPermissions = player.getEffectivePermissions()
                .stream()
                .filter(p -> p.getPermission().toLowerCase().startsWith("cell.rent."))
                .toArray(PermissionAttachmentInfo[]::new);

        List<Integer> max = new ArrayList<>();
        for (PermissionAttachmentInfo permissionAttachmentInfo: playerPermissions) {
            String permissionValue = permissionAttachmentInfo.getPermission().toLowerCase().replace("cell.rent.", "");
            if (!main.configStore.isNumeric(permissionValue))
                continue;

            max.add(Integer.parseInt(permissionValue));
        }

        if (!max.isEmpty()) {
            int maxAmount = max.stream()
                    .mapToInt(v -> v)
                    .max().orElseThrow(NoSuchElementException::new);

            if (rented <= maxAmount)
                return true;
        }

        playerPermissions = player.getEffectivePermissions()
                .stream()
                .filter(p -> p.getPermission().toLowerCase().startsWith("cell.rent." + group + "."))
                .toArray(PermissionAttachmentInfo[]::new);

        max = new ArrayList<>();
        for (PermissionAttachmentInfo permissionAttachmentInfo: playerPermissions) {
            String permissionValue = permissionAttachmentInfo.getPermission().toLowerCase().replace("cell.rent." + group + ".", "");
            if (!main.configStore.isNumeric(permissionValue))
                continue;

            max.add(Integer.parseInt(permissionValue));
        }

        if (!max.isEmpty()) {
            int maxAmount = max.stream()
                    .mapToInt(v -> v)
                    .max().orElseThrow(NoSuchElementException::new);

            if (rented <= maxAmount)
                return true;
        }

        return false;
    }

    public boolean rentCell(Player player, Cell cell) {
        if (cell.tenant == null) {
            Group group = main.configStore.groupData.groups.get(cell.group.type);

            if (!player.hasPermission(group.permission)) {
                player.sendMessage(main.configStore.messages.get(ConfigStore.Messages.CannotRentInGroup));
                return false;
            }

            if (!canRent(player, group.type.toLowerCase())) {
                player.sendMessage(main.configStore.messages.get(ConfigStore.Messages.CannotRentAnyMore));
                return false;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            if (!main.economy.has(offlinePlayer, cell.price)) {
                player.sendMessage(main.configStore.messages.get(ConfigStore.Messages.NotEnoughMoney));
                return false;
            }

            World world = Bukkit.getWorld(cell.world);
            RegionManager regionManager = new WorldUtils(main).getRegionManager(world);
            ProtectedRegion region = regionManager.getRegion("cellsplugin" + cell.id);
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Cells error! Report to staff. Region no longer exists for cell. Manual deletion?");
                return false;
            }

            main.economy.withdrawPlayer(offlinePlayer, cell.price);

            cell.tenant = player.getUniqueId();
            cell.rentTimestamp = System.currentTimeMillis();
            createOrUpdateCell(cell);

            region.getMembers().addPlayer(cell.tenant);
            player.sendMessage(main.configStore.messages.get(ConfigStore.Messages.RentedCell));
            return true;
        }

        return false;
    }

    public void deleteCell(Cell cell, String world) {
        new CellDataHelpers(main).delete(cell);
        main.configStore.cellData.cells.remove(cell.id);

        world = (cell.home == null) ? world : Bukkit.getWorld(cell.home.world).getName();

        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "rg remove -w " + world + " cellsplugin" + cell.id);
    }
}
