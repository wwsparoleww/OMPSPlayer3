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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
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
    int selectedItem;

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
        selectedItem = Lista.getSelectionModel().getSelectedIndex();
        listItems.remove(selectedItem);
        songs.remove(selectedItem);
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
                    Graj();
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

        Lista.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        Lista.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        getMeta(filePath);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

        OpenBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Open();
            }
        });

        PauseBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
            Pauza();
           }
        });

        DelBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                deleteAction(arg0);
            }
        });

        PreBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Previous();
            }
        });

        NextBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                Next();
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
                getMoment();
                Wznow();
            }
        });

        SliderVol.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setGlosnosc();
            }
        });

        SliderVol.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setGlosnosc();
            }
        });

        Runnable slidUpdate = new Runnable() {
            public void run() {
                Apdejt();
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(slidUpdate, 0, 1, TimeUnit.SECONDS);
    }

    void Open() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            sciezkautworu = selectedFile.getAbsolutePath();
            getMeta(sciezkautworu);
        }
    }

    void Apdejt()
    {
        if (isPaused != true){
            if (player != null) {
                if (isMousePressed != true) {
                    try {
                        pozostalo = FIS.available();
                        moment = ((double) (dlugoscutworu - pozostalo)) * 100 / dlugoscutworu;
                        SliderX.setValue(moment);
                    } catch (IOException ex) {

                    }
                }
            }
        }
    }

    void getMoment(){
        isMousePressed=false;
        moment= SliderX.getValue();
        pozostalo=Math.round(dlugoscutworu-dlugoscutworu*moment/100);
        if (player != null) {
            player.close();
        }
    }

    void Stop() {
        if (player != null) {
            player.close();
            pozostalo = dlugoscutworu;
            SliderX.setValue(0);
            //   dlugoscutworu = 0;
        }
    }

    void Graj() {
        selectedItem = Lista.getSelectionModel().getSelectedIndex();
        sciezkaToGo = songs.get(selectedItem).sciezka;
        SongNAME.setText(songs.get(selectedItem).artist + "-" + songs.get(selectedItem).title);
        try {
            if (player != null) {
                player.close();
            }
            FIS = new FileInputStream(sciezkaToGo);
            BIS = new BufferedInputStream(FIS);
            player = new Player(BIS);

            dlugoscutworu = FIS.available();
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
                    setGlosnosc();
                } catch (JavaLayerException ex) {
                }
            }

        }.start();
        isPaused = false;
    }

    void Pauza(){
        try {
            pozostalo=FIS.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isPaused=true;
        player.close();
    }

    void Wznow() {
        try {
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
        isPaused=false;
    }

    void Previous() {
        if (selectedItem > 0) {
            selectedItem = selectedItem - 1;
            Lista.getSelectionModel().select(selectedItem);
            Graj();
        }
    }

    void Next() {
        if (selectedItem < (songs.size() - 1)) {
            selectedItem = selectedItem + 1;
            Lista.getSelectionModel().select(selectedItem);
            Graj();
        }
    }


    void setGlosnosc()
    {
        double vo=SliderVol.getValue();
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

    void getMeta(String path)
    {
        try {
            InputStream input = new FileInputStream(new File(path));
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);

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