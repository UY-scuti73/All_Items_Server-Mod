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

import static xyz.quazaros.allitems73.files.WorldKeys.getWorldKey;

public final class FileHandler {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("AllItems");
    private static final String MODID = "allitems73";

    private FileHandler() {}

    /**
     * Helper to get the current world data path.
     * Returns null if world key isn't ready.
     */
    private static Path getDataPath() {
        String key = getWorldKey();
        if (key == null) return null;
        return CONFIG_DIR.resolve("Data").resolve(key + ".txt");
    }

    public static ArrayList<itemData> getItemData() {
        ArrayList<itemData> itemDataList = new ArrayList<>();
        Path path = getDataPath();

        if (path == null || !Files.exists(path)) {
            return itemDataList;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                itemData data = parseItemLine(line);
                if (data != null) itemDataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return itemDataList;
    }

    private static itemData parseItemLine(String input) {
        String[] parts = input.split(",");
        if (parts.length < 3) return null;

        return new itemData(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }

    public static void saveCurrentProgress() {
        Path path = getDataPath();
        if (path == null) {
            System.err.println("[AllItems] Cannot save: World Key is null!");
            return;
        }

        List<String> lines = main.ItemList.items.stream()
                .filter(i -> i.is_found)
                .map(i -> i.data.makeString())
                .collect(Collectors.toList());

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getBaseItemList() {
        Path path = CONFIG_DIR.resolve("items.txt");

        try {
            if (Files.exists(path)) {
                return (ArrayList<String>) Files.readAllLines(path, StandardCharsets.UTF_8);
            } else {
                // Export default from resources if missing
                List<String> defaults = loadListFromResources();
                Files.createDirectories(CONFIG_DIR);
                Files.write(path, defaults, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                return (ArrayList<String>) defaults;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<String> loadListFromResources() {
        List<String> lines = new ArrayList<>();
        String resourcePath = "/assets/" + MODID + "/items.txt";

        try (InputStream in = FileHandler.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}