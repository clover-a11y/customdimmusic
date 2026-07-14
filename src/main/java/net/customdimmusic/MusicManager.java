package net.customdimmusic;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Chịu trách nhiệm:
 *  - Tạo 3 thư mục overworld/nether/end trong config/customdimmusic/
 *  - Quét file .mp3 trong từng thư mục
 *  - Phát nhạc lặp lại (playlist) ứng với chiều không gian hiện tại
 *
 * Thư mục sẽ nằm ở:  <instance của bạn>/config/customdimmusic/overworld
 *                     <instance của bạn>/config/customdimmusic/nether
 *                     <instance của bạn>/config/customdimmusic/end
 */
public class MusicManager {

    public enum Dimension {
        OVERWORLD("overworld"),
        NETHER("nether"),
        END("end");

        public final String folderName;

        Dimension(String folderName) {
            this.folderName = folderName;
        }
    }

    private Thread playbackThread;
    private volatile AdvancedPlayer currentPlayer;
    private volatile boolean stopRequested = false;
    private Dimension currentlyPlayingDimension = null;

    private final VolumeAudioDevice audioDevice = new VolumeAudioDevice();

    public MusicManager() {
        // Tạo sẵn 3 thư mục để người dùng biết bỏ mp3 vào đâu
        for (Dimension d : Dimension.values()) {
            getFolder(d);
        }
    }

    public Path getFolder(Dimension d) {
        Path dir = FabricLoader.getInstance().getConfigDir()
                .resolve("customdimmusic")
                .resolve(d.folderName);
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
        return dir;
    }

    public Path getRootFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve("customdimmusic");
    }

    private List<Path> listMp3(Dimension d) {
        Path dir = getFolder(d);
        List<Path> result = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().toLowerCase().endsWith(".mp3"))
                 .forEach(result::add);
        } catch (IOException e) {
            CustomDimMusicClient.LOGGER.warn("Không đọc được thư mục nhạc: " + dir, e);
        }
        return result;
    }

    public void setVolume(float volume) {
        audioDevice.setVolume(volume);
    }

    /** Gọi mỗi tick từ client để đảm bảo đúng bài đang phát đúng dimension. */
    public synchronized void updateForDimension(Dimension target, ModConfig config) {
        if (!config.enabled || target == null) {
            stop();
            currentlyPlayingDimension = null;
            return;
        }

        if (target == currentlyPlayingDimension && playbackThread != null && playbackThread.isAlive()) {
            return; // Đang phát đúng nhạc rồi, không cần làm gì
        }

        currentlyPlayingDimension = target;
        startPlaylist(target, config);
    }

    public synchronized void stop() {
        stopRequested = true;
        if (currentPlayer != null) {
            currentPlayer.close();
            currentPlayer = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }
    }

    private void startPlaylist(Dimension dimension, ModConfig config) {
        stop();
        stopRequested = false;

        List<Path> tracks = listMp3(dimension);
        if (tracks.isEmpty()) {
            CustomDimMusicClient.LOGGER.info("Không có file .mp3 nào trong thư mục " + dimension.folderName);
            return;
        }
        if (config.shuffle) {
            Collections.shuffle(tracks);
        }

        playbackThread = new Thread(() -> playLoop(tracks, dimension), "CustomDimMusic-Playback");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    private void playLoop(List<Path> tracks, Dimension dimension) {
        int index = 0;
        while (!stopRequested) {
            Path track = tracks.get(index % tracks.size());
            index++;
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(track.toFile()))) {
                AdvancedPlayer player = new AdvancedPlayer(in, audioDevice);
                currentPlayer = player;

                final boolean[] naturallyFinished = {false};
                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        naturallyFinished[0] = true;
                    }
                });

                player.play(); // chặn (blocking) cho tới khi bài hát kết thúc hoặc bị close()

                if (stopRequested) {
                    return;
                }
            } catch (Exception e) {
                CustomDimMusicClient.LOGGER.warn("Lỗi khi phát file nhạc: " + track, e);
                // tiếp tục sang bài kế tiếp thay vì dừng hẳn
            }
        }
    }

    public boolean isPlaying() {
        return playbackThread != null && playbackThread.isAlive();
    }

    public Dimension getCurrentlyPlayingDimension() {
        return currentlyPlayingDimension;
    }
}
