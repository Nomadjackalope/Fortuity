package com.customdice.app;

import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * Created by Benjamin on 4/15/2015.
 *
 */
public interface InterCom {
    // DiceRoll
    void setNewRoll(boolean state);
    void clearRoller();
    boolean getNewRoll();

    // DiceHistories
    ArrayList<PreviousRoll> getDiceHistories();
    void removeDiceHistory(int position);
    void changeView(int position);
    void rerollOnes(int position);
    void uploadNewFav(int position);
    void rerollAllLowest(int position);
    void removeAllLowest(int position);
    void removeLowest(int position);
    void addRule(int position);
    void addHighlight(int position, boolean forFav);


    //DiceHistFragment
    ListView getDiceListView();

    // Favorites
    ArrayList<PreviousRoll> getFavorites();
    void uploadFav(int position);
    void removeFavHistory(int position);

    // Favorites Roller
    void clearSpace(int space);
    void rollAdd(PreviousRoll prevRoll, boolean forFav);
    // Used to roll a previously rolled favorite
    void rollFavorite(int position);
    // Used to quickly roll a favorite
    void rollFavoriteFav(int position);

    //FavHistFragment
    ListView getFavListView();

    // Notify
    void notifyRollAdapter();
    void notifyFavHist();
    void notifyDiceHist();

    // Dialog
    void openDialog(View view, Integer space);
    void closeDialog(boolean emptyValues, int[] values);

    // Toast
    void toast(CharSequence message);

    // JSON //Kept the JSON stuff in MainActivity for more convenient use
    //public String[] getFileList();

    // Animation
    void getAnimateRemoval(int position);
    void getAnimateRemovalFav(int position);

    // Settings
    boolean getDiceDisplaySetting();
    boolean getIndividualDisplaySetting();

    void setDiceDisplaySettings(boolean isChecked);
    void setIndividualDisplaySettings(boolean isChecked);

    void replayTutorial();

    void openDeletionDialog(boolean isFav);

    // Undo
    void onUndo();

}

