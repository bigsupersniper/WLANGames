package org.bigsupersniper.wlangames.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by sniper on 2014/7/30.
 */
public class CPoker {

    private final static String[] suits = { "spade" , "heart" , "club" , "diamond"  };
    private final static String[] numerals = { "1" , "2" , "3", "4", "5", "6", "7", "8", "9", "10" , "11", "12" , "13"};

    public static String[] shuffle(){
        List<String> list = new ArrayList<String>();
        boolean[][] lights = new boolean[13][4];
        Random random = new Random(new Date().getTime());
        int i = 0;
        while (i < 52) {
            int numeral = random.nextInt(13);
            int suit = random.nextInt(4);

            if (!lights[numeral][suit]) {
                lights[numeral][suit] = true;
                list.add(suits[suit] + "_" + numerals[numeral]);
                i++;
            }

        }

        return list.toArray(new String[0]);
    }

    public static String[] deal(String[] cards , int n){
        if (n > 3) return null;

        List<String> list = new ArrayList<String>();
        int i = 0;
        while (i < 52) {
            if (i % 4 == n) {
                list.add(cards[i]);
            }
            i++;
        }

        return list.toArray(new String[0]);
    }

}
