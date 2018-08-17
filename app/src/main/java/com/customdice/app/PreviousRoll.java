package com.customdice.app;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;


/**
 * Created by Ben on 3/4/2015.
 *
 * This is the object that is used for history items, favorite or not
 *  Special side dice
 *  -1 = total
 *  0 = modifier
 *  1 = d%
 *
 *  example PreviousRoll Map
 *  {-1=[21], 0=[-2, 4], 4=[3,2], 6=[1,3,3,5,2]}
 *     toString = 2d4 + 5d6 - 2 + 4 = 21 or Mod: -2, 4 | d4: 3, 2 | d6: 1, 3, 3, 5, 2
 *
 *  example PreviousRoll Map as a favorite in other words, forFavList == true
 *  Replicates the custom dice area custom buttons 1 to 7 mapped as 0 to 6 in order
 *  User added dice in following order in spaces 1 to 5 with no empty spaces between dice
 *      2d4 + 2d6 - 2 + 4 + 3d6
 *      {0=[4, 2], 1=[6, 2], 2=[0, -2], 3=[0, 4], 4=[6, 3]}
 *      {space=[numSides, numDice]}
 *
 */
public class PreviousRoll {
    private Map<Integer, ArrayList<Integer>> roll = new TreeMap<>();
    private Map<Integer, ArrayList<VOI>> valuesOfImportance = new TreeMap<>();
    private ArrayList<Rule> rules;
    private ArrayList<Integer> modifications;
    private static int id = 0;
    private int ID;
    private boolean viewIndividual;
    private boolean isFave;
    private boolean forFavList;
    private String name;
    private int color;

    // Constructor
    public PreviousRoll() {
        ID = id;
        id++;
        viewIndividual = false;
        isFave = false;
        forFavList = false;
        setTotal(0);
        name = "Favorite" + ID;  //Need this and color in case this is changed to fave
        color = 0xFF757575;
        rules = new ArrayList<>();
        modifications = new ArrayList<>();
    }

    // Fav Constructor
    public PreviousRoll(Boolean isFave) {
        ID = id;
        id++;
        viewIndividual = false;
        this.isFave = isFave;
        forFavList = false;
        name = "Favorite" + ID;
        color = 0xFF757575;
        rules = new ArrayList<>();
        modifications = new ArrayList<>();
    }

    //--------------- History -----------------//

    // Adds a single die roll to the roll map
    // Also used to add a set of dice if isFave == true ex. 2d4 {4, [2]} this is a die group
    public void addRoll(Integer sides, Integer result) {
        if (sides != 0) {
            if (roll.get(sides) == null) {
                roll.put(sides, new ArrayList<Integer>());
                roll.get(sides).add(result);
            } else {
                roll.get(sides).add(result);
            }
        } else {
            if (roll.get(sides) == null) {
                roll.put(sides, new ArrayList<Integer>());
                roll.get(sides).add(result);
            } else {
                Integer mod = roll.get(0).get(0);
                mod += result;
                roll.get(sides).add(0, mod);
            }
        }
    }

    // Used by FavRollFragment to add a {space=[numSides, numDice]}
    public void addFavRoll(Integer space, Integer sides, Integer dice) {
        ArrayList<Integer> sidesAndDice = new ArrayList<>(2);
        sidesAndDice.add(0, sides);
        sidesAndDice.add(1, dice);
        roll.put(space, sidesAndDice);
    }

    // Sets the total
    public void setTotal(Integer tot) {
        roll.put(-1, new ArrayList<Integer>()); //not efficient
        roll.get(-1).add(0, tot);
    }

    public int getTotal() {
        return roll.get(-1).get(0);
    }

    // Might never be used but it can return the roll map
    public Map<Integer, ArrayList<Integer>> getPreviousRoll() {
        return roll;
    }

    public void receiveRoll(Map<Integer, ArrayList<Integer>> roll) {
        this.roll = roll;
    }

    // Clears the roll map
    public void clear() {
        roll.clear();
    }

