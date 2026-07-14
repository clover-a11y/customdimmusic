package net.customdimmusic;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Màn hình cài đặt trong game của mod ("mở mod ra trong phần cài đặt").
 * Mở bằng phím tắt mặc định O (đổi được trong Controls > Custom Dimension Music).
 */
public class ConfigScreen extends Screen {

    private final Screen parent;
    private ButtonWidget enabledButton;
    private ButtonWidget muteButton;
    private ButtonWidget shuffleButton;

    protected ConfigScreen(Screen parent) {
        super(Text.translatable("customdimmusic.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig cfg = CustomDimMusicClient.CONFIG;
        int centerX = this.width / 2;
        int y = this.height / 2 - 60;

        enabledButton = ButtonWidget.builder(
                enabledText(cfg.enabled),
                b -> {
                    cfg.enabled = !cfg.enabled;
                    cfg.save();
                    b.setMessage(enabledText(cfg.enabled));
                }
        ).dimensions(centerX - 100, y, 200, 20).build();
        addDrawableChild(enabledButton);

        y += 24;
        addDrawableChild(new VolumeSlider(centerX - 100, y, 200, 20, cfg.volume));

        y += 24;
        muteButton = ButtonWidget.builder(
                muteText(cfg.muteVanillaMusic),
                b -> {
                    cfg.muteVanillaMusic = !cfg.muteVanillaMusic;
                    cfg.save();
                    b.setMessage(muteText(cfg.muteVanillaMusic));
                }
        ).dimensions(centerX - 100, y, 200, 20).build();
        addDrawableChild(muteButton);

        y += 24;
        shuffleButton = ButtonWidget.builder(
                shuffleText(cfg.shuffle),
                b -> {
                    cfg.shuffle = !cfg.shuffle;
                    cfg.save();
                    b.setMessage(shuffleText(cfg.shuffle));
                }
        ).dimensions(centerX - 100, y, 200, 20).build();
        addDrawableChild(shuffleButton);

        y += 24;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("customdimmusic.config.open_folder"),
                b -> openMusicFolder()
        ).dimensions(centerX - 100, y, 200, 20).build());

        y += 24;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("customdimmusic.config.done"),
                b -> close()
        ).dimensions(centerX - 100, y, 200, 20).build());
    }

    private Text enabledText(boolean on) {
        return Text.translatable("customdimmusic.config.enabled")
                .append(": ")
                .append(on ? Text.translatable("options.on") : Text.translatable("options.off"));
    }

    private Text muteText(boolean on) {
        return Text.translatable("customdimmusic.config.replace_vanilla")
                .append(": ")
                .append(on ? Text.translatable("options.on") : Text.translatable("options.off"));
    }

    private Text shuffleText(boolean on) {
        return Text.literal("Shuffle: " + (on ? "ON" : "OFF"));
    }

    private void openMusicFolder() {
        try {
            Path folder = CustomDimMusicClient.MUSIC.getRootFolder();
            Util.getOperatingSystem().open(folder.toUri());
        } catch (Exception e) {
            CustomDimMusicClient.LOGGER.warn("Không mở được thư mục nhạc bằng trình quản lý file hệ thống.", e);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    /** Thanh trượt chỉnh âm lượng nhạc custom, từ 0% -> 100%. */
    private static class VolumeSlider extends SliderWidget {
        VolumeSlider(int x, int y, int width, int height, float initialVolume) {
            super(x, y, width, height, volumeText(initialVolume), initialVolume);
        }

        private static Text volumeText(double v) {
            return Text.translatable("customdimmusic.config.volume")
                    .append(": " + Math.round(v * 100) + "%");
        }

        @Override
        protected void updateMessage() {
            this.setMessage(volumeText(this.value));
        }

        @Override
        protected void applyValue() {
            float v = (float) this.value;
            CustomDimMusicClient.CONFIG.volume = v;
            CustomDimMusicClient.CONFIG.save();
            CustomDimMusicClient.MUSIC.setVolume(v);
        }
    }
}
