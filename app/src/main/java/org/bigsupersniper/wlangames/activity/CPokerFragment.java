package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bigsupersniper.wlangames.R;

import java.util.Date;

public class CPokerFragment extends Fragment {

    private TextView tvCPokerDesc;
    private TextView tvCPokerCount;
    private int count;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_c_poker, container, false);

        tvCPokerDesc = (TextView)view.findViewById(R.id.tvCPokerDesc);
        tvCPokerCount = (TextView)view.findViewById(R.id.tvCPokerCount);
        tvCPokerCount.setText("游戏次数 : " + count + " 次");

        return view;
    }

    public void refreshCards(String cards){
        tvCPokerDesc.setText(cards);
        tvCPokerCount.setText("游戏次数 : " + (++count) + " 次");
    }

}
