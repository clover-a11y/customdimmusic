package net.customdimmusic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDimMusicClient implements ClientModInitializer {

    public static final String MOD_ID = "customdimmusic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig CONFIG;
    public static MusicManager MUSIC;

    private static KeyBinding openConfigKey;

    // Lưu giá trị âm lượng nhạc gốc để khôi phục lại khi tắt mod / rời world
    private Double savedVanillaMusicVolume = null;

    @Override
    public void onInitializeClient() {
        CONFIG = ModConfig.load();
        MUSIC = new MusicManager();
        MUSIC.setVolume(CONFIG.volume);

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.customdimmusic.open_config",
                GLFW.GLFW_KEY_O,
                "key.category.customdimmusic"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        LOGGER.info("Custom Dimension Music đã khởi động. Thư mục nhạc: " + MUSIC.getRootFolder());
    }

    private void onTick(MinecraftClient client) {
        // Mở màn hình cài đặt bằng phím tắt (mặc định phím O, có thể đổi trong Controls)
        while (openConfigKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new ConfigScreen(null));
            }
        }

        if (client.player == null || client.world == null) {
            handleDisabledState(client);
            return;
        }

        MusicManager.Dimension dim = mapDimension(client.world.getRegistryKey());
        MUSIC.updateForDimension(dim, CONFIG);

        applyVanillaMusicMute(client);
    }

    private void handleDisabledState(MinecraftClient client) {
        MUSIC.stop();
        restoreVanillaMusicVolume(client);
    }

    private MusicManager.Dimension mapDimension(RegistryKey<World> key) {
        if (key == World.OVERWORLD) return MusicManager.Dimension.OVERWORLD;
        if (key == World.NETHER) return MusicManager.Dimension.NETHER;
        if (key == World.END) return MusicManager.Dimension.END;
        return null; // chiều không gian modded khác - không phát nhạc custom
    }

    /**
     * Khi nhạc custom đang phát và người dùng bật "muteVanillaMusic",
     * hạ âm lượng nhạc nền gốc của Minecraft xuống 0 để tránh chồng tiếng.
     * Lưu ý: tên method getSoundVolumeOption ổn định qua nhiều bản, nhưng nếu
     * Minecraft 26.x đổi tên, chỉ cần sửa lại đúng 2 method bên dưới.
     */
    private void applyVanillaMusicMute(MinecraftClient client) {
        try {
            if (CONFIG.enabled && CONFIG.muteVanillaMusic && MUSIC.isPlaying()) {
                if (savedVanillaMusicVolume == null) {
                    savedVanillaMusicVolume = client.options.getSoundVolumeOption(SoundCategory.MUSIC).getValue();
                }
                client.options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(0.0);
            } else {
                restoreVanillaMusicVolume(client);
            }
        } catch (Throwable t) {
            // Nếu API âm lượng của bản 26.x khác tên, không làm crash toàn bộ mod.
            LOGGER.warn("Không thể chỉnh âm lượng nhạc gốc (API có thể đã đổi ở bản Minecraft này).", t);
        }
    }

    private void restoreVanillaMusicVolume(MinecraftClient client) {
        try {
            if (savedVanillaMusicVolume != null) {
                client.options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(savedVanillaMusicVolume);
                savedVanillaMusicVolume = null;
            }
        } catch (Throwable ignored) {
        }
    }
}
