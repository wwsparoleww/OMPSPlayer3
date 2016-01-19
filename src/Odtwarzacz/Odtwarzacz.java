package Odtwarzacz;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.*;
import java.io.*;

/**
 * Created by Wojtek on 2016-01-19.
 */
public class Odtwarzacz {

    String sciezkaDoOdtworzenia;
    FileInputStream FIS;
    BufferedInputStream BIS;
    public Player player;
    public long pozostalo;
    public double moment;
    public long dlugoscutworu;
    boolean isPaused=false;

    void Graj() {
        try {
            if (player != null) {
                player.close();
            }
            FIS = new FileInputStream(sciezkaDoOdtworzenia);
                if (isPaused){
                    FIS.skip(dlugoscutworu-pozostalo);
                }
            BIS = new BufferedInputStream(FIS);
            player = new Player(BIS);
                if(!isPaused){
                    dlugoscutworu = FIS.available();
                }
        }
        catch (FileNotFoundException | JavaLayerException ex) {
        }
        catch (IOException ex) {
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    player.play();
                } catch (JavaLayerException ex) {
                }
            }

        }.start();
        isPaused = false;
    }

    void Pauza(){
               try {
                   pozostalo=FIS.available();
               }
               catch (IOException e) {
                   e.printStackTrace();
               }
               isPaused=true;
               player.close();
    }

    void Stop() {
         if (player != null) {
             player.close();
             pozostalo = dlugoscutworu;
         }
    }


    double Apdejt()
    {
        if (!isPaused){
            if (player != null) {
                    try {
                        pozostalo = FIS.available();
                        moment = ((double) (dlugoscutworu - pozostalo)) * 100 / dlugoscutworu;
                    } catch (IOException ex) {
                    }
            }
        }
        return moment;
    }

    void setMoment() {
        pozostalo = Math.round(dlugoscutworu - dlugoscutworu * moment / 100);
        if (player != null) {
            player.close();
            Graj();
        }
    }

    void setGlosnosc(double vo) {
        float vol=(float)(vo*vo);
        try {
            Mixer.Info[] infos = AudioSystem.getMixerInfo();
            for (Mixer.Info info: infos)
            {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(Port.Info.SPEAKER))
                {
                    Port port = (Port)mixer.getLine(Port.Info.SPEAKER);
                    port.open();
                    if (port.isControlSupported(FloatControl.Type.VOLUME))
                    {
                        FloatControl volume =  (FloatControl)port.getControl(FloatControl.Type.VOLUME);
                        volume.setValue(vol);
                    }
                    port.close();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Erro\n"+e);
        }
    }

}