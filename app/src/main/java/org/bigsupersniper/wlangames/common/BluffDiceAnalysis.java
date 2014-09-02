package org.bigsupersniper.wlangames.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by linfeng on 2014/9/2.
 */
public class BluffDiceAnalysis {
    private static BluffDiceAnalysis instance;
    private Map<String , DiceAnalysis> analysisMap = new HashMap<String, DiceAnalysis>();

    private BluffDiceAnalysis() {

    }

    public static BluffDiceAnalysis getInstance() {
        if (instance == null) {
            synchronized (BluffDiceAnalysis.class) {
                if (instance == null) {
                    instance = new BluffDiceAnalysis();
                }
            }
        }
        return instance;
    }

    public synchronized void add(String key, int[] dices) {
        if(analysisMap.containsKey(key)){
            DiceAnalysis analysis = analysisMap.get(key);
            analysis.setTotal(analysis.getTotal() + 1);
        }else{
            DiceAnalysis analysis = new DiceAnalysis();
            analysis.setDices(dices);
            analysis.setTotal(1);
            analysisMap.put(key ,analysis);
        }
    }

    public synchronized void reset() {
        if (!analysisMap.isEmpty()) {
            analysisMap.clear();
        }
    }

    public synchronized List<DiceAnalysis> getSortList() {
        List<DiceAnalysis> list = new ArrayList<DiceAnalysis>();
        if (!analysisMap.isEmpty()){
            Iterator<String> keys = analysisMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                list.add(analysisMap.get(key));
            }

            Collections.sort(list, new Comparator<DiceAnalysis>() {
                @Override
                public int compare(DiceAnalysis diceAnalysis, DiceAnalysis diceAnalysis2) {
                    if (diceAnalysis.getTotal() > diceAnalysis2.getTotal()) {
                        return -1;
                    } else if (diceAnalysis.getTotal() < diceAnalysis2.getTotal()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

        }

        return  list;
    }
}
