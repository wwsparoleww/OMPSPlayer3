package Odtwarzacz;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.ListView;



public class Controller implements Initializable {

    class Utwor{

        String artist;//data member(also instance variable)
        String title;
        String sciezka;

        void dodajUtwor(String n,String k,String l){  //method
            artist=n;
            title=k;
            sciezka=l;
        }
    }

    FileInputStream FIS;
    BufferedInputStream BIS;
    List<Utwor> songs=new ArrayList<>();
    public Player player;
    public String sciezkautworu;
    public String sciezkaToGo;
    public long pozostalo;
    public long dlugoscutworu;
    public double moment;
    boolean isMousePressed=false;
    boolean isPaused=false;
    @FXML
    private Button PlayBUT;
    @FXML
    private Button StopBUT;
    @FXML
    private Button OpenBUT;
    @FXML
    private Button PauseBUT;
    @FXML
    private Button DelBUT;
    @FXML
    private Slider SliderX;
    @FXML
    private Button NextBUT;
    @FXML
    private Button PreBUT;
    @FXML
    private Slider SliderVol;
    @FXML
    private Text SongNAME;
    @FXML
    private ListView<String> Lista;
    final ObservableList<String> listItems = FXCollections.observableArrayList();

    @FXML
    private void addAction(Utwor s){
        songs.add(s);
    }

    @FXML
    private void deleteAction(ActionEvent action){
        int selectedItem = Lista.getSelectionModel().getSelectedIndex();
        listItems.remove(selectedItem);
        songs.remove(selectedItem);
     //   SongNAME.setText("proba");
    }
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        Lista.setItems(listItems);
        DelBUT.setDisable(true);

        Lista.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(Lista.isFocused()){
                    DelBUT.setDisable(false);
                }
            }
        });


        PlayBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                if(isPaused==false) {
                    int selectedItem = Lista.getSelectionModel().getSelectedIndex();
                    sciezkaToGo = songs.get(selectedItem).sciezka;
                    SongNAME.setText(songs.get(selectedItem).artist+"-"+songs.get(selectedItem).title);
                    try {
                        if (player != null) {
                            player.close();
                        }
                        FIS = new FileInputStream(sciezkaToGo);
                        BIS = new BufferedInputStream(FIS);
                        player = new Player(BIS);

                        dlugoscutworu = FIS.available();
                    } catch (FileNotFoundException | JavaLayerException ex) {
                    } catch (IOException ex) {
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                player.play();
                                isPaused=false;
                                setGlosnosc((float) 0.8);
                            } catch (JavaLayerException ex) {
                            }
                        }
                    }.start();
                }

                if(isPaused==true) {
                    Wznow();
                }
            }
        });

        StopBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Stop();
            }
        });

        OpenBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    sciezkautworu=selectedFile.getAbsolutePath();

                    try
                    {
                        if (player != null) {
                            player.close();
                        }
                        FIS=new FileInputStream(sciezkautworu);
                        BIS=new BufferedInputStream(FIS);
                        player=new Player(BIS);

                        dlugoscutworu=FIS.available();
                        sciezkautworu=sciezkautworu + "";
                        SongNAME.setText("Choose track");
                        getMeta(sciezkautworu);
                    }
                    catch(FileNotFoundException | JavaLayerException ex) {
                    }
                    catch(IOException ex) {
                    }
                }
            }
        });

        PauseBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                if(player!=null)
                {
                    try
                    {
                        isPaused=true;
                        pozostalo=FIS.available();
                        player.close();
                    }
                    catch(IOException ex)
                    {

                    }
                }
            }
        });

        DelBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                deleteAction(arg0);
            }
        });

        NextBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                if(player!=null) {
                }
            }
        });

        SliderX.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                isMousePressed=true;
            }
        });

        SliderX.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                isMousePressed=false;
                moment= SliderX.getValue();
                pozostalo=Math.round(dlugoscutworu-dlugoscutworu*moment/100);
                if (player != null) {
                    player.close();
                }
                Wznow();
            }
        });

        SliderVol.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double ctrl=SliderVol.getValue();
                float ctrlctrl=(float)(ctrl*ctrl);
                setGlosnosc(ctrlctrl);
            }
        });

        SliderVol.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double ctrl=SliderVol.getValue();
                float ctrlctrl=(float)(ctrl*ctrl);
                setGlosnosc(ctrlctrl);
            }
        });

        Runnable slidUpdate = new Runnable() {
            public void run() {

                if(player!=null)
                {
                    if(isMousePressed!=true) {

                        try {
                            pozostalo = FIS.available();
                            moment = ((double) (dlugoscutworu - pozostalo)) * 100 / dlugoscutworu;
                            SliderX.setValue(moment);
                        } catch (IOException ex) {

                        }
                    }
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(slidUpdate, 0, 1, TimeUnit.SECONDS);

    }

    void Stop() {
        if (player != null) {
            player.close();
            pozostalo = dlugoscutworu;
            SliderX.setValue(0);
            //   dlugoscutworu = 0;
        }
    }

    void Wznow() {
        try {
            isPaused=false;
            FIS=new FileInputStream(sciezkaToGo);
            FIS.skip(dlugoscutworu-pozostalo);
            BIS=new BufferedInputStream(FIS);
            player=new Player(BIS);
        }

        catch(FileNotFoundException | JavaLayerException ex) {
        }
        catch(IOException ex) {
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    player.play();
                }
                catch(JavaLayerException ex) {
                }
            }
        }.start();
    }

    void setGlosnosc(float vol)
    {
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

    void getMeta(String path)
    {
        try {

            InputStream input = new FileInputStream(new File(path));
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            //         fis.close();

            // List all metadata
            String[] metadataNames = metadata.names();

            for(String name : metadataNames){
                System.out.println(name + ": " + metadata.get(name));
            }

            // Retrieve the necessary info from metadata
            // Names - title, xmpDM:artist etc. - mentioned below may differ based
            System.out.println("----------------------------------------------");
            System.out.println("Title: " + metadata.get("title"));
            System.out.println("Artists: " + metadata.get("xmpDM:artist"));
            System.out.println("Composer : "+metadata.get("xmpDM:composer"));
            System.out.println("Genre : "+metadata.get("xmpDM:genre"));
            System.out.println("Album : "+metadata.get("xmpDM:album"));

            //  addAction(metadata.get("title"));
            Utwor s1=new Utwor();
            s1.dodajUtwor(metadata.get("xmpDM:artist"),metadata.get("title"),path);
            addAction(s1);
            listItems.add(metadata.get("title"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
    }


}