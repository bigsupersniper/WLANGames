package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.bigsupersniper.wlangames.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPokerFragment extends Fragment {

    private GridView gvCPoker;
    private TextView tvCPokerDesc;
    private TextView tvCPokerCount;
    private int count;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_c_poker, container, false);

        tvCPokerDesc = (TextView)view.findViewById(R.id.tvCPokerDesc);
        tvCPokerCount = (TextView)view.findViewById(R.id.tvCPokerCount);
        tvCPokerCount.setText("游戏次数 : " + count + " 次");
        gvCPoker = (GridView)view.findViewById(R.id.gvCPoker);

        return view;
    }

    public void refreshCards(String[] cards){
        tvCPokerDesc.setText("上一局游戏时间 : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        tvCPokerCount.setText("游戏次数 : " + (++count) + " 次");

        Class<R.drawable> c = R.drawable.class;
        Arrays.sort(cards);
        List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
        for (String card : cards){
            Map<String, Integer> map = new HashMap<String, Integer>();
            try {
                map.put("src", c.getField(card).getInt(R.drawable.class));
                list.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        gvCPoker.setAdapter(new SimpleAdapter(getActivity(), list, R.layout.gv_c_poker_item, new String[]{ "src" }, new int[]{R.id.imgCard}));
    }

}
