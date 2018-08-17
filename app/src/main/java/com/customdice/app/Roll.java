package com.customdice.app;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Benjamin on 4/23/2015.
 *
 * The class for saving a roll history JSON
 */
public class Roll {

    public boolean isFav;
    public boolean viewState;
    public String name;
    public int color;
    public int total;
    public int[] zero;
    public int[] one;
    public int[] two;
    public int[] three;
    public int[] four;
    public int[] five;
    public int[] six;

    public int[] colorHighlight;
    public int[] greaterLess;
    public int[] upperValue;
    public int[] lowerValue;

    public int[] rules;

    public Roll() {

    }

    public PreviousRoll getPrevRoll() {
        PreviousRoll prevRoll = new PreviousRoll(true);
        List<int[]> spaces = new ArrayList<>();

        spaces.add(0,zero);
        spaces.add(1,one);
        spaces.add(2,two);
        spaces.add(3,three);
        spaces.add(4,four);
        spaces.add(5,five);
        spaces.add(6,six);

        for (int i = 0; i < spaces.size(); i++) {
            int[] currentSpace = spaces.get(i);
            if (currentSpace != null) {

                int currentSpaceSize = currentSpace.length;

                if (currentSpaceSize > 1) {
                    for (Integer x = 1; x < currentSpaceSize; x++) {
                        prevRoll.addRoll(currentSpace[0], currentSpace[x]);
                        //System.out.println("Roll| addRoll" + currentSpace[0] + ", " + currentSpace[x]);
                    }
                }
            }
        }

        //Applies rules
        if (rules != null) {
            for (Integer x : rules) {
                prevRoll.addModificationRule(x);
            }
        }

        // Applies highlighting
        if (colorHighlight != null) {
            for (int i = 0; i < colorHighlight.length; i++) {
                prevRoll.addHighlight(colorHighlight[i], greaterLess[i], upperValue[i], lowerValue[i]);
            }
        }


        prevRoll.setName(name);
        prevRoll.setColor(color);
        prevRoll.setFave(isFav);
        prevRoll.setTotal(total);
        prevRoll.setViewState(viewState);

        return prevRoll;
    }

    // Initializes only the spaces that will be used so bonuses of 0 do not fill empty spaces
    public void init(int x, int size) {
        size = size + 1;

        switch (x) {
            case 0: zero = new int[size];
                break;
            case 1: one = new int[size];
                break;
            case 2: two = new int[size];
                break;
            case 3: three = new int[size];
                break;
            case 4: four = new int[size];
                break;
            case 5: five = new int[size];
                break;
            case 6: six = new int[size];
                break;
        }
    }

    public void initHighlightArrays(int x) {
        colorHighlight = new int[x];
        greaterLess = new int[x];
        upperValue = new int[x];
        lowerValue = new int[x];
    }

    public void initRuleArray(int x) {
        rules = new int[x];
    }

    public void addDie(int space, int sides, ArrayList<Integer> dice) {
        List<int[]> spaces = new ArrayList<>();

        spaces.add(0,zero);
        spaces.add(1,one);
        spaces.add(2,two);
        spaces.add(3,three);
        spaces.add(4,four);
        spaces.add(5,five);
        spaces.add(6,six);

        spaces.get(space)[0] = sides;

        int size = spaces.get(space).length;

        for(int x = 1; x < size; x++) {
            spaces.get(space)[x] = dice.get(x - 1);
        }

    }

    @Override
    public String toString() {
        String x = "Die";

        if(zero != null) {
            x = Integer.toString(zero[0]);
        }

        return x;
    }
}
