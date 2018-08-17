package com.customdice.app;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Benjamin on 4/15/2015.
 *
 * The class to save favorites to JSON
 */
public class Dice {

    public String name;
    public int color;
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

    public Dice() {

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
            if (spaces.get(i) != null) {
                if (spaces.get(i).length > 0) {
                    prevRoll.addFavRoll(i, spaces.get(i)[0], spaces.get(i)[1]);
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

        return prevRoll;
    }

    // Initializes only the spaces that will be used so bonuses of 0 do not fill empty spaces
    public void init(int x) {

        switch (x) {
            case 0: zero = new int[2];
                break;
            case 1: one = new int[2];
                break;
            case 2: two = new int[2];
                break;
            case 3: three = new int[2];
                break;
            case 4: four = new int[2];
                break;
            case 5: five = new int[2];
                break;
            case 6: six = new int[2];
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

    public void addDie(int space, int sides, int dice) {
        List<int[]> spaces = new ArrayList<>();

        spaces.add(0,zero);
        spaces.add(1,one);
        spaces.add(2,two);
        spaces.add(3,three);
        spaces.add(4,four);
        spaces.add(5,five);
        spaces.add(6,six);

        spaces.get(space)[0] = sides;
        spaces.get(space)[1] = dice;

    }

    @Override
    public String toString() {
        return name;
    }
}

