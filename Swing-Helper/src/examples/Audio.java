package examples;

import clara.Canvas;

public class Audio {
    public static void main(String[] args) {
        Canvas c = new Canvas(500, 500);

        // Set custom audio path for all new audio files loaded. Defaults to /
        c.getAudioManager().setAudioPath("src/assets/audio");

        // Load game.wav audio file
        c.getAudioManager().load("bg", "game.wav");

        while (true) {
            // Get audio file by key and use start method to play audio 
            c.getAudioManager().get("bg").start();
            c.repaint();
            c.addDelay((int) (1000d / 60d));
        }
    }
}
