import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private Clip bgmClip;

    // Untuk Sound Effect (Sekali main, ex: langkah, dadu)
    public void playSound(String filename) {
        try {
            File soundFile = new File("assets/" + filename);
            if (!soundFile.exists()) return; // Kalau ga ada, diam aja

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Audio SFX error: " + e.getMessage());
        }
    }

    // Untuk Background Music (Looping)
    public void playBGM(String filename) {
        try {
            stopBGM(); // Matikan lagu sebelumnya kalau ada
            File soundFile = new File("assets/" + filename);
            
            // --- DEBUG PRINT (Cek error di console bawah) ---
            System.out.println("Mencoba memutar BGM: " + soundFile.getAbsolutePath());
            if (!soundFile.exists()) {
                System.out.println("ERROR: File BGM tidak ditemukan di folder assets!");
                return;
            }
            // -----------------------------------------------

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioIn);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop selamanya
            bgmClip.start();
        } catch (Exception e) {
            System.out.println("BGM Error (Pastikan format WAV 16-bit): " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
}