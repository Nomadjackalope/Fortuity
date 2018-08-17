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
 * This controls the list of dice
 */
public class DiceHistFragment extends ListFragment {

    InterCom com;

    DiceHistListAdapter diceHistAdapter;

    static DiceHistFragment newInstance(int num) {
        return new DiceHistFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com = (InterCom) getActivity();
        diceHistAdapter = new DiceHistListAdapter(getActivity(), 0);
        return inflater.inflate(R.layout.fav_pager_hist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(diceHistAdapter);

        getListView().setFooterDividersEnabled(false);
        getListView().setHeaderDividersEnabled(false);

        // Helps to show data read in from file initially
        diceHistAdapter.notifyDataSetChanged();


    }
}
