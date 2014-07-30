package org.bigsupersniper.wlangames.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
    private int count = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluff_dice, container, false);

        tvDiceCount = (TextView)view.findViewById(R.id.tvDiceCount);
        tvDiceDesc = (TextView)view.findViewById(R.id.tvDiceDesc);
        tvDiceCount.setText("游戏次数 : " + count + " 次");
        gvDices = (GridView)view.findViewById(R.id.gvDices);
        player = MediaPlayer.create(getActivity(), R.raw.shake);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
            }
        });

        return view;
    }

    public void refreshDices(int[] ids){
        if (ids.length < 5) return;
        tvDiceDesc.setText("上一局游戏时间 : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
