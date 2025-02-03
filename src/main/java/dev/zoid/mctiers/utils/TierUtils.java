package dev.zoid.mctiers.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TierUtils {
    private static final String SEARCH_PROFILE_URL = "https://mctiers.com/api/search_profile/";
    private static final int HTTP_OK = 200;

    public enum PlayerTier {
        LT5, HT5, LT4, HT4, LT3, HT3, LT2, HT2, LT1, HT1, UNRANKED;

        public static PlayerTier from(int tier) {
            if (tier <= 2) {
                return PlayerTier.valueOf("HT" + tier);
            } else {
                return PlayerTier.valueOf("LT" + tier);
            }
        }
    }

    public record TierlistPlayer(UUID uuid, PlayerTier tier) {}

    private static String readInputStream(InputStream stream) throws Exception {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static CompletableFuture<TierlistPlayer> requestFromAPI(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(SEARCH_PROFILE_URL + playerName).openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream()) {
                        JsonObject response = JsonParser.parseString(readInputStream(inputStream)).getAsJsonObject();

                        if (response.has("uuid")) {
                            UUID uuid = UUID.fromString(response.get("uuid").getAsString());
                            JsonObject rankings = response.getAsJsonObject("rankings");

                            if (rankings.has("vanilla")) {
                                int tier = rankings.getAsJsonObject("vanilla").get("tier").getAsInt();
                                return new TierlistPlayer(uuid, PlayerTier.from(tier));
                            } else {
                                return new TierlistPlayer(uuid, PlayerTier.UNRANKED);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            return new TierlistPlayer(UUID.randomUUID(), PlayerTier.UNRANKED);
        });
    }

    private TierUtils() {}
}
