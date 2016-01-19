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
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.ListView;

public class Controller implements Initializable {

    public Odtwarzacz odtwarzacz;
    public Playlista playlista;
    boolean isMousePressed=false;
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
    public Text SongNAME;
    @FXML
    private ListView<String> Lista;
    final ObservableList<String> listItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        odtwarzacz=new Odtwarzacz();
        playlista=new Playlista();

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
                przygotuj();
                odtwarzacz.Graj();
            }
        });

        StopBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                odtwarzacz.Stop();
                SliderX.setValue(0);
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
                    String filePath;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        playlista.dodajDoPlaylisty(filePath);
                        addToListview();
                    }
                }
                event.setDropCompleted(success);
                event.consume();
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
                         playlista.dodajDoPlaylisty(selectedFile.getAbsolutePath());
                         addToListview();
                     }
            }
        });

        PauseBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
            odtwarzacz.Pauza();
           }
        });

        DelBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                selectedItem = Lista.getSelectionModel().getSelectedIndex();
                listItems.remove(selectedItem);
                playlista.songs.remove(selectedItem);
            }
        });

        PreBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                if (selectedItem > 0) {
                    selectedItem = selectedItem - 1;
                    Lista.getSelectionModel().select(selectedItem);
                    przygotuj();
                    odtwarzacz.Graj();
                }
            }
        });

        NextBUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                if (selectedItem < (playlista.songs.size() - 1)) {
                    selectedItem = selectedItem + 1;
                    Lista.getSelectionModel().select(selectedItem);
                    przygotuj();
                    odtwarzacz.Graj();
                }
            }
        });

        SliderX.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                isMousePressed=true;
                odtwarzacz.Pauza();
            }
        });

        SliderX.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                isMousePressed=false;
                odtwarzacz.moment= SliderX.getValue();
                odtwarzacz.setMoment();
            }
        });

        SliderVol.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double vo=SliderVol.getValue();
                odtwarzacz.setGlosnosc(vo);
            }
        });

        SliderVol.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double vo=SliderVol.getValue();
                odtwarzacz.setGlosnosc(vo);
            }
        });

        Runnable slidUpdate = new Runnable() {
            public void run() {
                if(!isMousePressed) {
                        SliderX.setValue(odtwarzacz.Apdejt());
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(slidUpdate, 0, 1, TimeUnit.SECONDS);
    }

    void przygotuj() {
        selectedItem = Lista.getSelectionModel().getSelectedIndex();
        String path = playlista.songs.get(selectedItem).sciezka;
        odtwarzacz.dlugoscutworu = playlista.songs.get(selectedItem).dlugosc;
        odtwarzacz.sciezkaDoOdtworzenia = path;
        String wyswietlacz = playlista.songs.get(selectedItem).artist + " - " + playlista.songs.get(selectedItem).title;
        SongNAME.setText(wyswietlacz);
    }

    void addToListview() {
        int sajz=playlista.songs.size();
        listItems.add(playlista.songs.get(sajz-1).artist+" - "+playlista.songs.get(sajz-1).title);
    }
}