    // Will return a string of the roll map in one of two forms
    @Override
    public String toString() {

        if(forFavList) {
            return toFavString();
        } else {

            StringBuilder allRolls = new StringBuilder("");

            // If viewIndividual is true this builds the individual view string ex. Mod: -2 | d4: 3, 4
            if (getViewState()) {

                Iterator<Map.Entry<Integer, ArrayList<Integer>>> iterate = roll.entrySet().iterator();
                while (iterate.hasNext()) {
                    Map.Entry entry = iterate.next();
                    Integer die = (Integer) entry.getKey();

                    if (die != -1) {

                        if (die == 0) {
                            allRolls.append("Mod: ");
                        } else {
                            allRolls.append("d").append(die).append(": ");
                        }

                        ArrayList<Integer> numSides = (ArrayList<Integer>) entry.getValue();
                        Integer numSidesSize = numSides.size();
                        for (Integer i = 0; i < numSidesSize; i++) {
                            allRolls.append(numSides.get(i));
                            if (i < numSidesSize - 1) {
                                allRolls.append(", ");
                            }
                        }

                        if (iterate.hasNext()) {
                            allRolls.append(" | ");
                        }
                    }
                }


                // If viewIndividual is false this builds the additive view string ex. 2d4 - 2 = 5
            } else {
                Iterator<Map.Entry<Integer, ArrayList<Integer>>> iterate = roll.entrySet().iterator();
                Integer mod = 0;
                Integer tot = 0;
                boolean addPlus = false;
                while (iterate.hasNext()) {
                    Map.Entry entry = iterate.next();
                    Integer die = (Integer) entry.getKey();

                    ArrayList<Integer> numSides = (ArrayList<Integer>) entry.getValue();
                    Integer numSidesLength = numSides.size();
                    if (die == -1) {
                        //System.out.println("PRtot: " + numSides);
                        tot = numSides.get(0);
                    } else if (die == 0) {
                        mod = numSides.get(0);
                    } else if (die == 1) {
                        allRolls.append("d%");
                    } else {
                        allRolls.append(numSidesLength.toString()).append("d").append(die.toString());
                        addPlus = true;
                    }

                    if (iterate.hasNext() && addPlus) {
                        allRolls.append(" + ");
                    } else if (mod != 0 && !iterate.hasNext()) {
                        if (mod < 0) {
                            allRolls.append(" - ");
                        } else {
                            allRolls.append(" + ");
                        }
                        allRolls.append(String.valueOf(Math.abs(mod)));
                    }
                }
                if (!forFavList) {
                    allRolls.append(" = ").append(tot.toString());
                }
            }

            return allRolls.toString();
        }
    }

