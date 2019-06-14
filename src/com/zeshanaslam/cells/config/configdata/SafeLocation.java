package com.zeshanaslam.cells.config.configdata;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class SafeLocation {
    public UUID world;
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    public SafeLocation() {}

    public SafeLocation(UUID world, double x, double y, double z, float pitch, float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Location getLocation() {
        Location location = new Location(Bukkit.getWorld(world), x, y, z);
        location.setPitch(pitch);
        location.setYaw(yaw);

        return location;
    }

    public SafeLocation fromLocation(Location location) {
        world = location.getWorld().getUID();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        pitch = location.getPitch();
        yaw = location.getYaw();

        return this;
    }
}
