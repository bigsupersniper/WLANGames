package org.bigsupersniper.wlangames.common;

import org.bigsupersniper.wlangames.R;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by linfeng on 2014/7/27.
 */
public class BluffDice {

    private final static int[] res = new int[]{R.drawable.dice_1, R.drawable.dice_2,
            R.drawable.dice_3, R.drawable.dice_4, R.drawable.dice_5,
            R.drawable.dice_6};

    private final static Map<Integer, Integer> resMaps = new HashMap<Integer, Integer>();

    static {
        resMaps.put(R.drawable.dice_1, 1);
        resMaps.put(R.drawable.dice_2, 2);
        resMaps.put(R.drawable.dice_3, 3);
        resMaps.put(R.drawable.dice_4, 4);
        resMaps.put(R.drawable.dice_5, 5);
        resMaps.put(R.drawable.dice_6, 6);
    }

    public static int getResId(int index) {
        return res[index];
    }

    public static String valueOf(int resId) {
        return resMaps.get(resId).toString();
    }

    public static int[] shake() {
        int[] dices = new int[5];
        Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < dices.length; i++) {
            int mod = r.nextInt(r.hashCode()) % nextInt(r , 1000);

            while (true){
                if (mod >= 0 && mod < 6) {
                    break;
                }else {
                    mod = r.nextInt(r.hashCode()) % nextInt(r , 1000);
                }
            }

            dices[i] = mod;
        }
        //排序
        Arrays.sort(dices);

        return dices;
    }

    private static int nextInt(Random r , int n){
        int next = r.nextInt(n);
        while (true) {
            if (next > 1) {
                break;
            }else{
                next = r.nextInt(n);
            }
        }
        return next;
    }

    public static int[] toRes(int[] ids) {
        int[] result = new int[ids.length];

        for (int i = 0; i < ids.length; i++) {
            result[i] = res[ids[i]];
        }

        return result;
    }

}
