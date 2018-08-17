package com.customdice.app;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Created by Benjamin on 4/15/2015.
 *
 *
 */
public class FavRollFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    InterCom com;

    private TextView c1; //stands for custom1
    private TextView c2;
    private TextView c3;
    private TextView c4;
    private TextView c5;
    private TextView c6;
    private TextView c7;
    private TextView cRoll;
    private TextView cTotal;
    private TextView cAddToFav;
    private ViewGroup rTotal;

    private int total;
    private PreviousRoll dice;

//    private Integer tempmod;
//    private Integer tempdice;
//    private Integer tempsides;
//    private boolean areEmptyValues;
//    private boolean isModValue;

    boolean saved = true;

//    View vMain;

    static FavRollFragment newInstance() {
        return new FavRollFragment();
    }

    // REMOVED onCreate()


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(dice == null) {
            dice = new PreviousRoll(true);
        }
        com = (InterCom) getActivity();

        return inflater.inflate(R.layout.fav_roller, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Sets all views
        c1 = (TextView) getView().findViewById(R.id.custom_button_1);
        c2 = (TextView) getView().findViewById(R.id.custom_button_2);
        c3 = (TextView) getView().findViewById(R.id.custom_button_3);
        c4 = (TextView) getView().findViewById(R.id.custom_button_4);
        c5 = (TextView) getView().findViewById(R.id.custom_button_5);
        c6 = (TextView) getView().findViewById(R.id.custom_button_6);
        c7 = (TextView) getView().findViewById(R.id.custom_button_7);
        cRoll = (TextView) getView().findViewById(R.id.custom_roll);
        cTotal = (TextView) getView().findViewById(R.id.custom_button_total);
        cAddToFav = (TextView) getView().findViewById(R.id.custom_button_addFav);
        rTotal = (ViewGroup) getView().findViewById(R.id.fav_total);

        // Set all onClickListeners

        c1.setOnClickListener(this);
        c2.setOnClickListener(this);
        c3.setOnClickListener(this);
        c4.setOnClickListener(this);
        c5.setOnClickListener(this);
        c6.setOnClickListener(this);
        c7.setOnClickListener(this);
        cRoll.setOnClickListener(this);
        rTotal.setOnClickListener(this);
        cAddToFav.setOnClickListener(this);

        c1.setOnLongClickListener(this);
        c2.setOnLongClickListener(this);
        c3.setOnLongClickListener(this);
        c4.setOnLongClickListener(this);
        c5.setOnLongClickListener(this);
        c6.setOnLongClickListener(this);
        c7.setOnLongClickListener(this);
        cRoll.setOnLongClickListener(this);

        c1.setOnTouchListener(this);
        c2.setOnTouchListener(this);
        c3.setOnTouchListener(this);
        c4.setOnTouchListener(this);
        c5.setOnTouchListener(this);
        c6.setOnTouchListener(this);
        c7.setOnTouchListener(this);

        setOriginals();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("saved", saved);

        super.onSaveInstanceState(outState);
    }

    // Sets all text to the original text
    private void setOriginals() {
        TextView[] favoriteArray = {c1, c2, c3, c4, c5, c6, c7};

        // Sets all the text back to "..."
        //int favoriteArrayLen = favoriteArray.length;
        for (TextView aFavoriteArray : favoriteArray) {
            aFavoriteArray.setText(R.string.empty_custom_die);
        }

        cAddToFav.setText(R.string.add_to_favorites);

        cTotal.setText("0");

    }

    private void updateSpaces() {
        TextView[] favoriteArray = {c1, c2, c3, c4, c5, c6, c7};

        int favoriteArrayLen = favoriteArray.length;
        for (int i = 0; i < favoriteArrayLen; ++i) {
            ArrayList<Integer> die = dice.getSpace(i);
            if (die != null) {
                addModOrDie(i, die.get(0), die.get(1));
            } else {
                favoriteArray[i].setText(R.string.empty_custom_die);
            }
        }

    }


    @Override
    public void onClick(View v) {
        //
        if(cTotal.getText() != "0") {
            total = 0;
            cTotal.setText(Integer.toString(total));
        }

        // any sided dice  //dice.addFavRoll(space, #sides, #dice) {space=[numSides, numDice]}
        if (v == c1) {
            com.openDialog(v, 0);
        } else if (v == c2) {
            com.openDialog(v, 1);
        } else if (v == c3) {
            com.openDialog(v, 2);
        } else if (v == c4) {
            com.openDialog(v, 3);
        } else if (v == c5) {
            com.openDialog(v, 4);
        } else if (v == c6) {
            com.openDialog(v, 5);
        } else if (v == c7) {
            com.openDialog(v, 6);
        } else if (v == rTotal) {
            total = 0;
            cTotal.setText(Integer.toString(total));
            setOriginals();
            dice = new PreviousRoll(true);
        } else if (v == cAddToFav ) {
            //System.out.println("FRF" + dice.getPreviousRoll());
            if (!dice.getPreviousRoll().isEmpty()) {
                dice.setForFavList(true);
                //dice.setID(com.getFavorites().size()); // TODO remove this when animation fixed
                com.rollAdd(dice, true);
                dice = new PreviousRoll(true);
                com.notifyFavHist();
                setOriginals();
                com.uploadFav(0);
                //System.out.println("FRFcAdd..Favorites list: " + com.getFavorites());
            }
        } else if (v == cRoll) {
            rollFav();
        }
        checkTextSize();
    }

    public void rollFav() {
        if (!dice.getPreviousRoll().isEmpty()) {
            // Checks that there are dice to roll
            if(!dice.checkOnlyMod()) {
                // Clear the dice roller to not complicate things
                com.clearRoller();

                roll();
                checkTextSize();
                cTotal.setText(Integer.toString(total));
            }
        } else {
            com.toast(getString(R.string.dice_empty));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v == c1) {
            dice.removeSpace(0);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } else if (v == c2) {
            dice.removeSpace(1);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } else if (v == c3) {
            dice.removeSpace(2);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } else if (v == c4) {
            dice.removeSpace(3);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } else if (v == c5) {
            dice.removeSpace(4);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } else if (v == c6) {
            dice.removeSpace(5);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } else if (v == c7) {
            dice.removeSpace(6);
            updateSpaces();
            com.notifyFavHist();
            return true;
        } if (v == cRoll) {
            if (!dice.getPreviousRoll().isEmpty()) {
                if (!com.getDiceHistories().isEmpty()) {

                    addRoll();
                    checkTextSize();
                    cTotal.setText(Integer.toString(total));

                } else {
                        // Checks that there are dice to roll
                    if (!dice.checkOnlyMod()) {
                            // Clear the dice roller to not complicate things
                            com.clearRoller();

                            roll();
                            checkTextSize();
                            cTotal.setText(Integer.toString(total));
                    }
                }
            }
            return true;
        }

        return false;
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    TransitionDrawable newBackground = (TransitionDrawable) getResources().getDrawable(R.drawable.delete_transition, null);
                    v.setBackground(newBackground);
                    TransitionDrawable drawable = (TransitionDrawable) v.getBackground().getCurrent();
                    drawable.startTransition(700);

                } catch (Resources.NotFoundException ex) {
                    System.out.println("Drawable not found");
                    ex.printStackTrace();
                }
            } else {

                try {
                    TransitionDrawable newBackground = (TransitionDrawable) getResources().getDrawable(R.drawable.delete_transition);
                    v.setBackground(newBackground);
                    TransitionDrawable drawable = (TransitionDrawable) v.getBackground().getCurrent();
                    drawable.startTransition(700);

                } catch (Resources.NotFoundException e) {
                    System.out.println("Drawable not found");
                    e.printStackTrace();
                }
            }

        } else if (event.getAction() == MotionEvent.ACTION_CANCEL ||
                event.getAction() == MotionEvent.ACTION_UP) {

            TransitionDrawable drawable = (TransitionDrawable) v.getBackground().getCurrent();
            drawable.reverseTransition(200);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    Drawable standardBackground = getResources().getDrawable(R.drawable.outline, null);
                    v.setBackground(standardBackground);

                } catch (Resources.NotFoundException e) {
                    System.out.println("Drawable not found");
                    e.printStackTrace();
                }
            } else {

                try {
                    Drawable standardBackground = getResources().getDrawable(R.drawable.outline);
                    v.setBackground(standardBackground);

                } catch (Resources.NotFoundException e) {
                    System.out.println("Drawable not found");
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    // Rolls the current set of dice
    // Essentially this converts a forFavList PreviousRoll to a history item PreviousRoll
    // {space=[numSides, numDice]}
    private void roll() {
        PreviousRoll prevRoll = new PreviousRoll();

        prevRoll.setViewState(com.getIndividualDisplaySetting());

        Random randomGenerator = new Random();
        Integer result;

        prevRoll.setName(dice.getName());
        prevRoll.setColor(dice.getColor());
        prevRoll.setRules(dice.getAllRules());

        //System.out.println("FRF dice: " + dice.getPreviousRoll());
        for (Map.Entry<Integer, ArrayList<Integer>> entry : dice.getPreviousRoll().entrySet()) {
            ArrayList<Integer> sidesAndDice = entry.getValue();
            Integer numSides = sidesAndDice.get(0);
            Integer numDice = sidesAndDice.get(1);

            if (numSides == 0) {
                prevRoll.addRoll(0, numDice);
                total += numDice;
            } else if (numSides == 1) {
                for (Integer i = 0; i < numDice; i++) {
                    result = randomGenerator.nextInt(100) + 1;
                    prevRoll.addRoll(1, result);
                    total += result;
                }
            } else {
                for (Integer i = 0; i < numDice; i++) {
                    result = randomGenerator.nextInt(numSides) + 1;
                    prevRoll.addRoll(numSides, result);
                    total += result;
                }
            }
        }

        prevRoll = applyModifications(prevRoll, dice.getModifications());

        //System.out.println("FRF" + total);
        //System.out.println("FRF" + prevRoll);
        prevRoll.total();
        total = prevRoll.getTotal();
        prevRoll.setFave(true);

        if(prevRoll.hasValues()) {
            // Add this previous roll to the history
            //com.getDiceHistories().add(0, prevRoll);
            com.rollAdd(prevRoll, false);
            com.notifyDiceHist();
        } else {
            com.toast(getString(R.string.dice_empty));
        }
    }

    private void addRoll() {
        PreviousRoll prevRoll = com.getDiceHistories().get(0);
        Random randomGenerator = new Random();
        Integer result;

        ArrayList<Integer> diceList = new ArrayList<>();

        for (Map.Entry<Integer, ArrayList<Integer>> entry : prevRoll.getPreviousRoll().entrySet()) {
            if (entry.getKey() != -1) {
                diceList.add(entry.getKey());
            }
        }

        for (Map.Entry<Integer, ArrayList<Integer>> entry : dice.getPreviousRoll().entrySet()) {
            if (!diceList.contains(entry.getValue().get(0))) {
                if (entry.getValue().get(0) != -1) {
                    diceList.add(entry.getValue().get(0));
                }
            }
        }

        System.out.println("FRF| preRoll: " + prevRoll);
        System.out.println("FRF| dice: " + dice);
        System.out.println("FRF| diceList: " + diceList);

        if(diceList.size() <= 7) {

            //System.out.println("FRF dice: " + dice.getPreviousRoll());
            for (Map.Entry<Integer, ArrayList<Integer>> entry : dice.getPreviousRoll().entrySet()) {
                ArrayList<Integer> sidesAndDice = entry.getValue();
                Integer numSides = sidesAndDice.get(0);
                Integer numDice = sidesAndDice.get(1);

                if (numSides == 0) {
                    prevRoll.addRoll(0, numDice);
                    total += numDice;
                } else if (numSides == 1) {
                    for (Integer i = 0; i < numDice; i++) {
                        result = randomGenerator.nextInt(100) + 1;
                        prevRoll.addRoll(1, result);
                        total += result;
                    }
                } else {
                    for (Integer i = 0; i < numDice; i++) {
                        result = randomGenerator.nextInt(numSides) + 1;
                        prevRoll.addRoll(numSides, result);
                        total += result;
                    }
                }
            }

            prevRoll.total();
            com.notifyDiceHist();
        } else {
            com.toast(getResources().getText(R.string.too_many_dice));
        }
    }

    // Changes the size of the text to fit on the screen
    private void checkTextSize() {
        if (total > 1000) {
            cTotal.setTextSize(2, 15); // (unit, size) 2=sp
        } else if (total > 100) {
            cTotal.setTextSize(2, 25); // (unit, size) 2=sp
        } else {
            cTotal.setTextSize(2, 35); // (unit, size) 2=sp
        }
    }

    // Adds favorite into FavRollFragment
    public void uploadFav(PreviousRoll favorite) {
        total = 0;
        setOriginals();
        dice = new PreviousRoll(true);
        //dice.setForFavList(true);
        dice.receiveRoll(favorite.getPreviousRoll());

        dice.setName(favorite.getName());
        dice.setColor(favorite.getColor());
        dice.setRules(favorite.getAllRules());
        dice.setModifications(favorite.getModifications());

        TextView[] favoriteArray = {c1, c2, c3, c4, c5, c6, c7};
        Integer space;

        for (Map.Entry<Integer, ArrayList<Integer>> entry : dice.getPreviousRoll().entrySet()) {
            space = entry.getKey();
            ArrayList<Integer> sidesAndDice = entry.getValue();
            Integer numSides = sidesAndDice.get(0);
            Integer numDice = sidesAndDice.get(1);
            StringBuilder diceText = new StringBuilder();

            if (numSides == 0) {
                if (numDice < 0) {
                    diceText.append("-");
                } else {
                    diceText.append("+");
                }
                diceText.append(Math.abs(numDice));
            } else if (numSides == 1) {
                diceText.append(numDice).append("d%");
            } else {
                diceText.append(numDice).append("d").append(numSides);
            }

            favoriteArray[space].setText(diceText.toString());
        }
    }

    public void addModOrDie(Integer space, Integer numSides, Integer numDice) {
        TextView[] favoriteArray = {c1,c2,c3,c4,c5,c6,c7};
        String die;

        if (numSides == 0) {
            dice.addFavRoll(space, 0, numDice);
            die = numDice.toString();
        } else {
            dice.addFavRoll(space, numSides, numDice);
            die = numDice.toString() + "d" + numSides.toString();
        }

        favoriteArray[space].setText(die);
    }

    // Applies the modifications to the rolled dice
    public PreviousRoll applyModifications(PreviousRoll previousRoll, ArrayList<Integer> modifications) {
        if (!modifications.isEmpty()) {
            for (Integer i : modifications) {
                switch (i) {
                    case 0:
                        previousRoll.rerollOnes();
                        break;
                    case 1:
                        previousRoll.removeOneLowest();
                        break;
                    case 2:
                        previousRoll.rerollAllLowest();
                        break;
                    default:
                        break;
                }
            }
        }

        return previousRoll;
    }
}

