package org.bigsupersniper.wlangames.common;

import java.util.Date;
import java.util.Random;

/**
 * Created by sniper on 2014/8/4.
 */
public class StringUtils {

    public static String getRandom(int length){
        char[] chars = new char[length];
        Random r = new Random(new Date().getTime());
        while (length > 0){
            int n = r.nextInt(123);
            if ((n >= 65 && n <= 90) || n >= 97){
                chars[length - 1] = (char)n;
                length--;
            }
        }

        return String.valueOf(chars);
    }

}
