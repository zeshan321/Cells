package com.zeshanaslam.cells.config.configdata.cells;

import com.google.gson.Gson;
import com.zeshanaslam.cells.Main;
import com.zeshanaslam.cells.config.ConfigStore;
import com.zeshanaslam.cells.utils.FileHandler;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CellDataHelpers {

    private Main main;
    private Gson gson;
    private final String path;

    public CellDataHelpers(Main main) {
        this.main = main;
        this.gson = new Gson();
        this.path = ConfigStore.path + "cells/";
    }

    public void createOrUpdateCell(Cell cell) {
        FileHandler fileHandler = new FileHandler(path + cell.id + ".yml");
        fileHandler.set("data", gson.toJson(cell));
        fileHandler.save();
    }

    public void delete(Cell cell) {
        FileHandler fileHandler = new FileHandler(path + cell.id + ".yml");
        fileHandler.delete();
    }

    public Cell getCell(int id) {
        if (!FileHandler.fileExists(path + id + ".yml")) {
            System.err.println("Cell not found: " + path + id + ".yml");
            return null;
        }

        FileHandler fileHandler = new FileHandler(path + id + ".yml");
        return gson.fromJson(fileHandler.getString("data"), Cell.class);
    }

    public List<Cell> getAllCells() {
        List<Cell> cells = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {

            List<String> result = walk.map(Path::toString)
                    .filter(f -> f.contains(".yml"))
                    .collect(Collectors.toList());

            for (String file: result) {
                file = file.replace("plugins\\Cells\\cells\\", "").replace("plugins/Cells/cells/", "").replace(".yml", "");
                int id = Integer.parseInt(file);

                cells.add(getCell(id));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cells;
    }
}
