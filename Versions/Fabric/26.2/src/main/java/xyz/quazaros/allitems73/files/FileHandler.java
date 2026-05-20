package xyz.quazaros.allitems73.files;

import net.fabricmc.loader.api.FabricLoader;
import xyz.quazaros.allitems73.items.itemData;
import xyz.quazaros.allitems73.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FileHandler {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("AllItems");
    private static final String MODID = "allitems73";

    private static Path getWorldFolder() {
        String key = WorldKeys.getWorldKey();
        if (key == null) return null;
        return CONFIG_DIR.resolve("Worlds").resolve(key);
    }

    /** Returns the world-specific base item list (or defaults if missing). */
    public static List<String> getBaseItemList() {
        Path worldFolder = getWorldFolder();
        if (worldFolder == null) return loadDefaults();

        Path worldListPath = worldFolder.resolve("items.txt");
        try {
            if (Files.exists(worldListPath)) {
                return Files.readAllLines(worldListPath, StandardCharsets.UTF_8);
            }

            // If it doesn't exist, seed it from defaults.
            List<String> defaults = loadDefaults();
            Files.createDirectories(worldFolder);
            Files.write(
                    worldListPath,
                    defaults,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            return defaults;
        } catch (IOException e) {
            e.printStackTrace();
            return loadDefaults();
        }
    }

    /** Call during mod init to ensure CONFIG_DIR/default_list.txt exists. */
    public static void initDefaultList() {
        Path defaultListPath = CONFIG_DIR.resolve("default_list.txt");
        if (Files.exists(defaultListPath)) return;

        List<String> defaults = loadListFromResources();
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.write(
                    defaultListPath,
                    defaults,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Returns the default item list from config, falling back to resources. */
    private static List<String> loadDefaults() {
        Path defaultListPath = CONFIG_DIR.resolve("default_list.txt");
        if (Files.exists(defaultListPath)) {
            try {
                List<String> lines = Files.readAllLines(defaultListPath, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) return lines;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return loadListFromResources();
    }

    /** Returns saved progress data for the current world. */
    public static ArrayList<itemData> getItemData() {
        ArrayList<itemData> itemDataList = new ArrayList<>();
        Path worldFolder = getWorldFolder();
        if (worldFolder == null) return itemDataList;

        Path progressPath = worldFolder.resolve("progress.txt");
        if (!Files.exists(progressPath)) return itemDataList;

        try {
            List<String> lines = Files.readAllLines(progressPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                itemData data = parseItemLine(line);
                if (data != null) itemDataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemDataList;
    }

    public static void saveCurrentProgress() {
        Path worldFolder = getWorldFolder();
        if (worldFolder == null) return;

        Path progressPath = worldFolder.resolve("progress.txt");
        List<String> lines = main.getItemList().items.stream()
                .filter(i -> i.is_found && i.data != null)
                .map(i -> i.data.makeString())
                .collect(Collectors.toList());

        try {
            Files.createDirectories(worldFolder);
            Files.write(
                    progressPath,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadListFromResources() {
        List<String> lines = new ArrayList<>();
        try (InputStream in = main.class.getResourceAsStream("/assets/" + MODID + "/items.txt")) {
            if (in == null) return lines;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private static itemData parseItemLine(String input) {
        String[] parts = input.split(",");
        if (parts.length < 3) return null;
        return new itemData(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}