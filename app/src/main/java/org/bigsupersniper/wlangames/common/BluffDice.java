package org.bigsupersniper.wlangames.common;

import org.bigsupersniper.wlangames.R;

import java.util.Arrays;

/**
 * Created by linfeng on 2014/7/27.
 */
public class BluffDice {

    private final static int[] res = new int[]{0, R.drawable.dice_1, R.drawable.dice_2,
            R.drawable.dice_3, R.drawable.dice_4, R.drawable.dice_5,
            R.drawable.dice_6};

    public static int getResId(int index) {
        return res[index];
    }

    public static int[] shake() {
        int[] dices = new int[5];
        for (int i = 0; i < dices.length; i++) {
            dices[i] = nextInt(1, 6);
        }
        //排序
        Arrays.sort(dices);

        return dices;
    }

    private static int nextInt(int min, int max) {
        int next = (int) (Math.random() * 10);
        if (next < min || next > max) {
            next = nextInt(min, max);
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
