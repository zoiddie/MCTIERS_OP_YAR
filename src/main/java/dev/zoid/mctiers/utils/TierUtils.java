package dev.zoid.mctiers.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TierUtils {
    private static final String TIER_URL = "https://mctiers.com/api/rankings/";
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final int HTTP_OK = 200;

    public enum PlayerTier {
        LT5(1), HT5(2), LT4(3), HT4(4), LT3(5), HT3(6),
        LT2(7), HT2(8), LT1(9), HT1(10), UNRANKED(-1);

        private static final Map<Integer, PlayerTier> TIER_MAP = new HashMap<>();
        
        static {
            for (PlayerTier tier : values()) {
                TIER_MAP.put(tier.tierValue, tier);
            }
        }

        public final int tierValue;

        PlayerTier(int value) {
            this.tierValue = value;
        }

        public static PlayerTier from(int value) {
            return TIER_MAP.getOrDefault(value, UNRANKED);
        }
    }

    public record TierlistPlayer(UUID uuid, PlayerTier tier) {}

    private static String readInputStream(InputStream stream) throws Exception {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static UUID parseUUID(String id) {
        return UUID.fromString(id.replaceFirst(
            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
            "$1-$2-$3-$4-$5"
        ));
    }

    private static UUID fetchUUID(String username) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(MOJANG_API_URL + username).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    String response = readInputStream(inputStream);
                    String id = JsonParser.parseString(response).getAsJsonObject().get("id").getAsString();
                    return parseUUID(id);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static CompletableFuture<TierlistPlayer> requestFromAPI(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID uuid = fetchUUID(username);
                if (uuid == null) {
                    return new TierlistPlayer(UUID.randomUUID(), PlayerTier.UNRANKED);
                }

                String uuidString = uuid.toString().replace("-", "");
                HttpURLConnection connection = (HttpURLConnection) new URL(TIER_URL + uuidString).openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream()) {
                        JsonObject response = JsonParser.parseString(readInputStream(inputStream)).getAsJsonObject();
                        JsonObject vanilla = response.getAsJsonObject("vanilla");
                        
                        int tier = vanilla.get("tier").getAsInt();
                        int position = vanilla.get("pos").getAsInt();
                        int tierValue = position == 0 ? 12 - tier * 2 : 11 - tier * 2;
                        
                        return new TierlistPlayer(uuid, PlayerTier.from(tierValue));
                    }
                }
            } catch (Exception ignored) {}
            return new TierlistPlayer(UUID.randomUUID(), PlayerTier.UNRANKED);
        });
    }

    private TierUtils() {}
}