    // Returns a colored string
    public SpannableStringBuilder toSpanString() {
        SpannableStringBuilder allRolls = new SpannableStringBuilder("");

        checkRule();

        // Creates individual string
        Iterator<Map.Entry<Integer, ArrayList<VOI>>> iterate = valuesOfImportance.entrySet().iterator();
        while (iterate.hasNext()) {
            Map.Entry entry = iterate.next();
            Integer die = (Integer) entry.getKey();

            if (die != -1) {

                ArrayList<VOI> numSides = (ArrayList<VOI>) entry.getValue();

                if (die == 0) {
                    allRolls.append("Mod: ");

                } else {
                    allRolls.append("d").append(die.toString()).append(": ");
                }


                Integer numSidesSize = numSides.size();
                for (Integer i = 0; i < numSidesSize; i++) {
                    VOI voi = numSides.get(i);
                    if (voi.getColor() != 0) {
                        ForegroundColorSpan fcs = new ForegroundColorSpan(voi.getColor());
                        int start = allRolls.length();
                        allRolls.append(voi.toString());

                        int offset = 1;

                        if (i < numSidesSize - 1) {
                            allRolls.append(", ");
                            offset = 2;
                        } else {
                            allRolls.append(" ");
                        }


                        allRolls.setSpan(fcs, start, allRolls.length() - offset,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    } else {
                        allRolls.append(numSides.get(i).toString());

                        if (i < numSidesSize - 1) {
                            allRolls.append(", ");
                        } else {
                            allRolls.append(" ");
                        }
                    }


                }

                if (iterate.hasNext()) {
                    allRolls.append("| ");
                }
            }
        }

        //System.out.println("PR| allRolls: " + allRolls);

        return allRolls;
    }

    // Sets boolean for viewing individual rolls or additive rolls
    public void setViewState(boolean state) {
        viewIndividual = state;
    }

    // Gets boolean for viewing individual rolls or additive rolls
    // Returns true if user wants to view Individual rolls
    public boolean getViewState() {
        return viewIndividual;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer id) {
        ID = id;
    }

    // Sets boolean for this as a favorite
    public void setFave(boolean state) {
        isFave = state;
    }

    // Gets boolean for this as a favorite
    public boolean getFave() {
        return isFave;
    }

    // Re-rolls any ones in the roll hashmap
    public void rerollOnes() {
        Random random = new Random();
        HashMap<Integer, ArrayList<Integer>> onesPositions = new HashMap<>();

        // Gets position of ones
        for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
            Integer die = entry.getKey();

            ArrayList<Integer> rolls = entry.getValue();
            int i = 0;
            if (die > 1) {
                for (int result : rolls) {
                    if (result == 1) {
                        if (onesPositions.get(die) == null) {
                            onesPositions.put(die, new ArrayList<Integer>());
                        }
                        onesPositions.get(die).add(i);

                        //System.out.println("PR| ones: " + onesPositions);
                    }
                    i++;
                }
            }
        }

        // Replaces ones
        for (Map.Entry<Integer, ArrayList<Integer>> entry : onesPositions.entrySet()) {
            Integer die = entry.getKey();
            ArrayList<Integer> pos = entry.getValue();
            int result = 0;

            for (Integer space : pos) {
                while (result <= 1) {
                    random.setSeed(random.nextLong());
                    result = random.nextInt(die) + 1;
                }

                int spot = space;
                roll.get(die).remove(spot);
                roll.get(die).add(spot, result);
            }
        }

        total();
    }

    // Removes all the lowest die result for every separate die
    // returns false if all rolls are gone
    public boolean removeAllLowest() {
        HashMap<Integer, ArrayList<Integer>> lowestPositions = new HashMap<>();


        // Gets position of lowest
        for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
            Integer die = entry.getKey();
            int lowest = 0;

            ArrayList<Integer> rolls = entry.getValue();
            int i = 0;
            if (die >= 1) {
                for (int result : rolls) {
                    // Initializes lowest to the first number
                    if(lowest == 0) {
                        lowest = result;
                    }

                    // If a new lowest die is found
                    if (result < lowest) {
                        lowest = result;
                        lowestPositions.put(die, new ArrayList<Integer>());
                        lowestPositions.get(die).add(i);

                    // If a lowest die is found
                    } else if (result == lowest) {
                        if (lowestPositions.get(die) == null) {
                            lowestPositions.put(die, new ArrayList<Integer>());
                        }
                        lowestPositions.get(die).add(i);
                        //System.out.println("PR| ones: " + onesPositions);
                    }
                    i++;
                }
            }
        }

        // Remove lowest
        for (Map.Entry<Integer, ArrayList<Integer>> entry : lowestPositions.entrySet()) {
            Integer die = entry.getKey();
            ArrayList<Integer> pos = entry.getValue();
            int spacesRemoved = 0;

            for (Integer space : pos) {
                int spot = space - spacesRemoved;
                roll.get(die).remove(spot);
                spacesRemoved++;
            }

            // If the die has all of its rolls removed, remove die
            if(roll.get(die).size() == 0) {
                //System.out.println("PR| roll " + roll);
                roll.remove(die);
                //System.out.println("PR| roll " + roll);
            }
        }

