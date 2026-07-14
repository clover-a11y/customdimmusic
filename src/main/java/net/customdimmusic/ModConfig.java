package net.customdimmusic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cấu hình đơn giản của mod, được lưu tại:
 *   <instance>/config/customdimmusic/settings.json
 *
 * Đây là "phần cài đặt" mà người dùng bật/tắt nhạc tùy chỉnh.
 */
public class ModConfig {

    public boolean enabled = false;      // Mặc định TẮT - người dùng phải tự bật trong màn hình cài đặt
    public float volume = 1.0f;          // 0.0 -> 1.0
    public boolean muteVanillaMusic = true; // Tắt tiếng nhạc gốc của Minecraft khi nhạc custom đang phát
    public boolean shuffle = true;       // Trộn ngẫu nhiên thứ tự các bài trong thư mục

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configFile() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("customdimmusic");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
        return dir.resolve("settings.json");
    }

    public static ModConfig load() {
        Path file = configFile();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                ModConfig cfg = GSON.fromJson(reader, ModConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (IOException e) {
                CustomDimMusicClient.LOGGER.warn("Không đọc được settings.json, dùng cấu hình mặc định.", e);
            }
        }
        ModConfig fresh = new ModConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        Path file = configFile();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            CustomDimMusicClient.LOGGER.warn("Không lưu được settings.json", e);
        }
    }
}
