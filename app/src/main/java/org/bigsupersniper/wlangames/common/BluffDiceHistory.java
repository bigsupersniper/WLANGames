package org.bigsupersniper.wlangames.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sniper on 2014/8/5.
 */
public class BluffDiceHistory {
    private static BluffDiceHistory instance;
    private Map<String, int[]> map = new HashMap<String, int[]>();

    private BluffDiceHistory() {

    }

    public static BluffDiceHistory getInstance() {
        if (instance == null) {
            synchronized (BluffDiceHistory.class) {
                if (instance == null) {
                    instance = new BluffDiceHistory();
                }
            }
        }
        return instance;
    }

    public synchronized void add(String id, int[] dices) {
        if (!map.containsKey(id)) {
            map.put(id, dices);
        }
    }

    public synchronized void reset() {
        if (!map.isEmpty()) {
            map.clear();
        }
    }

    public synchronized Map<String, int[]> getAll() {
        return map;
    }

}
