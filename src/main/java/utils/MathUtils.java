package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @ author: yxh
 * @ created: 2021-07-18 : 8:28 PM
 */
public class MathUtils {

    /**
     * @param array
     * get average or mean from an array
     * @return
     */
    public static double getMean(double[] array) {
        double average = 0;
        for (int i = 0; i < array.length; i++) {
            average += array[i];
        }
        average = average / array.length;
        return average;
    }

    public static double getMean(List<Double> array) {
        double average = 0;
        for (int i = 0; i < array.size(); i++) {
            average += array.get(i);
        }
        average = average / array.size();
        return average;
    }

    /**
     * @param array
     * get standard variation or sd from an array
     * @return
     */
    public static double getSd(double[] array) {
        double sd = 0;
        double average = getMean(array);
        for (int i = 0; i < array.length; i++) {
            sd += (array[i] - average) * (array[i] - average);
        }
        sd = sd / (array.length - 1);
        sd = Math.sqrt(sd);
        return sd;
    }

    /**
     * @param array
     * get variance from an array
     * @return
     */
    public static double getVar(double[] array) {
        double var = 0;
        double average = getMean(array);
        for (int i = 0; i < array.length; i++) {
            var += (array[i] - average) * (array[i] - average);
        }
        var = var / (array.length - 1);
        return var;
    }

    /**
     * @param array
     * get zscores from an array
     * @return
     */
    public static double[] getZscore(double[] array) {
        double sd = getSd(array);
        double average = getMean(array);
        double[] zscore = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            zscore[i] = (array[i] - average) / sd;
        }
        return zscore;
    }

    /**
     * @param array
     * get median form an array
     * @return
     */

    public static double getMedian(double[] array) {
        double median = 0;
        Arrays.sort(array);
        if (array.length % 2 == 1) {
            median = array[array.length / 2];
        } else {
            median = (array[array.length / 2] + array[array.length / 2 - 1]) / 2;
        }
        return median;
    }

    public static double getMedian(List<Double> arrays) {
        double[] array = new double[arrays.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = arrays.get(i).doubleValue();  // java 1.4 style
            // or:
//            target[i] = doubles.get(i);                // java 1.5+ style (outboxing)
        }
        return getMedian(array);
    }

    /**
     * @param array
     * get range from an array
     * @return
     */
    public static double getRange(double[] array){
        double range = 0;
        Arrays.sort(array);
        range = array[array.length-1]-array[0];
        return range;
    }

    /**
     * @param array
     * @return
     */
    public static double getMax(double[] array){
        double max = 0;
        Arrays.sort(array);
        max = array[array.length-1]-array[0];
        return max;
    }

    /**
     * @param array
     * @return
     */
    public static double getMin(double[] array){
        double min = 0;
        Arrays.sort(array);
        min = array[0];
        return min;
    }

    public static double getMode(double[] array){
        double mode = 0;
        Arrays.sort(array);
        HashMap<Double,Integer> num = new HashMap<>();
        for (int i = 0; i < array.length;) {
            int count = 1;
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] == array[j]) {
                    count++;
                } else {
                    break;
                }
            }
            num.put(array[i],count);
        }
        return mode;
    }

}
