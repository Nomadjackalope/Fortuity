package com.customdice.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Benjamin on 4/15/2015.
 *
 * This controls the list of favorites
 */
public class FavHistFragment extends ListFragment {

    InterCom com;

    FavHistListAdapter favHistAdapter;

    static FavHistFragment newInstance(int num) {
        return new FavHistFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com = (InterCom) getActivity();
        favHistAdapter = new FavHistListAdapter(getActivity(), 0);

        return inflater.inflate(R.layout.fav_pager_hist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(favHistAdapter);

        if(favHistAdapter != null) {
            favHistAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setFooterDividersEnabled(false);
        getListView().setHeaderDividersEnabled(false);

        favHistAdapter.notifyDataSetChanged();
    }
}

