package com.customdice.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by Benjamin on 5/19/2015.
 *
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    InterCom com;

    private CheckBox diceDisplay;
    private CheckBox individualDisplay;
    private Button replayTutorial;
    private Button deleteHistory;
    private Button deleteFavorites;

    static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        com = (InterCom) getActivity();

        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        diceDisplay = (CheckBox) v.findViewById(R.id.dice_display);
        individualDisplay = (CheckBox) v.findViewById(R.id.individual_display);
        replayTutorial = (Button) v.findViewById(R.id.replay_tutorial);
        deleteHistory = (Button) v.findViewById(R.id.clear_dice_history);
        deleteFavorites = (Button) v.findViewById(R.id.clear_favorites);

        replayTutorial.setOnClickListener(this);
        deleteHistory.setOnClickListener(this);
        deleteFavorites.setOnClickListener(this);

        diceDisplay.setOnCheckedChangeListener(this);
        individualDisplay.setOnCheckedChangeListener(this);

        getAllSettings();

    }

    @Override
    public void onClick(View v) {
        if (v == replayTutorial) {
            com.replayTutorial();
        } else if (v == deleteHistory) {
            com.openDeletionDialog(false);
        } else if (v == deleteFavorites) {
            com.openDeletionDialog(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == diceDisplay) {
            com.setDiceDisplaySettings(isChecked);
        } else if (buttonView == individualDisplay) {
            com.setIndividualDisplaySettings(isChecked);
        }

    }

    private void getAllSettings() {
        diceDisplay.setChecked(com.getDiceDisplaySetting());
        individualDisplay.setChecked(com.getIndividualDisplaySetting());
    }

    public void setDiceDisplay(boolean diceDisplay) {
        this.diceDisplay.setChecked(diceDisplay);
    }

    public void setIndividualDisplay(boolean individualDisplay) {
        this.individualDisplay.setChecked(individualDisplay);
    }
}
