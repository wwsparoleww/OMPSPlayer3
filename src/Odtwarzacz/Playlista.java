package Odtwarzacz;


import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wojtek on 2016-01-18.
 */
public class Playlista {
    List<Utwor> songs=new ArrayList<>();

    void dodajDoPlaylisty(String path){
        {
            try {
                InputStream input = new FileInputStream(new File(path));
                ContentHandler handler = new DefaultHandler();
                long dlugosc=input.available();
                Metadata metadata = new Metadata();
                Parser parser = new Mp3Parser();
                ParseContext parseCtx = new ParseContext();
                parser.parse(input, handler, metadata, parseCtx);
                Utwor s=new Utwor();
                s.utworzUtwor(metadata.get("xmpDM:artist"),metadata.get("title"),path,dlugosc);
                songs.add(s);

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
}
