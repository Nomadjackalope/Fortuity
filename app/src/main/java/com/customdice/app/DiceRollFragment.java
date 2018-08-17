package com.customdice.app;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Ben on 4/14/2015.
 *
 * This handles the dice roller display, clicks, and addition
 */
public class DiceRollFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    InterCom com;

    private ImageButton d4anim;
    private ImageButton d6anim;
    private ImageButton d8anim;
    private ImageButton d10anim;
    private ImageButton d12anim;
    private ImageButton d20anim;
    private ImageButton d100anim;

    private TextView d4;
    private TextView d6;
    private TextView d8;
    private TextView d10;
    private TextView d12;
    private TextView d20;
    private TextView d100;
    private TextView plus;
    private EditText bonusDisplay;
    private TextView totalDisplay;

    private int total = 0;
    private int diceTotal = 0;
    private int percentTotal = 0;
    private int bonusTotal = 0;
    private int[] diceCount = {0,0,0,0,0,0,0};
    private boolean reset = false;
    private boolean bonusPositive = true;

    public boolean makeNewRoll;


    static DiceRollFragment newInstance() {
        return new DiceRollFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup pager, Bundle savedInstanceState) {
        makeNewRoll = true;
        com = (InterCom) getActivity();
        return inflater.inflate(R.layout.dice_roller, pager, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set all the views
        d4anim = (ImageButton) getView().findViewById(R.id.image_d4);
        d6anim = (ImageButton) getView().findViewById(R.id.image_d6);
        d8anim = (ImageButton) getView().findViewById(R.id.image_d8);
        d10anim = (ImageButton) getView().findViewById(R.id.image_d10);
        d12anim = (ImageButton) getView().findViewById(R.id.image_d12);
        d20anim = (ImageButton) getView().findViewById(R.id.image_d20);
        d100anim = (ImageButton) getView().findViewById(R.id.image_d100);


        d4 = (TextView) getView().findViewById(R.id.button_d4);
        d6 = (TextView) getView().findViewById(R.id.button_d6);
        d8 = (TextView) getView().findViewById(R.id.button_d8);
        d10 = (TextView) getView().findViewById(R.id.button_d10);
        d12 = (TextView) getView().findViewById(R.id.button_d12);
        d20 = (TextView) getView().findViewById(R.id.button_d20);
        d100 = (TextView) getView().findViewById(R.id.button_d100);
        totalDisplay = (TextView) getView().findViewById(R.id.total);
        bonusDisplay = (EditText) getView().findViewById(R.id.bonus);
        plus = (TextView) getView().findViewById(R.id.plus);

        // Set all OnClickListeners
        d4anim.setOnClickListener(this);
        d6anim.setOnClickListener(this);
        d8anim.setOnClickListener(this);
        d10anim.setOnClickListener(this);
        d12anim.setOnClickListener(this);
        d20anim.setOnClickListener(this);
        d100anim.setOnClickListener(this);
        totalDisplay.setOnClickListener(this);
        bonusDisplay.setOnClickListener(this);
        plus.setOnClickListener(this);

        d20anim.setOnLongClickListener(this);

        setOriginals();
        getBonusDisplay();
        checkTextSize();

    }

    public void setOriginals() {
        //Adds the last rolls to the history if there have been any rolls
        if (diceTotal > 0 || percentTotal > 0) {
            if (bonusTotal != 0) {
                rollTheDice(0, bonusTotal);
            }

            rollTheDice(-1, total);

            addHistory();
        }

        //Clears text and sign in bonus area and clears total
        bonusDisplay.setText("");
        modifierChanged();
        setTotalZero();

        //Sets up arrays defining all the original dice backgrounds, references, and string names
        int[] backgroundArray = {R.drawable.d4, R.drawable.d6, R.drawable.d8,
                R.drawable.d10, R.drawable.d12, R.drawable.d20, R.drawable.d10};
        TextView[] dieArray = {d4,d6,d8,d10,d12,d20,d100};
        ImageButton[] imageArray = {d4anim, d6anim, d8anim, d10anim, d12anim, d20anim, d100anim};
        java.lang.String[] dieNameArray = {"d4","d6","d8","d10","d12","d20","d%"};
        int dieArrayLen = dieArray.length;

        //Sets all the text and background back to the originals
        for (int i = 0; i < dieArrayLen; ++i) {
            dieArray[i].setText(dieNameArray[i]);
            imageArray[i].setImageResource(backgroundArray[i]);
        }

        //Garbage collection
        //System.gc();

        makeNewRoll = true;
    }

    // This is where past dice rolls and their results are stored
    public void rollTheDice(Integer sides, Integer result) {

        if(makeNewRoll) {
            PreviousRoll prev1 = new PreviousRoll();

            prev1.setViewState(com.getIndividualDisplaySetting());

            prev1.addRoll(sides, result);
            // Assuming total is called enough
            prev1.setTotal(total);
            com.rollAdd(prev1, false);
            makeNewRoll = false;
        } else {
            com.getDiceHistories().get(0).addRoll(sides, result);
            // Assuming total is called enough
            com.getDiceHistories().get(0).setTotal(total);
        }

        com.notifyDiceHist();
    }

    // When the dice are reset this is called to add the previous rolls to history
    private void addHistory() {
        //System.out.println(DRFmainRollArray);
        com.notifyDiceHist();

        bonusDisplay.setText("");
        makeNewRoll = true;
    }

    // This handles the total when the modifier changes
    private void modifierChanged() {
        if (bonusTotal < 0) {
            plus.setText("-");
        } else {
            plus.setText("+");
        }
//        if (bonusDisplay.getText().length() > 0) {
//            //Integer temp = bonusTotal * -1;
//            bonusDisplay.setText(bonusTotal);
//            bonusDisplay.invalidate();
//            //bonusTotal *= -1;
//        }
    }

    // Sets all totals to 0
    public void setTotalZero() {
        total = 0;
        bonusTotal = 0;
        diceTotal = 0;
        percentTotal = 0;

        int x;

        for (x = 0; x < diceCount.length; x++) {
            diceCount[x] = 0;
        }
        totalDisplay.setText(Integer.toString(total));
    }

    // This handles numbers typed into the bonus area
    private void getBonusDisplay() {
        bonusDisplay.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //Updates bonusTotal to the bonusDisplay taking into account the sign (+,-)
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && s.length() < 6) {
                    int sign;

                    if (plus.getText().toString().equals("+")) {
                        sign = 1;
                    } else {
                        sign = -1;
                    }
                    try {
                        bonusTotal = Integer.parseInt(s.toString()) * sign;

//                        if(Math.abs(bonusTotal) != Integer.parseInt(bonusDisplay.getText().toString()) ) {
//                            modifierChanged();
//                        }

                    } catch (NumberFormatException n) {
                        bonusDisplay.setText("");
                        bonusTotal = 0;
                    }
//                    if(s.length() == 0) {
//
//                    }


                } else {
                    bonusTotal = 0;
                }
                total();
            }
        });
    }

    // Adds up all dice and displays
    private void total() {
        if (diceTotal > 0) {
            total = diceTotal + bonusTotal;
        } else if (percentTotal > 0) {
            total = percentTotal + bonusTotal;
        } else {
            total = bonusTotal;
        }
        totalDisplay.setText(Integer.toString(total));
        totalDisplay.invalidate();
        com.notifyDiceHist();


    }

    // Changes the size of the text to fit on the screen
    private void checkTextSize() {
        if (total > 100) {
            totalDisplay.setTextSize(2, 25); // (unit, size) 2=sp
        } else {
            totalDisplay.setTextSize(2, 45); // (unit, size) 2=sp
        }
    }

    @Override
    public void onClick(View vbutton) {
        Random randomGenerator = new Random();
        long seed = randomGenerator.nextLong();

        randomGenerator.setSeed(seed);

        if(reset && vbutton != plus && vbutton != bonusDisplay) {
            reset = false;
            setTotalZero();
            setOriginals();
        }

        if (vbutton!= bonusDisplay) {
            InputMethodManager imm = (InputMethodManager) vbutton.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(vbutton.getWindowToken(), 0);
            bonusDisplay.clearFocus();
        }

        // Standard dice //simplify this section
        int result;
        if (vbutton== d4anim) {
            result = randomGenerator.nextInt(4) + 1;
            diceTotal += result;
            diceCount[0] += 1;
            animate(0);
            total();
            rollTheDice(4, result);
        } else if (vbutton== d6anim) {
            result = randomGenerator.nextInt(6) + 1;
            diceTotal += result;
            diceCount[1] += 1;
            animate(1);
            total();
            rollTheDice(6, result);
        } else if (vbutton== d8anim) {
            result = randomGenerator.nextInt(8) + 1;
            diceTotal += result;
            diceCount[2] += 1;
            animate(2);
            total();
            rollTheDice(8, result);
        } else if (vbutton== d10anim) {
            result = randomGenerator.nextInt(10) + 1;
            diceTotal += result;
            diceCount[3] += 1;
            animate(3);
            total();
            rollTheDice(10, result);
        } else if (vbutton== d12anim) {
            result = randomGenerator.nextInt(12) + 1;
            diceTotal += result;
            diceCount[4] += 1;
            animate(4);
            total();
            rollTheDice(12, result);
        } else if (vbutton== d20anim) {
            result = randomGenerator.nextInt(20) + 1;
            diceTotal += result;
            diceCount[5] += 1;
            animate(5);
            total();
            rollTheDice(20, result);

            //d%
        } else if (vbutton== d100anim) {

            //If any other dice have been rolled they are added to history and diceCount is reset
            if (diceTotal > 0) {
                setOriginals();
                setTotalZero();
            }

            //Sets the total to a number between 1 and 100 (inclusive)
            percentTotal = randomGenerator.nextInt(100) + 1;
            total();

            diceCount[6] += 1;
            animate(6);
            rollTheDice(1, percentTotal);

            reset = true;

            //Call the original text function
            //setOriginals();

            //RESETS THE CALCULATOR
        } else if (vbutton== totalDisplay) {
            //System.out.println("DRF diceHistoryArray " + diceHistoryArray);
            setOriginals();
            setTotalZero();

            //Changes bonusPositive to add or subtract modifier
        } else if(vbutton == plus) {
            bonusTotal *= -1;
            modifierChanged();
        }

        total();
        checkTextSize();
    }

    @Override
    public boolean onLongClick(View v) {
        Random randomGenerator = new Random();
        long seed = randomGenerator.nextLong();

        randomGenerator.setSeed(seed);

        if(reset && v!= plus && v != bonusDisplay) {
            reset = false;
            setTotalZero();
            setOriginals();
        }

        int result;

        if(v == d20anim) {
            //If any other dice have been rolled they are added to history and diceCount is reset
            if (diceTotal > 0) {
                setOriginals();
                setTotalZero();
            }

            result = randomGenerator.nextInt(20) + 1;
            diceTotal += result;
            diceCount[5] += 1;
            animate(5);
            total();
            rollTheDice(20, result);

            reset = true;
        }
        return true;
    }

    // This is where the changing of the number and backgrounds occurs
    private void animate(int die) {
        int[] animArray = {R.drawable.d4_anim, R.drawable.d6_anim, R.drawable.d8_anim, R.drawable.d10_anim, R.drawable.d12_anim, R.drawable.d20_anim, R.drawable.d10_anim};
        TextView[] dieArray = {d4,d6,d8,d10,d12,d20,d100};
        ImageButton[] imageArray = {d4anim, d6anim, d8anim, d10anim, d12anim, d20anim, d100anim};
        java.lang.String[] dieNameArray = {"d4","d6","d8","d10","d12","d20","d%"};


        imageArray[die].setImageResource(animArray[die]);
        AnimationDrawable animation = (AnimationDrawable) imageArray[die].getDrawable();
        animation.setOneShot(true);

        animation.stop();

        //Starts animation if it is not running
        //Don't know why this is an if-else statement
        if (animation.isRunning()) {
            dieArray[die].setBackgroundResource(animArray[die]);
        } else {
            animation.start();
        }

        // Changing of the number
        if (die != 6) {
            if(com.getDiceDisplaySetting()) {
                dieArray[die].setText(Integer.toString(diceCount[die]));
            } else {
                dieArray[die].setText(Integer.toString(diceCount[die]) + dieNameArray[die]);
            }
        }
    }

    public boolean getNewRoll() {
        return makeNewRoll;
    }

    public void setNewRoll(boolean state) {
        makeNewRoll = state;
    }

}

