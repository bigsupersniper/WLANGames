package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.view.DragAdapter;
import org.bigsupersniper.wlangames.view.DragGridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPokerFragment extends Fragment {

    private DragGridView gvCPoker;
    private TextView tvCPokerDesc;
    private TextView tvCPokerCount;
    private int count;
    private DragAdapter dragAdapter;
    private List<Map<String, Integer>> list;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_c_poker, container, false);

        tvCPokerDesc = (TextView)view.findViewById(R.id.tvCPokerDesc);
        tvCPokerCount = (TextView)view.findViewById(R.id.tvCPokerCount);
        tvCPokerCount.setText("游戏次数 : " + count + " 次");
        gvCPoker = (DragGridView)view.findViewById(R.id.gvCPoker);
        gvCPoker.setOnChangeListener(new DragGridView.OnChanageListener() {
            @Override
            public void onChange(int from, int to) {
                Map<String, Integer> temp = list.get(from);
                if(from < to){
                    for(int i= from; i<to; i++){
                        Collections.swap(list, i, i + 1);
                    }
                }else if(from > to){
                    for(int i=from; i>to; i--){
                        Collections.swap(list, i, i-1);
                    }
                }

                list.set(to, temp);
                dragAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void refreshCards(String[] cards){
        tvCPokerDesc.setText("上一局游戏时间 : " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        tvCPokerCount.setText("游戏次数 : " + (++count) + " 次");

        Class<R.drawable> c = R.drawable.class;
        Arrays.sort(cards);
        list = new ArrayList<Map<String, Integer>>();
        Map<String, Integer> m1 = new HashMap<String, Integer>();
        m1.put("src",  R.drawable.bigjoker);
        list.add(m1);
        Map<String, Integer> m2 = new HashMap<String, Integer>();
        m2.put("src", R.drawable.litterjoker);
        list.add(m2);

        for (String card : cards){
            Map<String, Integer> map = new HashMap<String, Integer>();
            try {
                map.put("src", c.getField(card).getInt(R.drawable.class));
                list.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dragAdapter = new DragAdapter(getActivity() , list);
        gvCPoker.setAdapter(dragAdapter);
        //gvCPoker.setAdapter(new SimpleAdapter(getActivity(), list, R.layout.gv_c_poker_item, new String[]{ "src" }, new int[]{R.id.imgCard}));
    }

}
