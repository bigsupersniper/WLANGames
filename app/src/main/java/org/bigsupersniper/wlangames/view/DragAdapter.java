package org.bigsupersniper.wlangames.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.bigsupersniper.wlangames.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linfeng on 2014/7/31.
 */
public class DragAdapter extends BaseAdapter implements BaseDragGridAdapter {
    private List<Map<String, Integer>> list;
    private LayoutInflater mInflater;
    private int mHidePosition = -1;

    public DragAdapter(Context context, List<Map<String, Integer>> list){
        this.list = list;
        mInflater = LayoutInflater.from(context);
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

    /**
     * 由于复用convertView导致某些item消失了，所以这里不复用item，
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.gv_c_poker_item, null);
        ImageView mImageView = (ImageView) convertView.findViewById(R.id.imgCard);
        mImageView.setImageResource((Integer) list.get(position).get("src"));

        if(position == mHidePosition){
            convertView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }


    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        Map<String, Integer> temp = list.get(oldPosition);
        if(oldPosition < newPosition){
            for(int i=oldPosition; i<newPosition; i++){
                Collections.swap(list, i, i + 1);
            }
        }else if(oldPosition > newPosition){
            for(int i=oldPosition; i>newPosition; i--){
                Collections.swap(list, i, i-1);
            }
        }

        list.set(newPosition, temp);
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

}
