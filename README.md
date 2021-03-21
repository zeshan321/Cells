# Cells
Custom cells plugin with auction house system for Lemmett.

Config: [Here](https://github.com/zeshan321/Cells/blob/master/config.yml)

### Commands:
- /Cells (Basic Command)
- /Cells Groupadd [group name] [permission]  (Groupname per cellward and a permission node that makes it where they can only rent cells in that group) (when permission gets removed their cell gets unrented)
- /Cells AHPos1 and AHPos2 (set the 2 positions of the AH area)
- /Cells AHCreate [Name] (creates the AH region)
- /Cells create [name] [price] [group] [auction name/none] (Creates a cell with name, price, group it belongs too, and auction house the cell belongs to if stated)
- /Cells setdays [intvalue] [cellid/group/all] (changes the amount of days the cells can be rented to for) 
- /Cells setcost [intvalue] [cellid/group/all] (changes the amount of money the cell can be rented for)
- /Cells sethome [cellid] (sets cell home)
- /Cells home (pretty simple explanation of what this does) 
- /Cells addsign [cellid] (Sets the sign the player is looking at to the cell)
- /Cells removesign (removes a cell from a sign that the player is looking at)
- /Cells refreshsigns
- /Cells rent [cellid] (Rents the cell) (Owner command only [Perm based only])
- /Cells unrent (Unrents the cell) (Owner command only [Perm based only])
- /Cells evict  [player]   (Unrents cell from player and sends items in cell to auction house) 
- /Cells reset [cellid/all]  (resets the cell without sending the items inside to the auction house)
- /Cells reload (reloads the plugin)

### Permissions:
- cells.permission - allows the player to do /cells
- cells.* - Grant every permission to a player
- cells.rent.<group>.<amount> - Grant permission for a player to rent an amount of cells per cell group.
- cells.rent.# - Allows player to rent that many cells in all groups made.
- cells.rent.* - Grant permission for a player to rent unlimited cells per cell group
- cells.admin.bypass.storageaccess - Grant permission for a player to open storage in any cell (Permbase only! NO OP)
- cells.admin.bypass.build - Grant permission for a player to build in any cell (Permbase only! NO OP)
- cells.command.create - Grant permission for a player to create, Groups, AuctionHouses, and Cells (Permbase only! NO OP)
- cells.command.list - Grant permission for a player to view all cell commands (Permbase only! NO OP)
- cells.command.home - Grants permission for a player to teleport to their cells (Permbase only! NO OP)
- cells.command.sethome - Grants permission for a player to set the home of any cell (Permbase only! NO OP)
- cells.command.evict- Grants permission for a player to evict the owner of a cell (Permbase  / consolecommand command only! NO OP)
- cells.command.setcost - Grants permission for a player to teleport to set the price of a cell(s) (Permbase only! NO OP)
- cells.command.removesign - Grants permission for a player to remove signs from cells (Permbase only! NO OP)
- cells.command.refreshsigns - Grant permission for a player to refresh all the signs (Permbase only! NO OP)
- cells.command.reload - Grant permission for a player to reload the configs (Permbase only! NO OP)


