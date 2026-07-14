package net.customdimmusic;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;

import javax.sound.sampled.FloatControl;

/**
 * JLayer mặc định không cho chỉnh âm lượng khi phát MP3.
 * Class này mở rộng JavaSoundAudioDevice để lấy MASTER_GAIN control của
 * SourceDataLine bên dưới và chỉnh âm lượng (0.0 -> 1.0) theo config của mod.
 */
public class VolumeAudioDevice extends JavaSoundAudioDevice {

    private volatile float volume = 1.0f;

    public void setVolume(float v) {
        this.volume = Math.max(0f, Math.min(1f, v));
        applyVolume();
    }

    @Override
    protected void createSource() throws JavaLayerException {
        super.createSource();
        applyVolume();
    }

    private void applyVolume() {
        try {
            if (source != null && source.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum();
                float max = gain.getMaximum();
                float db;
                if (volume <= 0.0001f) {
                    db = min;
                } else {
                    db = (float) (20.0 * Math.log10(volume));
                    db = Math.max(min, Math.min(max, db));
                }
                gain.setValue(db);
            }
        } catch (Exception ignored) {
            // Một số hệ thống âm thanh không hỗ trợ MASTER_GAIN, bỏ qua an toàn.
        }
    }
}
