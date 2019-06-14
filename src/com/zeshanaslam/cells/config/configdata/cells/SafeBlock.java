package com.zeshanaslam.cells.config.configdata.cells;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

public class SafeBlock {
    public String world;
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;
    public String blockData;

    public SafeBlock(String world, double x, double y, double z, float pitch, float yaw, String blockData) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.blockData = blockData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SafeBlock safeBlock = (SafeBlock) o;
        return Double.compare(safeBlock.x, x) == 0 &&
                Double.compare(safeBlock.y, y) == 0 &&
                Double.compare(safeBlock.z, z) == 0 &&
                Float.compare(safeBlock.pitch, pitch) == 0 &&
                Float.compare(safeBlock.yaw, yaw) == 0 &&
                Objects.equals(world, safeBlock.world) &&
                Objects.equals(blockData, safeBlock.blockData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, pitch, yaw, blockData);
    }

    public BlockData getBlockData() {
        return Bukkit.getServer().createBlockData(blockData);
    }

    public Location getLocation() {
        Location location = new Location(Bukkit.getWorld(world), x, y, z);
        location.setPitch(pitch);
        location.setYaw(yaw);

        return location;
    }
}
