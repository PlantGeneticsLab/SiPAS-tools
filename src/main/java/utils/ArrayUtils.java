package utils;

/**
 * Common sorting utils with Array
 *
 * @ author: yxh
 * @ created: 2021-08-05 : 9:38 PM
 */
public class ArrayUtils {

    /**
     * @param arr
     * @return the smallest value and smallest index
     */
    public static double[] Min(double[] arr,int number){
        double min1 = arr[0];
        int index1 = 0;
        for (int i = 1; i < arr.length; i++) {
            if(arr[i] < min1){
                min1 = arr[i];
                index1 = i;
            }
        }
        double[] minMin = {min1,index1};
        return minMin;
    }
}
