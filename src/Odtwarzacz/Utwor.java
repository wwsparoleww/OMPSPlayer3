package Odtwarzacz;

/**
 * Created by Wojtek on 2016-01-17.
 */
public class Utwor{

    String artist;
    String title;
    String sciezka;
    long dlugosc;

    void utworzUtwor(String n,String k,String l, long dlug){
        artist=n;
        title=k;
        sciezka=l;
        dlugosc=dlug;
    }
}
