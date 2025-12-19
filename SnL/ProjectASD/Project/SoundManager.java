import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private Clip bgmClip;

    // [SOLUSI AUDIO]: Menggunakan Alamat Lengkap (Absolute Path)
    // Karena Java kadang bingung cari file, kita kasih alamat lengkap folder assets-nya.
    // PERHATIKAN: Path ini harus diganti kalau pindah laptop!
    private static final String BASE_PATH = "C:/KULIAH/ASD/FP_ASD/FP_ASD_Snaknladder_Maze/SnL/ProjectASD/Project/assets/";

    public void playSound(String filename) {
        loadAndPlay(filename, false); // False = putar sekali aja (SFX)
    }

    public void playBGM(String filename) {
        stopBGM(); // Matiin lagu lama dulu
        loadAndPlay(filename, true); // True = putar terus-terusan (Looping)
    }

    private void loadAndPlay(String filename, boolean isLooping) {
        try {
            String fullPath = BASE_PATH + filename;
            File file = new File(fullPath);

            if (!file.exists()) return; // Kalau file gak ada, diem aja biar gak error

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            if (isLooping) {
                bgmClip = clip;
                // Kecilin volume dikit biar gak berisik
                try {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(-10.0f); 
                } catch (Exception e) {}
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        } catch (Exception e) {
            // Error ditangkap disini biar program gak crash
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop(); bgmClip.close();
        }
    }
}