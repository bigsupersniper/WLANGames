package org.bigsupersniper.wlangames.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.StackView;
import android.widget.TextView;
import android.widget.Toast;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.BluffDice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluffDiceFragment extends Fragment {

    private MediaPlayer player;
    private GridView gvDices ;
    private TextView tvDiceDesc;
    private TextView tvDiceCount;
    private GridView gvSelect;
    private int count = 0;
    private static List<Map<String, Integer>> stackViewList ;

    static {
        stackViewList = new ArrayList<Map<String, Integer>>();
        for (int i = 0 ; i < 6 ; i++){
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put("src", BluffDice.getResId(i));
            stackViewList.add(map);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluff_dice, container, false);

        tvDiceCount = (TextView)view.findViewById(R.id.textView20);
        tvDiceDesc = (TextView)view.findViewById(R.id.tvDiceDesc);
        tvDiceCount.setText("游戏次数 : " + count + " 次");
        gvDices = (GridView)view.findViewById(R.id.gvDices);
        gvSelect = (GridView)view.findViewById(R.id.gvSelect);
        gvSelect.setAdapter(new SimpleAdapter(getActivity(), stackViewList , R.layout.gv_bluff_dice_item, new String[]{ "src" }, new int[]{R.id.imgDice}));
        gvSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Integer> map = (Map<String, Integer>)adapterView.getItemAtPosition(i);
                Toast.makeText(getActivity() , BluffDice.valueOf(map.get("src")) , Toast.LENGTH_SHORT).show();
            }
        });
        player = MediaPlayer.create(getActivity(), R.raw.shake);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
            }
        });

        NumberPicker numberPicker = (NumberPicker)view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(1);
        numberPicker.setMaxValue(20);

        return view;
    }

    public void refreshDices(int[] ids){
        if (ids.length < 5) return;
        tvDiceDesc.setText("上一局游戏时间 : " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        tvDiceCount.setText("游戏次数 : " + (++count) + " 次");
        if (!player.isPlaying()) {
            player.start();
        }
        Arrays.sort(ids);
        int[] res = BluffDice.toRes(ids);
        List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();

        for (int i = 0; i < ids.length; i++) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put("src", res[i]);
            list.add(map);
        }

        gvDices.setAdapter(new SimpleAdapter(getActivity(), list, R.layout.gv_bluff_dice_item, new String[]{ "src" }, new int[]{R.id.imgDice}));
    }

}
