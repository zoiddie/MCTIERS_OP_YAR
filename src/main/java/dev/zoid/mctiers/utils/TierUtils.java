package dev.zoid.mctiers.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TierUtils {
    private static final String TIER_URL = "https://mctiers.com/api/rankings/";
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";

    public enum PlayerTier {
        LT5(1), HT5(2), LT4(3), HT4(4), LT3(5), HT3(6), LT2(7), HT2(8), LT1(9), HT1(10), UNRANKED(-1);

        private static final Map<Integer, PlayerTier> tierMap = new HashMap<>();
        static { for (PlayerTier t : values()) tierMap.put(t.tierValue, t); }

        public final int tierValue;
        PlayerTier(int v) { tierValue = v; }
        public static PlayerTier from(int v) { return tierMap.getOrDefault(v, UNRANKED); }
    }

    public static class TierlistPlayer {
        public final UUID uuid;
        public final PlayerTier tier;
        public TierlistPlayer(UUID u, PlayerTier t) { uuid = u; tier = t; }
    }

    private static UUID fetchUUID(String username) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(MOJANG_API_URL + username).openConnection();
            c.setRequestMethod("GET");
            if (c.getResponseCode() == 200) {
                try (InputStream is = c.getInputStream()) {
                    String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    String id = JsonParser.parseString(s).getAsJsonObject().get("id").getAsString();
                    return UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static CompletableFuture<TierlistPlayer> requestFromAPI(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID u = fetchUUID(username);
                if (u == null) return new TierlistPlayer(UUID.randomUUID(), PlayerTier.UNRANKED);
                HttpURLConnection c = (HttpURLConnection) new URL(TIER_URL + u.toString().replace("-", "")).openConnection();
                c.setRequestMethod("GET");
                if (c.getResponseCode() == 200) {
                    try (InputStream is = c.getInputStream()) {
                        JsonObject o = JsonParser.parseString(new String(is.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                        JsonObject v = o.getAsJsonObject("vanilla");
                        int t = v.get("tier").getAsInt();
                        int p = v.get("pos").getAsInt();
                        return new TierlistPlayer(u, PlayerTier.from(p == 0 ? 12 - t*2 : 11 - t*2));
                    }
                }
            } catch (Exception ignored) {}
            return new TierlistPlayer(UUID.randomUUID(), PlayerTier.UNRANKED);
        });
    }
}