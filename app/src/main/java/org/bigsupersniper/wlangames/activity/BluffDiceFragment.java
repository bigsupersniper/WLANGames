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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bigsupersniper.wlangames.R;
import org.bigsupersniper.wlangames.common.BluffDice;
import org.bigsupersniper.wlangames.common.BluffDiceAnalysis;
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
    private GridView gvDices;
    private TextView tvDiceDesc;
    private Button btnOpen;
    private TextView tvLastHistory;
    private ListView lvHistory;
    private int count = 0;
    private AlertDialog resultDialog;
    private Map<String, int[]> lastResultHistory;
    private List<int[]> diceHistory = new ArrayList<int[]>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluff_dice, container, false);

        tvDiceDesc = (TextView) view.findViewById(R.id.tvDiceDesc);
        gvDices = (GridView) view.findViewById(R.id.gvDices);
        btnOpen = (Button) view.findViewById(R.id.btnOpen);
        tvLastHistory = (TextView) view.findViewById(R.id.tvLastHistory);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //若已请求结果则不重复发送请求
                if (lastResultHistory == null) {
                    getParent().sendCmd(SocketCmd.BluffDice_Open);
                } else {
                    showDices(lastResultHistory);
                }
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

        lvHistory = (ListView)view.findViewById(R.id.lvHistory);

        return view;
    }

    private IndexActivity getParent() {
        return (IndexActivity) getActivity();
    }

    public void router(SocketMessage msg) {
        int cmd = msg.getCmd();
        switch (cmd) {
            case SocketCmd.BluffDice_Send:
                int[] ids = new Gson().fromJson(msg.getBody(), int[].class);
                this.refreshDices(ids);
                this.lastResultHistory = null;
                break;
            case SocketCmd.BluffDice_Open_Resp:
                lastResultHistory = new Gson().fromJson(msg.getBody(), new TypeToken<Map<String, int[]>>() {
                }.getType());
                this.showDices(lastResultHistory);
                break;
            default:
                break;
        }
    }

    public void refreshHistory(int[] ids){
        String key ="";
        for (int i = 0 ; i < ids.length ; i++){
            int x = ids[i] + 1;
            key += x;
        }
        diceHistory.add(0, ids);
        //add to analysis
        BluffDiceAnalysis.getInstance().add(key , ids);

        List<Map<String, Object>> adapterList = new ArrayList<Map<String, Object>>();
        int all = diceHistory.size();
        int size = all;
        if (diceHistory.size() >= 5){
            size = 5;
        }

        if (size > 0){
            Iterator<int[]> iterator = diceHistory.iterator();
            while (iterator.hasNext()){
                Map<String, Object> adapterMap = new HashMap<String, Object>();
                List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
                int[] res = BluffDice.toRes(iterator.next());
                for (int j = 0; j < res.length; j++) {
                    Map<String, Integer> m = new HashMap<String, Integer>();
                    m.put("src", res[j]);
                    list.add(m);
                }

                adapterMap.put("title", "第 " + (all--) + " 局");
                adapterMap.put("diceList", list);
                adapterList.add(adapterMap);

                if (adapterList.size() == size) break;
            }
        }

        lvHistory.setAdapter(new DiceListViewAdapter(getActivity(), adapterList));
    }

    public void refreshDices(int[] ids) {
        if (ids.length < 5) return;
        if (!btnOpen.isShown()) {
            btnOpen.setVisibility(View.VISIBLE);
        }
        if (!tvLastHistory.isShown()){
            tvLastHistory.setVisibility(View.VISIBLE);
        }
        count++;
        Toast.makeText(getActivity() , "第 " + (count) + " 局游戏已开始" , Toast.LENGTH_SHORT).show();
        tvDiceDesc.setText("第 " + (count) + " 局游戏开始于 : " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
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

        if (resultDialog != null && resultDialog.isShowing()) {
            resultDialog.dismiss();
        }

        gvDices.setAdapter(new SimpleAdapter(getActivity(), list, R.layout.gv_bluff_dice_item, new String[]{"src"}, new int[]{R.id.imgDice}));
        //bind history
        refreshHistory(ids);
    }

    public void showDices(Map<String, int[]> map) {
        if (!map.isEmpty()) {
            Activity _that = getActivity();
            List<Map<String, Object>> adapterList = new ArrayList<Map<String, Object>>();

            Iterator<String> keys = map.keySet().iterator();

            while (keys.hasNext()) {
                String key = keys.next();
                Map<String, Object> adapterMap = new HashMap<String, Object>();
                List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
                int[] res = BluffDice.toRes(map.get(key));
                for (int i = 0; i < res.length; i++) {
                    Map<String, Integer> m = new HashMap<String, Integer>();
                    m.put("src", res[i]);
                    list.add(m);
                }

                adapterMap.put("title", key);
                adapterMap.put("diceList", list);
                adapterList.add(adapterMap);
            }

            View view = _that.getLayoutInflater().inflate(R.layout.dialog_dice_listview, null);
            ListView listView = (ListView) view.findViewById(R.id.lvDicesResult);
            listView.setAdapter(new DiceListViewAdapter(_that, adapterList));

            if (resultDialog != null && resultDialog.isShowing()) {
                resultDialog.dismiss();
            }

            resultDialog = new AlertDialog.Builder(_that).setTitle("第 " + count + " 局结果").setView(view).setNegativeButton("确定", null).show();
        }
    }

}
