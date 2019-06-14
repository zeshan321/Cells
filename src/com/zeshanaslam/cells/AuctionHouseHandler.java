package com.zeshanaslam.cells;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeshanaslam.cells.config.configdata.SafeLocation;
import com.zeshanaslam.cells.config.configdata.cells.SafeBlock;
import com.zeshanaslam.cells.config.configdata.auctionhouse.AuctionHouse;
import com.zeshanaslam.cells.config.configdata.cells.Cell;
import com.zeshanaslam.cells.config.configdata.signs.BuySign;
import com.zeshanaslam.cells.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AuctionHouseHandler {

    private Main main;
    private WorldUtils worldUtils;

    public AuctionHouseHandler(Main main) {
        this.main = main;
        this.worldUtils = new WorldUtils(main);
    }

    public boolean create(Player player, String name, Material placeOn, Material frontOf) throws IncompleteRegionException {
        Region selection = worldUtils.getRegionSelection(player);
        if (selection == null) {
            return false;
        }

        ProtectedRegion region = new ProtectedCuboidRegion(name, selection.getMinimumPoint(), selection.getMaximumPoint());
        RegionManager regionManager = worldUtils.getRegionManager(player);
        if (regionManager == null) {
            return false;
        }

        regionManager.addRegion(region);

        AuctionHouse auctionHouse = new AuctionHouse(name, placeOn.name(), frontOf.name(), player.getWorld().getUID());
        main.configStore.auctionHouseData.auctionHouses.put(auctionHouse.name, auctionHouse);
        return true;
    }

    public boolean auctionCell(Cell cell, List<ItemStack> itemStacks) {
        if (cell.auctionHouse.equalsIgnoreCase("none"))
            return true;

        AuctionHouse auctionHouse = main.configStore.auctionHouseData.auctionHouses.get(cell.auctionHouse);

        for (SafeBlock safeBlock: cell.placedBlocks) {
            BlockData blockData = safeBlock.getBlockData();
            ItemStack itemStack = new ItemStack(blockData.getMaterial());
            itemStacks.add(itemStack);
        }

        Collections.shuffle(itemStacks);
        int startIndex = 0;

        while(startIndex < itemStacks.size()) {
            int taking = getRandomNumberInRange(main.configStore.auctionHouseData.perChestMin, main.configStore.auctionHouseData.perChestMax);

            Location location = createChests(auctionHouse, 1, cell).get(0);
            Chest chest = (Chest) location.getBlock().getState();
            Inventory inventory = chest.getBlockInventory();

            List<ItemStack> toAdd = itemStacks.subList(startIndex, ((startIndex + taking) > itemStacks.size()) ? itemStacks.size() : taking + startIndex);
            startIndex += toAdd.size();

            for (ItemStack itemStack: toAdd) {
                inventory.addItem(itemStack);
            }
        }

        return true;
    }

    public List<Location> createChests(AuctionHouse auctionHouse, int amount, Cell cell) {
        List<Location> locations = Lists.newArrayList();

        World world = Bukkit.getWorld(auctionHouse.world);
        RegionManager regionManager = worldUtils.getRegionManager(world);
        ProtectedRegion region = regionManager.getRegion(auctionHouse.name);
        if (region == null) {
            System.err.println("Cells error! Region no longer exists for auction house. Manual deletion?");
            return null;
        }

        BlockVector3 max = region.getMaximumPoint();
        BlockVector3 min = region.getMinimumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() != Material.matchMaterial(auctionHouse.placedOn))
                        continue;

                    Block newBlock = world.getBlockAt(x, y + 1, z);
                    if (newBlock.getType() != Material.AIR)
                        continue;

                    locations.add(newBlock.getLocation());
                }
            }
        }

        Collections.shuffle(locations);

        List<Location> chests = Lists.newArrayList();
        for (int i = 0; i < amount; i++) {
            Block block = locations.get(i).getBlock();
            Location location = block.getLocation();
            block.setType(Material.CHEST);

            Block signBlock = null;

            Directional directional = (Directional) block.getBlockData();
            if (block.getRelative(BlockFace.NORTH).getType() == Material.CLAY) {
                directional.setFacing(BlockFace.SOUTH);

                signBlock = createSignBlock(block, BlockFace.SOUTH, BlockFace.SOUTH);
            } else if (block.getRelative(BlockFace.EAST).getType() == Material.CLAY) {
                directional.setFacing(BlockFace.WEST);

                signBlock = createSignBlock(block, BlockFace.WEST, BlockFace.WEST);
            } else if (block.getRelative(BlockFace.WEST).getType() == Material.CLAY) {
                directional.setFacing(BlockFace.EAST);

                signBlock = createSignBlock(block, BlockFace.EAST, BlockFace.EAST);
            } else {
                directional.setFacing(BlockFace.NORTH);

                signBlock = createSignBlock(block, BlockFace.NORTH, BlockFace.NORTH);
            }
            block.setBlockData(directional);

            SafeLocation signLocation = new SafeLocation().fromLocation(signBlock.getLocation());
            SafeLocation blockLocation = new SafeLocation().fromLocation(block.getLocation());
            BuySign buySign = new BuySign(cell.id, signLocation, blockLocation,
                    0,
                    null,
                    getRandomNumberInRange((int) main.configStore.auctionHouseData.lowest, (int) main.configStore.auctionHouseData.highest),
                    cell.tenant);

            main.configStore.buySignData.signs.put(signLocation.getLocation(), buySign);

            chests.add(block.getLocation());
        }

        return chests;
    }

    public int getRandomNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public int getRandomNumberInRangeDouble(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public Block createSignBlock(Block block, BlockFace relative, BlockFace blockFace) {
        Block signBlock = block.getRelative(relative);
        signBlock.setType(Material.OAK_WALL_SIGN);

        Directional directional = (Directional) signBlock.getBlockData();
        directional.setFacing(blockFace);
        signBlock.setBlockData(directional);
        return signBlock;
    }
}
