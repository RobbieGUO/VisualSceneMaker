/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.vsm.xtension.stickman;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Robbie
 */
public class VoiceReader {

    private static String file = "audio/";
    private static MediaPlayer mediaPlayer;
    private static Boolean playingMarking = false;

    public static void readFile(String s) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        file = file + s;
        play(file);
    }

    private static void play(String str) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        
        Media hit = new Media(new File(str).toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
        playingMarking = true;
        mediaPlayer.play();

    }

    public static void Stop() {
        if (playingMarking) {
            mediaPlayer.stop();
            playingMarking = false;
        }
    }

}
