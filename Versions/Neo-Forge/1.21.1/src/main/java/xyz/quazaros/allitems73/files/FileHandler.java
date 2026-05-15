package xyz.quazaros.allitems73.files;

import net.neoforged.fml.loading.FMLPaths;
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

import static xyz.quazaros.allitems73.files.WorldKeys.getWorldKey;

public final class FileHandler {
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("AllItems");
    private static final String MODID = "allitems73";

    private static Path getWorldFolder() {
        String key = WorldKeys.getWorldKey();
        if (key == null) return null;
        return CONFIG_DIR.resolve("Worlds").resolve(key);
    }

    // This looks for the world-specific item list
    public static List<String> getBaseItemList() {
        Path worldFolder = getWorldFolder();
        if (worldFolder == null) return loadListFromResources();

        Path worldListPath = worldFolder.resolve("items.txt");

        try {
            if (Files.exists(worldListPath)) {
                return Files.readAllLines(worldListPath, StandardCharsets.UTF_8);
            } else {
                // If it doesn't exist, create it from the default resource
                List<String> defaults = loadListFromResources();
                Files.createDirectories(worldFolder);
                Files.write(worldListPath, defaults, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                return defaults;
            }
        } catch (IOException e) {
            return loadListFromResources();
        }
    }

    // This looks for the progress data (is_found info)
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
                .filter(i -> i.is_found)
                .map(i -> i.data.makeString())
                .collect(Collectors.toList());

        try {
            Files.createDirectories(worldFolder);
            Files.write(progressPath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadListFromResources() {
        List<String> lines = new ArrayList<>();
        try (InputStream in = main.class.getResourceAsStream("/assets/" + MODID + "/items.txt")) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) lines.add(line.trim());
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return lines;
    }

    private static itemData parseItemLine(String input) {
        String[] parts = input.split(",");
        if (parts.length < 3) return null;
        return new itemData(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}