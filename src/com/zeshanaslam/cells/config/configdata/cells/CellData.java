package com.zeshanaslam.cells.config.configdata.cells;

import com.zeshanaslam.cells.Main;

import java.util.HashMap;

public class CellData {

    public HashMap<Integer, Cell> cells;
    private CellDataHelpers cellDataHelpers;

    public CellData(Main main) {
        cellDataHelpers = new CellDataHelpers(main);

        cells = new HashMap<>();
        for (Cell cell: cellDataHelpers.getAllCells()) {
            cells.put(cell.id, cell);
        }

        System.out.println("Loaded " + cells.size() + " cells!");
    }

    public void save() {
        for (Cell cell: cells.values()) {
            cellDataHelpers.createOrUpdateCell(cell);
        }

        System.out.println("Saved " + cells.size() + " cells!");
    }
}
