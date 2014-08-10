package org.bigsupersniper.wlangames.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.bigsupersniper.wlangames.R;

import java.util.List;
import java.util.Map;

/**
 * Created by linfeng on 2014/8/5.
 */
public class DiceListViewAdapter extends BaseAdapter {
    private List<Map<String, Object>> list;
    private LayoutInflater inflater;
    private Context context;

    public DiceListViewAdapter(Context context, List<Map<String, Object>> list) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.dialog_dice_listview_item, null);
        GridView gvDicesResult = (GridView) view.findViewById(R.id.gvDicesResult);
        TextView tvClientIP = (TextView) view.findViewById(R.id.tvClientIP);
        Map<String, Object> map = list.get(position);
        tvClientIP.setText(map.get("id").toString());
        List<Map<String, Integer>> list = (List<Map<String, Integer>>) map.get("diceList");
        gvDicesResult.setAdapter(new SimpleAdapter(this.context, list, R.layout.gv_bluff_dice_item, new String[]{"src"}, new int[]{R.id.imgDice}));

        return view;
    }

}
