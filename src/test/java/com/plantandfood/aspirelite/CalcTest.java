package com.plantandfood.aspirelite;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CalcTest {

    @Test
    public void TestMean() throws Exception {
        /* Check that the mean calculation works */

        /* Create the array list */
        ArrayList<Float> array = new ArrayList<>();

        /* Test */
        array.add(0f);
        assertEquals(0, Calc.mean(array), 0);
        array.add(2f);
        assertEquals(1, Calc.mean(array), 0);
        array.add(4f);
        assertEquals(2, Calc.mean(array), 0);
    }

    @Test
    public void TestStddev() throws Exception {
        /* Check that the standard deviation calculation works */

        /* Create the array list */
        ArrayList<Float> array = new ArrayList<>();

        /* Test */
        array.add(0f);
        assertEquals(0, Calc.stddev(Calc.mean(array), array), 0);
        array.add(0f);
        assertEquals(0, Calc.stddev(Calc.mean(array), array), 0);
        array.add(1f);
        assertEquals(.4714, Calc.stddev(Calc.mean(array), array), .0001);
        array.add(-1f);
        assertEquals(.7071, Calc.stddev(Calc.mean(array), array), .0001);
        array.add(-3f);
        assertEquals(1.3565, Calc.stddev(Calc.mean(array), array), .0001);
    }

}
