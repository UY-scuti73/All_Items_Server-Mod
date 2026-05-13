package xyz.quazaros.allitems73.files;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

public class WorldKeys {
    private static String worldKey;

    public static void setWorldKey(MinecraftServer server) {
        if (server == null) {
            worldKey = null;
            return;
        }

        if (server.isDedicatedServer()) {
            worldKey = getDedicatedWorldId(server);
        } else {
            worldKey = getSingleplayerWorldId(server);
        }
    }

    public static String getWorldKey() {
        return worldKey;
    }

    private static String getSingleplayerWorldId(MinecraftServer server) {
        String levelName = server.getWorldData().getLevelName();
        return "sp_" + sanitize(levelName);
    }

    private static String getDedicatedWorldId(MinecraftServer server) {
        String base;

        if (server instanceof DedicatedServer dedicated) {
            base = dedicated.getProperties().levelName;
        } else {
            base = server.getWorldData().getLevelName();
        }

        return "mp_" + sanitize(base);
    }

    private static String sanitize(String in) {
        if (in == null) return "unknown";
        return in.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}