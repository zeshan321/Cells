package com.zeshanaslam.cells.utils;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PlayerUtils {

    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(new Permission(permission, PermissionDefault.FALSE));
    }


}