        total();
        // Checks if die has anything but modifiers
        return hasValues();
    }

    // Re-rolls all the lowest dice for every separate die
    public void rerollAllLowest() {
        Random random = new Random();
        HashMap<Integer, ArrayList<Integer>> lowestPositions = new HashMap<>();


        // Gets position of lowest
        for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
            Integer die = entry.getKey();
            int lowest = 0;

            ArrayList<Integer> rolls = entry.getValue();
            int i = 0;
            if (die >= 1) {
                for (int result : rolls) {
                    // Initializes lowest to the first number
                    if(lowest == 0) {
                        lowest = result;
                    }

                    // If a new lowest die is found
                    if (result < lowest) {
                        lowest = result;
                        lowestPositions.put(die, new ArrayList<Integer>());
                        lowestPositions.get(die).add(i);

                        // If a lowest die is found
                    } else if (result == lowest) {
                        if (lowestPositions.get(die) == null) {
                            lowestPositions.put(die, new ArrayList<Integer>());
                        }
                        lowestPositions.get(die).add(i);
                        //System.out.println("PR| ones: " + onesPositions);
                    }
                    i++;
                }
            }
        }

        // Replaces lowest
        for (Map.Entry<Integer, ArrayList<Integer>> entry : lowestPositions.entrySet()) {
            Integer die = entry.getKey();
            ArrayList<Integer> pos = entry.getValue();
            int result;

            for (Integer space : pos) {

                random.setSeed(random.nextLong());
                result = random.nextInt(die) + 1;

                int spot = space;
                roll.get(die).remove(spot);
                roll.get(die).add(spot, result);
            }
        }

        total();
    }

    // Removes one lowest die result for every separate die
    // returns false if all rolls are gone
    public boolean removeOneLowest() {
        HashMap<Integer, ArrayList<Integer>> lowestPositions = new HashMap<>();
        ArrayList<Integer> toBeRemoved = new ArrayList<>();


        // Gets position of lowest
        for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
            Integer die = entry.getKey();
            int lowest = 0;

            ArrayList<Integer> rolls = entry.getValue();
            int i = 0;
            if (die >= 1) {
                for (int result : rolls) {
                    // Initializes lowest to the first number
                    if(lowest == 0) {
                        lowest = result;
                    }

                    // If a new lowest die is found
                    if (result <= lowest) {
                        lowest = result;
                        lowestPositions.put(die, new ArrayList<Integer>());
                        lowestPositions.get(die).add(i);

                    }
                    i++;
                }
            }
        }

        // Remove lowest
        for (Map.Entry<Integer, ArrayList<Integer>> entry : lowestPositions.entrySet()) {
            Integer die = entry.getKey();
            ArrayList<Integer> pos = entry.getValue();
            int spacesRemoved = 0;

            if(roll.get(die).size() == 1) {
                toBeRemoved.add(die);
            } else {
                for (Integer space : pos) {
                    int spot = space - spacesRemoved;
                    roll.get(die).remove(spot);
                    spacesRemoved++;
                }
            }
        }

        // Check if die will have rolls left if all dice toberemoved are gone
        for (Map.Entry<Integer, ArrayList<Integer>> entry : lowestPositions.entrySet()) {
            Integer die = entry.getKey();

            if (die > 0) {
                if(!toBeRemoved.contains(die)) {
                    for(Integer x : toBeRemoved) {
                        roll.remove(x);
                    }
                    total();
                    return true;
                }
            }

        }

        total();
        return false;
    }

    // Checks if die has anything but modifiers
    public boolean hasValues() {
        for(Integer i = 1; i <= 100; i++) {
            if(roll.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    // Re-calculates the total
    public void total() {
        int total = 0;
        for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
            Integer die = entry.getKey();
            ArrayList<Integer> numSides = entry.getValue();

            for (int result : numSides) {
                if (die >= 0) {
                    total += result;
                }
            }
        }
        setTotal(total);
    }


    //------------- Favorites -------------//

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setForFavList(boolean state) {
        forFavList = state;
    }

    public boolean getForFavList() {
        return forFavList;
    }

    // Returns string for a favList item  // {space=[numSides, numDice]}
    public String toFavString() {
        StringBuilder string = new StringBuilder("");
        boolean plus = false;

        for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
            ArrayList<Integer> sidesAndDie = entry.getValue();

            Integer numSides = sidesAndDie.get(0);
            Integer numDice = sidesAndDie.get(1);

            // This allows for a negative from modifiers to be 2d6 - 4 instead of 2d6 + (-4)
            if (numSides != 0 && plus) {
                string.append(" + ");
            }

            if (numSides == 0) {
                if (numDice == 0) {
                    // Do nothing
                    //break; //I think
                } else if (numDice < 0 && plus) {
                    string.append(" - ");
                } else if (numDice < 0) {
                    string.append("-");
                } else if (plus) {
                    string.append(" + ");
                }
                string.append(Math.abs(numDice));
            } else if (numSides == 1) {
                string.append(numDice).append("d%");
            } else {
                string.append(numDice).append("d").append(numSides);
            }

            plus = true;
        }

        return string.toString();
    }

    public ArrayList<Integer> getValues(int space) {
        return roll.get(space);
    }

    // Check if only mod so the user cannot favRoll mods
    // Returns true if isOnlyMod, false if it has dice
    public boolean checkOnlyMod() {
        if(isFave) {
            for(Integer i = 0; i <= 6; i++) {
                // iterates through all spaces
                if(roll.get(i) != null) {
                    // if a die is found return false
                    if(roll.get(i).get(0) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public ArrayList<Integer> getSpace(Integer space) {
            return roll.get(space);
    }

    public void removeSpace(Integer space) {
        try {
            roll.remove(space);
        } catch(UnsupportedOperationException e) {
            System.out.println("PR| removeSpace");
            e.printStackTrace();
        }
    }


    //------------- Highlighting & Rules ----------------//


    // Creates  the values of importance map
    private void checkRule() {
        // Sample rule
        /*rules = new ArrayList<>();
        Rule rule = new Rule(0xFF00FF00, 1, 0, 101);
        Rule rule2 = new Rule(0xFFFF0000, 0, 0, 101);
        Rule rule3 = new Rule(0xFFFFF647, 2, 4, 6);
        rules.add(rule);
        rules.add(rule2);
        rules.add(rule3);*/

        valuesOfImportance.clear();

        // For blank rules we still need to fill vOI
        if(rules.isEmpty()) {

            for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
                Integer die = entry.getKey();

                if (die >= 0) {

                    ArrayList<Integer> results = entry.getValue();
                    int numSidesSize = results.size();

                    if (valuesOfImportance.get(die) == null) {
                        valuesOfImportance.put(die, new ArrayList<VOI>(numSidesSize));
                    }

                    // Create voi from Integers and add them to the vOI array list
                    for (Integer i = 0; i < numSidesSize; i++) {
                        VOI voi = new VOI(results.get(i));
                        valuesOfImportance.get(die).add(voi);
                    }

                }
            }
        }

        for (Rule x : rules) {


            // Least
            if (x.greaterLess == 0) {

                for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
                    Integer die = entry.getKey();

                    if (die > 0) {

                        ArrayList<Integer> results = entry.getValue();
                        Integer numSidesSize = results.size();

                        // Set integer to highest possible value
                        Integer less = x.upperValue;

                        // Gets the lowest number
                        for (Integer i : results) {
                            if (i < less) {
                                less = i;
                            }
                        }

                        //Checks the lowest number positions
                        for (Integer i = 0; i < numSidesSize; i++) {
                            // If the die does exist check if the result has been made
                            if (valuesOfImportance.get(die) != null) {
                                //
                                try {
                                    // If a previous rule is not there add one
                                    if (valuesOfImportance.get(die).get(i).getColor().equals(0)) {
                                        if (results.get(i).equals(less)) {
                                            VOI voi = new VOI(results.get(i), x.color);
                                            int old = i;
                                            valuesOfImportance.get(die).add(old,voi);
                                            valuesOfImportance.get(die).remove(old + 1);
                                        }
                                    }
                                // If no result exists create one
                                } catch (IndexOutOfBoundsException e) {
                                    VOI voi;
                                    if (results.get(i).equals(less)) {
                                        voi = new VOI(results.get(i), x.color);
                                    } else {
                                        voi = new VOI(results.get(i));
                                    }
                                    valuesOfImportance.get(die).add(voi);
                                }

                            // If the die doesn't exist create it and add a result
                            } else {
                                valuesOfImportance.put(die, new ArrayList<VOI>());
                                VOI voi;
                                if (results.get(i).equals(less)) {
                                    voi = new VOI(results.get(i), x.color);
                                } else {
                                    voi = new VOI(results.get(i));
                                }
                                valuesOfImportance.get(die).add(voi);
                            }
                        } //for

                    } else if (die == 0) {

                        ArrayList<Integer> results = entry.getValue();
                        int numSidesSize = results.size();

                        if (valuesOfImportance.get(die) == null) {
                            valuesOfImportance.put(die, new ArrayList<VOI>(numSidesSize));
                        }

                        // Create voi from Integers and add them to the vOI array list
                        for (Integer i = 0; i < numSidesSize; i++) {
                            VOI voi = new VOI(results.get(i));
                            valuesOfImportance.get(die).add(voi);
                        }
                    }
                }

            // Greatest
            } else if (x.greaterLess == 1) {

                for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
                    Integer die = entry.getKey();

                    if (die > 0) {

                        ArrayList<Integer> results = entry.getValue();
                        Integer numSidesSize = results.size();

                        // Set integer to lowest possible value
                        Integer more = x.lowerValue;

                        // Gets the highest number
                        for (Integer i : results) {
                            if (i > more) {
                                more = i;
                            }
                        }

                        //Checks the lowest number positions
                        for (Integer i = 0; i < numSidesSize; i++) {
                            // If the die does exist check if the result has been made
                            if (valuesOfImportance.get(die) != null) {
                                //
                                try {
                                    // If a previous rule is not there add one
                                    if (valuesOfImportance.get(die).get(i).getColor().equals(0)) {
                                        if (results.get(i).equals(more)) {
                                            VOI voi = new VOI(results.get(i), x.color);
                                            int old = i;
                                            valuesOfImportance.get(die).add(old,voi);
                                            valuesOfImportance.get(die).remove(old + 1);
                                        }
                                    }
                                    // If no result exists create one
                                } catch (IndexOutOfBoundsException e) {
                                    VOI voi;
                                    if (results.get(i).equals(more)) {
                                        voi = new VOI(results.get(i), x.color);
                                    } else {
                                        voi = new VOI(results.get(i));
                                    }
                                    valuesOfImportance.get(die).add(voi);
                                }

                                // If the die doesn't exist create it and add a result
                            } else {
                                valuesOfImportance.put(die, new ArrayList<VOI>());
                                VOI voi;
                                if (results.get(i).equals(more)) {
                                    voi = new VOI(results.get(i), x.color);
                                } else {
                                    voi = new VOI(results.get(i));
                                }
                                valuesOfImportance.get(die).add(voi);
                            }
                        } //for
                    } else if (die == 0) {

                        ArrayList<Integer> results = entry.getValue();
                        int numSidesSize = results.size();

                        if (valuesOfImportance.get(die) == null) {
                            valuesOfImportance.put(die, new ArrayList<VOI>(numSidesSize));
                        }

                        // Create voi from Integers and add them to the vOI array list
                        for (Integer i = 0; i < numSidesSize; i++) {
                            VOI voi = new VOI(results.get(i));
                            valuesOfImportance.get(die).add(voi);
                        }
                    }
                }

            // a < x < b
            } else if (x.greaterLess == 2) {
                for (Map.Entry<Integer, ArrayList<Integer>> entry : roll.entrySet()) {
                    Integer die = entry.getKey();

                    if (die > 0) {

                        ArrayList<Integer> results = entry.getValue();
                        Integer numSidesSize = results.size();

                        //Checks the lowest number positions
                        for (Integer i = 0; i < numSidesSize; i++) {
                            // If the die does exist check if the result has been made
                            if (valuesOfImportance.get(die) != null) {
                                //
                                try {
                                    // If a previous rule is not there add one
                                    if (valuesOfImportance.get(die).get(i).getColor().equals(0)) {
                                        if (results.get(i) > x.lowerValue &&
                                                results.get(i) < x.upperValue) {

                                            VOI voi = new VOI(results.get(i), x.color);
                                            int old = i;
                                            valuesOfImportance.get(die).add(old,voi);
                                            valuesOfImportance.get(die).remove(old + 1);
                                        }
                                    }
                                    // If no result exists create one
                                } catch (IndexOutOfBoundsException e) {
                                    VOI voi;
                                    if (results.get(i) > x.lowerValue &&
                                            results.get(i) < x.upperValue) {

                                        voi = new VOI(results.get(i), x.color);
                                    } else {
                                        voi = new VOI(results.get(i));
                                    }
                                    valuesOfImportance.get(die).add(voi);
                                }

                                // If the die doesn't exist create it and add a result
                            } else {
                                valuesOfImportance.put(die, new ArrayList<VOI>());
                                VOI voi;
                                if (results.get(i) > x.lowerValue &&
                                        results.get(i) < x.upperValue) {

                                    voi = new VOI(results.get(i), x.color);
                                } else {
                                    voi = new VOI(results.get(i));
                                }
                                valuesOfImportance.get(die).add(voi);
                            }
                        } //for
                    } else if (die == 0) {

                        ArrayList<Integer> results = entry.getValue();
                        int numSidesSize = results.size();

                        if (valuesOfImportance.get(die) == null) {
                            valuesOfImportance.put(die, new ArrayList<VOI>(numSidesSize));
                        }

                        // Create voi from Integers and add them to the vOI array list
                        for (Integer i = 0; i < numSidesSize; i++) {
                            VOI voi = new VOI(results.get(i));
                            valuesOfImportance.get(die).add(voi);
                        }
                    }

                }
            }

        }
        //System.out.println("PR| valuesOfImportance: " + valuesOfImportance);

    }

    public void addHighlight(int color, int greaterLess, int lowerValue, int upperValue) {
        Rule rule = new Rule(color, greaterLess, lowerValue, upperValue);

        rules.add(rule);
    }

    public void setRules(ArrayList<Rule> rules) {
        this.rules = rules;
    }

    public ArrayList<Rule> getAllRules() {
        return rules;
    }

    // Allows for simple creation of rules for highlighting
    public class Rule {
        int color;
        int greaterLess; // 0 for least, 1 for greatest, 2 for a < x < b
        int upperValue;
        int lowerValue;

        public Rule(int color, int greaterLess, int lowerValue, int upperValue) {
            this.color = color;
            this.greaterLess = greaterLess;
            this.upperValue = upperValue;
            this.lowerValue = lowerValue;
        }

    }

    // Object for valuesOfImportance. Contains a Color and an Integer
    public class VOI {
        Integer integer;
        Integer color;

        public VOI(Integer integer) {
            this.integer = integer;
            color = 0;
        }

        public VOI (Integer integer, Integer color) {
            this.integer = integer;
            this.color = color;
        }

        public Integer getColor() {
            return color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }

        public Integer getInteger() {
            return integer;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        @Override
        public String toString() {
            return integer.toString();
        }
    }

    // Adds automatically applied modifiers
    // 0 : reroll 1's
    // 1 : remove lowest
    // 2 : reroll all lowest
    public void addModificationRule(Integer rule) {
        modifications.add(rule);
        //System.out.println("PR| just because");
    }

    public void removeModificationRule(Integer rule) {
        modifications.remove(rule);
    }

    public void setModifications(ArrayList<Integer> modifications) {
        this.modifications = modifications;
    }

    public ArrayList<Integer> getModifications() {
        return modifications;
    }
}