package Odtwarzacz;

import static org.junit.Assert.*;

/**
 * Created by Wojtek on 2016-01-22.
 */
public class OdtwarzaczTest {
    Odtwarzacz odtwarzacz1;

    public OdtwarzaczTest() { }
    @org.junit.Test

    public void setUp() {
        odtwarzacz1 = new Odtwarzacz();
        testObliczGlosnosc();
   }

    protected void tearDown() { }

    public void testObliczGlosnosc() {
        assertEquals(odtwarzacz1.obliczGlosnosc(0),0,0);
        assertEquals(odtwarzacz1.obliczGlosnosc(-1),0,0);
        assertEquals(odtwarzacz1.obliczGlosnosc(0.1),0.01,0.01);
        assertEquals(odtwarzacz1.obliczGlosnosc(0.5),0.25,0);
        assertEquals(odtwarzacz1.obliczGlosnosc(1),1,0);
        assertEquals(odtwarzacz1.obliczGlosnosc(1.1),1,0);
        assertEquals(odtwarzacz1.obliczGlosnosc(100),1,0);
    }
}

