/* Calc - holder for generic calculation methods */

package com.plantandfood.aspirelite;

import java.util.ArrayList;

class Calc {

    public static float mean(ArrayList<Float> results) {
        /* Calculate the mean of the given results */
        float total = 0;
        for (int i = 0; i < results.size(); i++) {
            total += results.get(i);
        }
        return total / results.size();
    }

    public static float stddev(float mean, ArrayList<Float> results) {
        /* Calculate the standard deviation of the given results */
        float sum = 0;
        for (int i = 0; i < results.size(); i++) {
            sum += Math.pow(mean - results.get(i), 2f);
        }
        return (float) Math.sqrt(sum / results.size());
    }
}
