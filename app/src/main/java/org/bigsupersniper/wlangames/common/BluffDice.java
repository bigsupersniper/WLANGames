package org.bigsupersniper.wlangames.common;

import java.util.Date;
import java.util.Random;

/**
 * Created by linfeng on 2014/7/27.
 */
public class BluffDice {
    public static int[] shake(){
        int [] dices = new int[5];
        Random r = new Random(new Date().getTime());
        for (int i = 0 ; i < dices.length ; i++){
            dices[i] = r.nextInt(6);
        }

        return dices;
    }
}
