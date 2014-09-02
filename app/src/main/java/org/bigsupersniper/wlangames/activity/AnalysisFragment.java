package org.bigsupersniper.wlangames.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.bigsupersniper.wlangames.R;
import java.util.List;
import java.util.Map;

public class AnalysisFragment extends Fragment {

    private ListView lvDicesResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        lvDicesResult = (ListView)view.findViewById(R.id.lvDicesResult);

        return view;
    }

    public void refreshAnalysis(List<Map<String , Object>> list) {

    }


}
