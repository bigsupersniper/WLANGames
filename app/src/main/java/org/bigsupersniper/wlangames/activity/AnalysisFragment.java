package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.BluffDice;
import org.bigsupersniper.wlangames.common.BluffDiceAnalysis;
import org.bigsupersniper.wlangames.common.DiceAnalysis;
import org.bigsupersniper.wlangames.view.DiceListViewAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalysisFragment extends Fragment {

    private ListView lvDicesResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        lvDicesResult = (ListView)view.findViewById(R.id.lvDicesResult);

        return view;
    }

    public void refreshAnalysis() {
        List<Map<String, Object>> adapterList = new ArrayList<Map<String, Object>>();
        List<DiceAnalysis> ls = BluffDiceAnalysis.getInstance().getSortList();
        if (!ls.isEmpty()){
            int i = 1;
            Iterator<DiceAnalysis> iterator = ls.iterator();
            while (iterator.hasNext()){
                DiceAnalysis analysis = iterator.next();
                Map<String, Object> adapterMap = new HashMap<String, Object>();
                List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
                int[] res = BluffDice.toRes(analysis.getDices());
                for (int j = 0; j < res.length; j++) {
                    Map<String, Integer> m = new HashMap<String, Integer>();
                    m.put("src", res[j]);
                    list.add(m);
                }

                adapterMap.put("title", i + "、" + analysis.getTotal() + " 次");
                adapterMap.put("diceList", list);
                adapterList.add(adapterMap);

                if (i++ == 10)break;
            }
        }

        lvDicesResult.setAdapter(new DiceListViewAdapter(getActivity(), adapterList));
    }


}
