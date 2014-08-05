package org.bigsupersniper.wlangames.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.BluffDice;
import org.bigsupersniper.wlangames.socket.SocketClient;
import org.bigsupersniper.wlangames.socket.SocketCmd;
import org.bigsupersniper.wlangames.socket.SocketMessage;
import org.bigsupersniper.wlangames.view.DiceListViewAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BluffDiceFragment extends Fragment {

    private MediaPlayer player;
    private GridView gvDices ;
    private TextView tvDiceDesc;
    private TextView tvDiceCount;
    private Button btnOpen;
    private int count = 0;
    private AlertDialog resultDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluff_dice, container, false);

        tvDiceCount = (TextView)view.findViewById(R.id.textView20);
        tvDiceDesc = (TextView)view.findViewById(R.id.tvDiceDesc);
        tvDiceCount.setText("游戏次数 : " + count + " 次");
        gvDices = (GridView)view.findViewById(R.id.gvDices);
        btnOpen = (Button)view.findViewById(R.id.btnOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketMessage msg = new SocketMessage();
                msg.setFrom(socketClient.getLocalIP());
                msg.setTo(socketClient.getRemoteIP());
                msg.setCmd(SocketCmd.BluffDice_Open);
                socketClient.send(msg);
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

        return view;
    }

    private SocketClient socketClient;
    public void setSocketClient(SocketClient socketClient){
        this.socketClient = socketClient;
    }


    public void refreshDices(int[] ids){
        if (ids.length < 5) return;
        if (!btnOpen.isShown()){
            btnOpen.setVisibility(View.VISIBLE);
        }
        tvDiceDesc.setText("上一局游戏时间 : " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        tvDiceCount.setText("游戏次数 : " + (++count) + " 次");
        if (!player.isPlaying()) {
            player.start();
        }
        int[] res = BluffDice.toRes(ids);
        List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();

        for (int i = 0; i < ids.length; i++) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put("src", res[i]);
            list.add(map);
        }

        gvDices.setAdapter(new SimpleAdapter(getActivity(), list, R.layout.gv_bluff_dice_item, new String[]{ "src" }, new int[]{R.id.imgDice}));
    }

    public void showResult(Map<String, int[]> map){
        if (!map.isEmpty()) {
            Activity _that = getActivity();
            List<Map<String, Object>> adapterList = new ArrayList<Map<String, Object>>();

            Iterator<String> keys = map.keySet().iterator();

            while (keys.hasNext()) {
                String id = keys.next();
                Map<String, Object> adapterMap = new HashMap<String, Object>();
                List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
                int[] res = BluffDice.toRes(map.get(id));
                for (int i = 0; i < res.length; i++) {
                    Map<String, Integer> m = new HashMap<String, Integer>();
                    m.put("src", res[i]);
                    list.add(m);
                }

                adapterMap.put("id", id);
                adapterMap.put("diceList", list);
                adapterList.add(adapterMap);
            }

            View view = _that.getLayoutInflater().inflate(R.layout.dialog_dice_listview, null);
            ListView listView = (ListView) view.findViewById(R.id.lvDicesResult);
            listView.setAdapter(new DiceListViewAdapter(_that, adapterList));

            if (resultDialog != null && resultDialog.isShowing()) {
                resultDialog.dismiss();
            }

            resultDialog = new AlertDialog.Builder(_that).setTitle("本局结果").setView(view).setNegativeButton("确定", null).show();
        }
    }

}
