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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_c_poker, container, false);
        TextView textView = (TextView)view.findViewById(R.id.textView);
        textView.setText(new Date().getTime() + "");

        return view;
    }

}
