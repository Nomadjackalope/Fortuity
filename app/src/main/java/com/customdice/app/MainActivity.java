package com.customdice.app;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.customdice.app.tutorial.Placement;
/*import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;*/
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.splunk.mint.Mint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends FragmentActivity
        implements InterCom, View.OnClickListener {

    final static int MOVE_DURATION = 150;
    public static final String PREFS_NAME = "MyPrefsFile";

    final Context context = this;

    RollFragAdapter rollFragAdapter;
    HistFragAdapter histAdapter;

    ViewPager listPager;
    ViewPager dicePager;

    private ArrayList<PreviousRoll> diceHistories;
    private ArrayList<PreviousRoll> favorites;
    private PreviousRoll deleted;
    private int deletedPosition;

    private Button okay;
    private Button nothanks;
    private RelativeLayout introTutorial;

    Integer space;

    private ImageView settingsButton;

    private boolean individualDisplaySetting = false;
    private boolean diceDisplaySetting = false;

    private boolean opened;

    private UndoView undoView;

    //Handler adDelay;
    //AdView adView;

    Placement placement;
    int counter = 0;
    TextView demoText;

    String version;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mint.initAndStartSession(this, "d1fd6001");

        setContentView(R.layout.activity_main);

        diceHistories = new ArrayList<>();
        favorites = new ArrayList<>();

        rollFragAdapter = new RollFragAdapter(getSupportFragmentManager());
        histAdapter = new HistFragAdapter(getSupportFragmentManager());

        dicePager = (ViewPager) findViewById(R.id.dicePager);
        listPager = (ViewPager) findViewById(R.id.listPager);


        if (dicePager != null) {
            dicePager.setAdapter(rollFragAdapter);
            dicePager.setOffscreenPageLimit(2);
        } else {
            if (getSupportFragmentManager().findFragmentByTag("favRoller") == null &&
                    getSupportFragmentManager().findFragmentByTag("settings") == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fav_roller, FavRollFragment.newInstance(), "favRoller").commit();
            }
            if (getSupportFragmentManager().findFragmentByTag("diceRoller") == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.dice_roller, DiceRollFragment.newInstance(), "diceRoller").commit();
            }
        }
        if (listPager != null) {
            listPager.setAdapter(histAdapter);
        } else {
            if (getSupportFragmentManager().findFragmentByTag("diceHist") == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.dice_hist, DiceHistFragment.newInstance(0), "diceHist").commit();
            }
            if (getSupportFragmentManager().findFragmentByTag("favHist") == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fav_hist, FavHistFragment.newInstance(0), "favHist").commit();
            }
        }

        FileInitializer fileInitializer = new FileInitializer();
        fileInitializer.execute();

        if (space == null) {
            space = -1;
        }

        //adDelay = new Handler();

        // Settings from preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        opened = settings.getBoolean("opened", false);
        individualDisplaySetting = settings.getBoolean("individual", false);
        diceDisplaySetting = settings.getBoolean("dice", false);
        version = settings.getString("version", "1.6.2");

        if (getResources().getString(R.string.selected_configuration).equals("xlarge")
                || getResources().getString(R.string.selected_configuration).equals("xlarge-land")) {
            settingsButton = (ImageView) findViewById(R.id.settings);
            settingsButton.setOnClickListener(this);
            //System.out.println("MA| Settings button initialized");
        }

        okay = (Button) findViewById(R.id.okay);
        nothanks = (Button) findViewById(R.id.nothanks);
        introTutorial = (RelativeLayout) findViewById(R.id.intotut);

        demoText = (TextView) findViewById(R.id.demo_text);

        okay.setOnClickListener(this);
        nothanks.setOnClickListener(this);
        introTutorial.setOnClickListener(this);

//        System.out.println("MA| opened: " + opened);
//        System.out.println("MA| counter: " + counter);

        undoView = new UndoView(findViewById(R.id.undobar), this);

        checkTut();

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String thisVersion = pInfo.versionName;

            if(!thisVersion.equals(version)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(R.string.update_title);
                dialog.setMessage(R.string.update_message);
                dialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Just exit
                    }
                });
                dialog.create().show();
            }

            version = thisVersion;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPause() {
        /*if (adView != null) {
            adView.pause();
        }*/
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (adView != null) {
            adView.resume();
        }*/
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        space = savedInstanceState.getInt("space");
//        counter = savedInstanceState.getInt("count");
//        System.out.println("MA| count restore: " + counter);
        undoView.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("space", space);
//        outState.putInt("count", counter);
//        System.out.println("MA| count save: " + counter);
        super.onSaveInstanceState(outState);
        undoView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        DiceWriter writer = new DiceWriter();
        writer.execute();

        RollWriter rollWriter = new RollWriter();
        rollWriter.execute();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("opened", opened);
        editor.putBoolean("individual", individualDisplaySetting);
        editor.putBoolean("dice", diceDisplaySetting);
        editor.putString("version", version);

        editor.commit();

    }

    @Override
    public void onClick(View v) {
        demoText = (TextView) findViewById(R.id.demo_text);
        //System.out.println("MA| clicked: " + v);
        if (v == okay) {

            introTutorial.setVisibility(View.GONE);
            setupDice();

            runAppropriateTut();

        } else if (v == nothanks) {
            introTutorial.setVisibility(View.GONE);
            //loadAds();
            opened = true;
        } else if (v == settingsButton) {
            //System.out.println("MA| settings clicked");
            toggleSettings();
        } else if (v == demoText) {
            runAppropriateTut();
        }
    }


    //-------------------------------------- Intro Demo -----------------------------------//

    // Finds what configuration the user's device is and runs nextSlide or nextSlideLarge
    private void runAppropriateTut() {
        if (getResources().getString(R.string.selected_configuration).equals("xlarge")
                || getResources().getString(R.string.selected_configuration).equals("xlarge-land")) {
            slideLarge();

        } else {
            slide();
        }
    }

    private void setupDice() {
        PreviousRoll previousRoll = new PreviousRoll();
        previousRoll.addRoll(6, 5);
        previousRoll.addRoll(6, 1);
        previousRoll.addRoll(8, 4);
        previousRoll.addRoll(0, 4);
        rollAdd(previousRoll, false);

        notifyDiceHist();
    }

    private void setupFavorite() {
        PreviousRoll previousRoll = new PreviousRoll();
        previousRoll.addRoll(6, 5);
        previousRoll.addRoll(6, 1);
        previousRoll.addRoll(8, 4);
        previousRoll.addRoll(0, 4);
        rollAdd(previousRoll, false);

        notifyDiceHist();

        uploadNewFav(0);
    }

    /*private void loadAds() {
        adDelay.postDelayed(new Runnable() {
            @Override
            public void run() {
                adView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice("37C4A4F8CD31591E228D78B20B0BBB52")
                        .addTestDevice("F77FA552ECD82309796085A5AF6B5487")
                        .addTestDevice("F706797568E14108ED99D5CF384B6137")
                                .build();

                adView.loadAd(adRequest);
            }
        }, getResources().getInteger(R.integer.ad_delay));
    }*/

    // Supposed to allow for rotation to bring tut back to the right count
    public void initPlacement() {
        placement = new Placement.Builder(this)
                .setOnClickListener(this)
                .setText("Here you can quickly roll common dice. Click on the d8 to roll one.")
                .setTarget(findViewById(R.id.bonus))
                .build();

        runAppropriateTut();
    }

    // This is the switch for the introduction tutorial
    private void slide() {

        switch (counter) {
            case 0:

                dicePager.setCurrentItem(0, false);
                listPager.setCurrentItem(0, false);

                placement = new Placement.Builder(this)
                        .setOnClickListener(this)
                        .setText("Here you can quickly roll common dice. Click on the d8 to roll one.")
                        .setTarget(findViewById(R.id.dicePager))
                        .build();
                break;
            case 1:
                // Negative modifier

                placement.setText("To add a modifier type a number in the modifier area. Negative values are accepted.");
                placement.setTarget(findViewById(R.id.bonus));

                break;
            case 2:
                // Clearing rolls

                placement.setText("Click the total to clear the rolled dice.");
                placement.setTarget(findViewById(R.id.total));
                if(diceHistories.isEmpty()) {
                    setupDice();
                }

                break;
            case 3:
                // Change to dice history

                placement.setText("These are the previous rolls. Click on the summation to see the individual rolls.");
                try {
                    if (diceHistories.isEmpty()) {
                        setupDice();
                    }
                    if (getDiceHistories().get(0).getFave()) {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                    } else {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                    }

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.listPager));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (getDiceHistories().get(0).getFave()) {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                                } else {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                                }

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;
            case 4:
                // Quick create favorite

                placement.setText("Click these dots to bring up a menu. Select Create a favorite to make a favorite from the rolled dice.");
                try {
                    if (diceHistories.isEmpty()) {
                        setupDice();
                    }

                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.drop_down));


                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.listPager));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.drop_down));

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;
            case 5:
                // Change to Custom roller
                dicePager.setCurrentItem(1);

                placement.setText("Favorites are added to here. This is the Custom Roller. There are 7 spaces for differently sided dice. From here you can create favorites for use later.");
                placement.setTarget(findViewById(R.id.dicePager));

                break;
            case 6:
                // Roll

                placement.setText("Click on Roll dice to roll the current set of dice in the custom roller.");
                placement.setTarget(findViewById(R.id.custom_roll));

                break;
            case 7:
                // Long press to add roll

                placement.setText("Long click on Roll dice to add a roll to the previous roll. Use this in games like Pig or combining custom dice.");
                placement.setTarget(findViewById(R.id.custom_roll));

                break;

            case 8:
                // Rerolling a favorite is easy!

                placement.setText("Long clicking a favorite will reroll it. Try it now!");
                try {
                    if (diceHistories.isEmpty()) {
                        setupDice();
                    }
                    if (getDiceHistories().get(0).getFave()) {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                    } else {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                    }

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.listPager));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (getDiceHistories().get(0).getFave()) {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                                } else {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                                }

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;

            case 9:
                // Click total to clear

                placement.setText("Clicking here will clear the roller in order to make a new favorite.");
                placement.setTarget(findViewById(R.id.fav_total));

                if(favorites.isEmpty()) {
                    PreviousRoll favRoll = new PreviousRoll(true);
                    favRoll.addFavRoll(2, 13, 5);
                    favRoll.addFavRoll(3, 4, 2);
                    favRoll.setName("Magic Missle");
                    favRoll.setColor(getResources().getColor(R.color.green));
                    favorites.add(favRoll);

                    notifyFavHist();
                }

                break;
            case 10:
                // Change to Favorites list
                listPager.setCurrentItem(1);


                placement.setText("Clicking on a favorite will upload it to the roller where you can edit it and roll it." +
                        " \n\nThank you for using Fortuity!" +
                        "\n\nMay the odds be ever in your favor.");
                try {
                    if(favorites.isEmpty()) {
                        PreviousRoll favRoll = new PreviousRoll(true);
                        favRoll.addFavRoll(2, 13, 5);
                        favRoll.addFavRoll(3, 4, 2);
                        favRoll.setName("Magic Missle");
                        favRoll.setColor(getResources().getColor(R.color.green));
                        favorites.add(favRoll);

                        notifyFavHist();
                    }
                    placement.setTarget(getFavListView().getChildAt(0).findViewById(R.id.favhistorytext));

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.listPager));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                placement.setTarget(getFavListView().getChildAt(0).findViewById(R.id.favhistorytext));

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;
            case 11:
                placement.end();
                opened = true;
                //loadAds();

                break;
        }

        counter++;

    }

    // The switch for the layout_large tutorial
    private void slideLarge() {

        if(!getSupportFragmentManager().findFragmentByTag("favRoller").isVisible()) {
            toggleSettings();
        }

        switch (counter) {

            case 0:

                placement = new Placement.Builder(this)
                        .setOnClickListener(this)
                        .setText("Here you can quickly roll common dice. Click on the d8 to roll one.")
                        .setTarget(findViewById(R.id.dice_roller))
                        .build();
                break;
            case 1:
                // Negative modifier

                placement.setText("To add a modifier type a number in the modifier area. Negative values are accepted.");
                placement.setTarget(findViewById(R.id.bonus));

                break;
            case 2:
                // Clearing rolls

                placement.setText("Click the total to clear the rolled dice.");
                placement.setTarget(findViewById(R.id.total));

                break;
            case 3:
                // Change to dice history


                placement.setText("These are the previous rolls. Click on the summation to see the individual rolls.");
                try {
                    if (diceHistories.isEmpty()) {
                        setupDice();
                    }
                    if (getDiceHistories().get(0).getFave()) {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                    } else {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                    }

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.dice_hist));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (getDiceHistories().get(0).getFave()) {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                                } else {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                                }

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;
            case 4:
                // Quick create favorite

                placement.setText("Click these dots to bring up a menu. Select Create a favorite to make a favorite from the rolled dice.");
                try {
                    if (diceHistories.isEmpty()) {
                        setupDice();
                    }
                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.drop_down));

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.dice_hist));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.drop_down));

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;
            case 5:
                // Change to Custom roller

                placement.setText("The favorite was added to here. This is the Custom Roller. There are 7 spaces for differently sided dice. From here you can create favorites for use later.");
                placement.setTarget(findViewById(R.id.fav_roller));

                break;
            case 6:
                // Roll

                placement.setText("Click on Roll dice to roll the current set of dice in the custom roller.");
                placement.setTarget(findViewById(R.id.custom_roll));

                break;
            case 7:
                // Long press to add roll

                placement.setText("Long click on Roll dice to add a roll to the previous roll. Use this in games like Pig or combining custom dice.");
                placement.setTarget(findViewById(R.id.custom_roll));

                break;

            case 8:
                // Rerolling a favorite is easy!

                placement.setText("Long clicking a favorite will reroll it. Try it now!");
                try {
                    if (diceHistories.isEmpty()) {
                        setupDice();
                    }
                    if (getDiceHistories().get(0).getFave()) {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                    } else {
                        placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                    }

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.dice_hist));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (getDiceHistories().get(0).getFave()) {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.favhistorytext));
                                } else {
                                    placement.setTarget(getDiceListView().getChildAt(0).findViewById(R.id.historytext));
                                }

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;

            case 9:
                // Click total to clear

                placement.setText("Clicking here will clear the roller in order to make a new favorite.");
                placement.setTarget(findViewById(R.id.fav_total));

                if(favorites.isEmpty()) {
                    PreviousRoll favRoll = new PreviousRoll(true);
                    favRoll.addFavRoll(2, 13, 5);
                    favRoll.addFavRoll(3, 4, 2);
                    favRoll.setName("Magic Missle");
                    favRoll.setColor(getResources().getColor(R.color.green));
                    favorites.add(favRoll);

                    notifyFavHist();
                }

                break;
            case 10:
                // Change to Favorites list

                if(favorites.isEmpty()) {
                    PreviousRoll favRoll = new PreviousRoll(true);
                    favRoll.addFavRoll(2, 13, 5);
                    favRoll.addFavRoll(3, 4, 2);
                    favRoll.setName("Magic Missle");
                    favRoll.setColor(getResources().getColor(R.color.green));
                    favorites.add(favRoll);

                    notifyFavHist();
                }
                placement.setText("Clicking on a favorite will upload it to the roller where you can edit it and roll it." +
                        " \n\nThank you for using Fortuity!" +
                        "\n\nMay the odds be ever in your favor.");
                try {
                    if(favorites.isEmpty()) {
                        PreviousRoll favRoll = new PreviousRoll(true);
                        favRoll.addFavRoll(2, 13, 5);
                        favRoll.addFavRoll(3, 4, 2);
                        favRoll.setName("Magic Missle");
                        favRoll.setColor(getResources().getColor(R.color.green));
                        favorites.add(favRoll);

                        notifyFavHist();
                    }
                    placement.setTarget(getFavListView().getChildAt(0).findViewById(R.id.favhistorytext));

                } catch (NullPointerException e) {
                    placement.setTarget(findViewById(R.id.fav_hist));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                placement.setTarget(getFavListView().getChildAt(0).findViewById(R.id.favhistorytext));

                            } catch (NullPointerException npe) {
                                System.out.println("Could not find a dice history item");
                            }
                        }
                    }, 100);

                }

                break;
            case 11:
                placement.end();
                opened = true;
                //loadAds();

                break;
        }

        counter++;

    }

    // Loads ads and removes intro tut view if app has been opened
    private void checkTut() {
        if(opened) {
            introTutorial.setVisibility(View.GONE);
            //loadAds();
        }
    }


    //------------------------------------- AsyncTasks -------------------------------------//

    // Favorites writer
    private class DiceWriter extends AsyncTask<Void, Void, String> {

        FileOutputStream outputStream;

        protected String doInBackground(Void... params) {
            try {
                outputStream = openFileOutput("dice.txt", MainActivity.MODE_PRIVATE);
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream, "UTF-8");

                // Gets the list of favorites to write, gets the size, makes a temp prevRoll
                int favSize = favorites.size();
                PreviousRoll prevRoll;

                // This is the array that will store the Dice objects which have been converted from
                //      from PreviousRolls
                Dice[] arr = new Dice[favSize];

                // For the whole of the favorites list
                for (int i = 0; i < favSize; i++) {
                    // Make a new Dice object
                    Dice die = new Dice();

                    // Does not let this Dice object get written
                    boolean hasValues = false;

                    // Gets the first prevRoll from favorites and takes the name to the Dice
                    prevRoll = favorites.get(i);
                    die.name = prevRoll.getName();
                    die.color = prevRoll.getColor();


                    // Filling all the highlighting rules
                    if (!prevRoll.getAllRules().isEmpty()) {
                        die.initHighlightArrays(prevRoll.getAllRules().size());
                    }

                    int j = 0;
                    for(PreviousRoll.Rule rule : prevRoll.getAllRules()) {
                        die.colorHighlight[j] = rule.color;
                        die.greaterLess[j] = rule.greaterLess;
                        die.upperValue[j] = rule.upperValue;
                        die.lowerValue[j] = rule.lowerValue;

                        j++;
                    }

                    // Initializing modification array
                    if (!prevRoll.getModifications().isEmpty()) {
                        die.initRuleArray(prevRoll.getModifications().size());
                    }

                    // Adding modification rules
                    for(int x = 0; x < prevRoll.getModifications().size(); x++) {
                        die.rules[x] = prevRoll.getModifications().get(x);
                    }

                    // For all the spaces
                    for (int x = 0; x < 6; x++) {
                        // sidesAndDice stores the [sides, dice] array
                        ArrayList<Integer> sidesAndDice = prevRoll.getValues(x);
                        //System.out.println("MA| sidesAndDice: " + sidesAndDice);

                        // As long as the array is not null
                        if (sidesAndDice != null) {

                            // initializes the variable in Dice[] only if it will have values
                            // this avoids a die being loaded with bonuses of 0 in every space
                            die.init(x);

                            // at the correct space this adds the [sides, dice]
                            int sides = sidesAndDice.get(0);
                            int dice = sidesAndDice.get(1);
                            die.addDie(x, sides, dice);

                            // Because the Dice is not going to be null this allows it to be written
                            hasValues = true;
                        }
                    }
                    // If at least one space has non-null values the Dice will be added to the array
                    if (hasValues) {
                        arr[i] = die;
                    }
                }

                // Gson converts the Dice object to a JSON object and write it to the file
                Gson gson = new Gson();
                //System.out.println(gson.toJson(arr));
                gson.toJson(arr, outputWriter);

                outputWriter.close();

            } catch (IOException e) {
                System.out.println("MA| stream out failed");
                e.printStackTrace();
            }
            return null;
        }
    }

    // Favorites Reader
    private class DiceFetcher extends AsyncTask<Void, Void, String> {

        FileInputStream stream;

        String string = "Hello world! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! !!!!!!!!! ";
        FileOutputStream out;

        @Override
        protected String doInBackground(Void... params) {

            // Opens or creates a new file, adds a string, and closes the file

            String[] filelist = fileList();
            /*for (String aFilelist : fileList()) {
                System.out.println("MA| filelist: " + aFilelist);
            }*/

            if (filelist.length > 0) {
                try {
                    stream = openFileInput("dice.txt");
                    InputStreamReader inputRead = new InputStreamReader(stream, "UTF-8");

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    Dice[] dices;
                    dices = gson.fromJson(inputRead, Dice[].class);
                    stream.close();

                    //System.out.println("MA| dices[0]: " + dices[0].toString());

                /*for (Dice dice : dices) {
                    System.out.println("MA| reading gson: " + dice.toString());
                }*/

                    if (dices != null) {
                        if (dices.length > 0) {
                            diceToPreviousRoll(dices);
                            handleDicesList();
                        }
                    }

                } catch (FileNotFoundException e) {
                    System.out.println("MA| Dice file not found");
                    failedLoadingDice();
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("MA| Dice read IOexception");
                    failedLoadingDice();
                    e.printStackTrace();
                }
            }

            if (fileList().length < 1) {
                try {

                    //System.out.println("MA| filelist size: " + fileList().length);
                    /*for (String aFilelist : filelist) {
                        System.out.println("MA| filelist: " + aFilelist);
                    }*/
                    out = openFileOutput("dice.txt", MainActivity.MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(out, "UTF-8");
                    //outputWriter.close();

                    // Testing code to provide values for the favorites list
                    /*Dice dice1 = new Dice();
                    Dice dice2 = new Dice();
                    dice1.name = "Magic Missle";
                    dice1.zero = new int[]{4, 3};
                    dice1.one = new int[]{5, 6};
                    dice2.name = "UberBlade";
                    dice2.two = new int[]{2, 5};
                    Dice[] arr = new Dice[2];
                    arr[0] = dice1;
                    arr[1] = dice2;

                    Gson gson = new Gson();
                    System.out.println(gson.toJson(arr));
                    gson.toJson(arr, outputWriter);*/

                    outputWriter.close();

                } catch (IOException e) {
                    System.out.println("MA| stream out failed");
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    // Roll history writer
    private class RollWriter extends AsyncTask<Void, Void, String> {

        FileOutputStream outputStream;

        protected String doInBackground(Void... params) {
            try {
                outputStream = openFileOutput("roll.txt", MainActivity.MODE_PRIVATE);
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream, "UTF-8");

                // Gets the list of favorites to write, gets the size, makes a temp prevRoll
                int diceSize = diceHistories.size();
                PreviousRoll prevRoll;

                // This is the array that will store the Roll objects which have been converted from
                //      from PreviousRolls
                Roll[] arr = new Roll[diceSize];

                // For the whole of the diceHistories list
                for (int i = 0; i < diceSize; i++) {
                    // Make a new Dice object
                    Roll roll = new Roll();

                    // Does not let this Dice object get written
                    boolean hasValues = false;

                    // Gets the prevRoll from diceHistories and takes the name to the Roll
                    prevRoll = diceHistories.get(i);
                    roll.name = prevRoll.getName();
                    roll.color = prevRoll.getColor();
                    roll.total = prevRoll.getTotal();
                    roll.isFav = prevRoll.getFave();
                    roll.viewState = prevRoll.getViewState();

                    // Filling all the highlighting rules
                    if (!prevRoll.getAllRules().isEmpty()) {
                        roll.initHighlightArrays(prevRoll.getAllRules().size());
                    }

                    int j = 0;
                    for(PreviousRoll.Rule rule : prevRoll.getAllRules()) {
                        roll.colorHighlight[j] = rule.color;
                        roll.greaterLess[j] = rule.greaterLess;
                        roll.upperValue[j] = rule.upperValue;
                        roll.lowerValue[j] = rule.lowerValue;

                        j++;
                    }

                    // Initializing modification array
                    if (!prevRoll.getModifications().isEmpty()) {
                        roll.initRuleArray(prevRoll.getModifications().size());
                    }

                    // Adding modification rules
                    for(int x = 0; x < prevRoll.getModifications().size(); x++) {
                        roll.rules[x] = prevRoll.getModifications().get(x);
                    }


                    int x = 0;

                    // For all the dice
                    for (Map.Entry<Integer, ArrayList<Integer>> entry : prevRoll.getPreviousRoll().entrySet()) {
                        Integer die = entry.getKey();

                        if (die >= 0) {
                            // sidesAndDice stores the [sides, dice] array
                            ArrayList<Integer> sidesAndDice = entry.getValue();
                            //System.out.println("MA| sidesAndDice: " + sidesAndDice);

                            // As long as the array is not null
                            if (sidesAndDice != null) {

                                int size = sidesAndDice.size();

                                // initializes the variable in Dice[] only if it will have values
                                // this avoids a roll being loaded with bonuses of 0 in every space
                                roll.init(x, size);

                                // adds the array to a space
                                roll.addDie(x, die, sidesAndDice);

                                // Because the Dice is not going to be null this allows it to be written
                                hasValues = true;
                            }
                            x++;
                        }
                    }
                    // If at least one space has non-null values the Dice will be added to the array
                    if (hasValues) {
                        arr[i] = roll;
                    }
                }

                // Gson converts the Dice object to a JSON object and write it to the file
                Gson gson = new Gson();
                //System.out.println(gson.toJson(arr));
                gson.toJson(arr, outputWriter);

                outputWriter.close();

            }catch(IOException e){
                System.out.println("MA| stream out failed");
                e.printStackTrace();
            }
            return null;
        }
    }

    // Roll history reader
    public class RollFetcher extends AsyncTask<Void, Void, String> {

        FileInputStream stream;

        @Override
        protected String doInBackground(Void... params) {

            // Opens file, adds a string, and closes the file

            try {
                stream = openFileInput("roll.txt");
                InputStreamReader inputRead = new InputStreamReader(stream, "UTF-8");

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Roll[] rolls;
                rolls = gson.fromJson(inputRead, Roll[].class);
                stream.close();

                //System.out.println("MA| dices[0]: " + dices[0].toString());

            /*for (Dice dice : dices) {
                System.out.println("MA| reading gson: " + dice.toString());
            }*/

                if (rolls != null) {
                    if (rolls.length > 0) {
                        rollToPreviousRoll(rolls);
                        handleRollsList();
                    }
                }

            } catch (FileNotFoundException e) {
                System.out.println("MA| Roll file not found");
                failedLoadingRolls();
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("MA| Roll read IOexception");
                failedLoadingRolls();
                e.printStackTrace();
            }

            return null;
        }
    }

    private class FileInitializer extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            FileOutputStream out;

            String[] fileList = fileList();
            /*for(String file : fileList) {
                System.out.print("MA|Files: " + file + ", ");
            }*/

            if(fileList.length == 0) {
                try {

                    out = openFileOutput("dice.txt", MainActivity.MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(out, "UTF-8");

                    outputWriter.close();
                } catch (IOException e) {
                    System.out.println("MA| stream out failed");
                    e.printStackTrace();
                }
                try {

                    out = openFileOutput("roll.txt", MainActivity.MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(out, "UTF-8");

                    outputWriter.close();
                } catch (IOException e) {
                    System.out.println("MA| stream out failed");
                    e.printStackTrace();
                }
            }

            /*String[] fileListToWrite = new String[2];

            for (String file : fileList) {
                if (file.equals("dice.txt")) {
                    fileListToWrite[0] = file;
                } else if (file.equals("roll.txt")) {
                    fileListToWrite[1] = file;
                }
            }

            for (String file : fileListToWrite) {
                if (file != null) {
                    if (!file.equals("dice.txt") || !file.equals("roll.txt")) {
                        try {

                            out = openFileOutput(file, MainActivity.MODE_PRIVATE);
                            OutputStreamWriter outputWriter = new OutputStreamWriter(out, "UTF-8");

                            outputWriter.close();
                        } catch (IOException e) {
                            System.out.println("MA| stream out failed");
                            e.printStackTrace();
                        }
                    }
                }
            }*/



            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            new RollFetcher().execute();

            new DiceFetcher().execute();
        }
    }

    private void handleDicesList() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Dice loaded!", Toast.LENGTH_SHORT).show();

                // Notifies list on initial loading
                if (histAdapter != null && favorites != null) {
                    notifyFavHist();
                }
            }
        });

    }

    private void handleRollsList() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Rolls loaded!", Toast.LENGTH_SHORT).show();

                // Notifies list on initial loading
                if (histAdapter != null && favorites != null) {
                    notifyDiceHist();
                }
            }
        });

    }

    private void failedLoadingDice() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Failed to load dice.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void failedLoadingRolls() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Failed to load rolls.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Takes the json dice set Dice[] and uploads it to favorites
    private void diceToPreviousRoll(Dice[] diceSet) {

        for (Dice aDice : diceSet) {
            favorites.add(aDice.getPrevRoll());
            //Don't notifyHistAdapter unless you runOnUI thread
        }
    }

    private void rollToPreviousRoll(Roll[] rollSet) {

        for(Roll aRoll : rollSet) {
            //System.out.println("MA| aRoll " + aRoll);
            if(diceHistories == null) {
                System.out.println("MA| diceHistories not initialized");
            } else {
                try {
                    diceHistories.add(aRoll.getPrevRoll());
                } catch (NullPointerException e) {
                    System.out.println("MA| RollToPreviousRoll failed");
                }
            }

            //Don't notifyHistAdapter unless you runOnUI thread
        }
    }




    //-------------------------------------- InterCom --------------------------------------//

    public Fragment getRollFrag(int fav) {
        FragmentManager manager = getSupportFragmentManager();
        if(dicePager != null) {
            return manager.findFragmentByTag(
                    "android:switcher:" + R.id.dicePager + ":" + fav);
        } else if (fav == 0) {
            return manager.findFragmentByTag("diceRoller");
        } else if (fav == 1) {
            return manager.findFragmentByTag("favRoller");
        } else if (fav == 2) {
            return manager.findFragmentByTag("settings");
        } else {
            System.out.println("MA| Roll Fragment " + fav + " not found");
            return null;
        }
    }

    public Fragment getHistFrag(int fav) {
        FragmentManager manager = getSupportFragmentManager();
        if(listPager != null) {
            return manager.findFragmentByTag(
                    "android:switcher:" + R.id.listPager + ":" + fav);
        } else if (fav == 0) {
            return manager.findFragmentByTag("diceHist");
        } else if (fav == 1) {
            return manager.findFragmentByTag("favHist");
        } else {
            System.out.println("MA| Roll Fragment " + fav + " not found");
            return null;
        }
    }


    //-----DiceRolls------------------//

    // Tells DiceRoll to make a new roll
    @Override
    public void setNewRoll(boolean state) {
        DiceRollFragment dRF = (DiceRollFragment) getRollFrag(0);

        dRF.setOriginals(); // Hopefully this does not cause unanticipated errors
    }

    @Override
    public boolean getNewRoll() {
        DiceRollFragment dRF = (DiceRollFragment) getRollFrag(0);

        return dRF.getNewRoll();
    }

    @Override
    public void clearRoller() {
        DiceRollFragment dRF = (DiceRollFragment) getRollFrag(0);

        dRF.setOriginals();
        dRF.setTotalZero();
    }


    //-----DiceHistories--------------//

    // Toggles the individual or summed dice view
    @Override
    public void changeView(int position) {
        PreviousRoll item = diceHistories.get(position);

        if(item.getViewState()) {
            item.setViewState(false);
        } else {
            item.setViewState(true);
        }
    }

    // Supplies diceHistories to DiceHistListAdapter
    @Override
    public ArrayList<PreviousRoll> getDiceHistories() {
        return diceHistories;
    }

    // Re-rolls Ones for a given roll
    @Override
    public void rerollOnes(int position) {
        diceHistories.get(position).rerollOnes();
    }

    // NOT UP TO DATE
    @Override
    public void rerollAllLowest(int position) {
        diceHistories.get(position).rerollAllLowest();
    }

    @Override
    public void removeLowest(int position) {
        boolean hasValues = diceHistories.get(position).removeOneLowest();
        if (!hasValues) {
            removeDiceHistory(position);
            if(position == 0) {
                setNewRoll(true);
            }
        }
    }

    @Override
    public void removeAllLowest(int position) {
        boolean hasValues = diceHistories.get(position).removeAllLowest();
        if (!hasValues) {
            removeDiceHistory(position);
        }
    }

    // Creates a new favorite from a previous roll
    @Override
    public void uploadNewFav(int position) {
        PreviousRoll tempRoll;
        tempRoll = convertToFav(diceHistories.get(position));
        if (tempRoll != null) {
            tempRoll.setForFavList(true);
            favorites.add(0, tempRoll);

            FavRollFragment fRF = (FavRollFragment) getRollFrag(1);

            fRF.uploadFav(favorites.get(0));

            notifyFavHist();
        }
    }

    @Override
    public void removeDiceHistory(int position) {
        deleted = diceHistories.get(position);
        deletedPosition = position;
        diceHistories.remove(position);

        undoView.showUndoBar(getString(R.string.undobar_sample_message));
    }

    @Override
    public void addRule(int position) {
        openRuleDialog(position);
    }

    @Override
    public void addHighlight(int position, boolean forFav) {
        openHighlightDialog(position, forFav);
    }


    //-----DiceHistFragment-----------//

    public ListView getDiceListView() {
        DiceHistFragment dHF= (DiceHistFragment) getHistFrag(0);
        return dHF.getListView();
    }


    //-----Favorites------------------//

    @Override
    public void uploadFav(int position) {
        FavRollFragment fRF = (FavRollFragment) getRollFrag(1);

        fRF.uploadFav(favorites.get(position));
    }

    @Override
    public ArrayList<PreviousRoll> getFavorites() {
        return favorites;
    }

    @Override
    public void removeFavHistory(int position) {
        deleted = favorites.get(position);

        // For some wild reason favorites don't have forFavList == true so this is my workaround
        deleted.setForFavList(true);
        deletedPosition = position;
        favorites.remove(position);

        undoView.showUndoBar(getString(R.string.undobar_sample_message));
    }

    // Favorites Roller
    public void clearSpace(int space) {

    }


    //-----FavRoller------------------//

    @Override
    public void rollAdd(PreviousRoll prevRoll, boolean forFav) {
        if(forFav) {
            FavHistFragment fHF = (FavHistFragment) getHistFrag(1);
            animateAdd(getFavListView(), fHF.favHistAdapter, prevRoll, true);
        } else {
            DiceHistFragment dHF = (DiceHistFragment) getHistFrag(0);
            animateAdd(getDiceListView(), dHF.diceHistAdapter, prevRoll, false);
        }
    }

    // from long pressing a rolled favorite. will roll it again.
    @Override
    public void rollFavorite(int position) {

        PreviousRoll previousRoll = diceHistories.get(position);

        FavRollFragment fRF = (FavRollFragment) getRollFrag(1);

        previousRoll = convertToFav(previousRoll);

        fRF.uploadFav(previousRoll);
        fRF.rollFav();

    }

    // from long pressing a favorite will upload and roll it
    @Override
    public void rollFavoriteFav(int position) {
        FavRollFragment fRF = (FavRollFragment) getRollFrag(1);

        fRF.uploadFav(favorites.get(position));
        fRF.rollFav();

    }

    //-----FavHistFragment------------//

    public ListView getFavListView() {
        FavHistFragment fHF= (FavHistFragment) getHistFrag(1);
        return fHF.getListView();
    }


    //-----Notify---------------------//

    @Override
    public void notifyRollAdapter() {
        rollFragAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyFavHist() {
        FavHistFragment fHF= (FavHistFragment) getHistFrag(1);

        try {
            fHF.favHistAdapter.notifyDataSetChanged();

        } catch (NullPointerException e) {
            System.out.println("MA| notifyFavHist failed... NPE");
            //e.printStackTrace();
        } catch (RuntimeException e) {
            System.out.println("MA| notifyFavHist failed... RuntimeException");
        }
    }

    public void notifyFavHistInvalidated() {
        FavHistFragment fHF= (FavHistFragment) getHistFrag(1);

        try {
            fHF.favHistAdapter.notifyDataSetInvalidated();

        } catch (NullPointerException e) {
            System.out.println("MA| notifyFavHist failed...");
            //e.printStackTrace();
        }
    }

    @Override
    public void notifyDiceHist() {
        DiceHistFragment dHF= (DiceHistFragment) getHistFrag(0);

        try {
            dHF.diceHistAdapter.notifyDataSetChanged();

        } catch (NullPointerException e) {
            System.out.println("MA| notifyDiceHist failed...");
            //e.printStackTrace();
        } catch (RuntimeException e) {
            System.out.println("MA| notifyFavHist failed... RuntimeException");
        }
    }


    //-----Dialog & Toast-------------//

    @Override
    public void openDialog(View view, Integer space) {
        MyDialogFragment newFragment = MyDialogFragment.newInstance(this);
        newFragment.show(getSupportFragmentManager(), "dialog");

        this.space = space;
    }

    @Override
    public void closeDialog(boolean emptyValues, int[] values) {
        MyDialogFragment mdDialog = (MyDialogFragment) getSupportFragmentManager()
                .findFragmentByTag("dialog");

        mdDialog.dismiss();

        if(!emptyValues) {
            FavRollFragment fRF= (FavRollFragment) getRollFrag(1);

            fRF.addModOrDie(space, values[0], values[1]);
        }
    }

    public void openRuleDialog(final int position) {
        final PreviousRoll previousRoll = favorites.get(position);

        final ArrayList<Integer> modifications = new ArrayList<>();

        // Initializes previous rules
        boolean[] checked = new boolean[3];
        for(Integer x : previousRoll.getModifications()) {
            modifications.add(x);
            checked[x] = true;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // Set title
        dialog.setTitle(R.string.modification_title);

        // Set items
        dialog.setMultiChoiceItems(R.array.rules, checked,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which,
                                boolean isChecked) {
                if (isChecked) {
                    modifications.add(which);
                } else if (modifications.contains(which)) {
                    modifications.remove(Integer.valueOf(which));
                }

                notifyFavHist();
            }
        })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        previousRoll.setModifications(modifications);
                        notifyFavHistInvalidated();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });


        dialog.create().show();
    }

    public void openHighlightDialog(final int position, final boolean forFav) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // Set title
        dialog.setTitle(R.string.rule_title);

        // Set items
        dialog.setItems(R.array.highlights, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (forFav) {

                            favorites.get(position).addHighlight(getResources().getColor(R.color.md_red_400), 0, 0, 101);
                            notifyFavHist();
                        } else {
                            diceHistories.get(position).addHighlight(getResources().getColor(R.color.md_red_400), 0, 0, 101);
                            notifyDiceHist();
                        }
                        break;
                    case 1:
                        if (forFav) {
                            favorites.get(position).addHighlight(getResources().getColor(R.color.md_green_400), 1, 0, 101);
                            notifyFavHist();
                        } else {
                            diceHistories.get(position).addHighlight(getResources().getColor(R.color.md_green_400), 1, 0, 101);
                            notifyDiceHist();
                        }
                        break;
                }
            }
        });

        dialog.create().show();
    }


    public void toast(CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    //-----Settings-------------------//

    @Override
    public boolean getDiceDisplaySetting() {
        return diceDisplaySetting;
    }

    @Override
    public boolean getIndividualDisplaySetting() {
        return individualDisplaySetting;
    }

    @Override
    public void setDiceDisplaySettings(boolean isChecked) {
        diceDisplaySetting = isChecked;
    }

    @Override
    public void setIndividualDisplaySettings(boolean isChecked) {
        individualDisplaySetting = isChecked;
    }

    @Override
    public void replayTutorial() {
        opened = false;
        counter = 0;
        introTutorial.setVisibility(View.VISIBLE);
    }

    @Override
    public void openDeletionDialog(final boolean isFav) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // Set title
        if(isFav) {
            dialog.setTitle(R.string.delete_favorites);
        } else {
            dialog.setTitle(R.string.delete_history);
        }

        // Set items
        dialog.setMessage(R.string.no_undo)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isFav) {
                            favorites.clear();
                            notifyFavHist();
                        } else {
                            diceHistories.clear();
                            notifyDiceHist();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                });


        dialog.create().show();
    }

    public void toggleSettings() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


        SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager()
                    .findFragmentByTag("settings");

        FavRollFragment favRollFragment = (FavRollFragment) getSupportFragmentManager()
                .findFragmentByTag("favRoller");

        //System.out.println("MA| settingsFragment");
        //System.out.println("MA| settings visible: " + settingsFragment.isVisible()
        //        + "favRoller visible: " + favRollFragment.isVisible());

        if(settingsFragment == null) {
            //System.out.println("MA| settings null");

            transaction.addToBackStack("settings");

            transaction.replace(R.id.fav_roller, SettingsFragment.newInstance(), "settings")
                    .commit();

        } else if (favRollFragment == null) {
            //System.out.println("MA| fRF null");

            transaction.replace(R.id.fav_roller, FavRollFragment.newInstance(), "favRoller").commit();

            transaction.addToBackStack("favRoller");

        } else if (settingsFragment.isVisible()) {
            getSupportFragmentManager().popBackStack();

        } else if (favRollFragment.isVisible()) {
            transaction.replace(R.id.fav_roller, SettingsFragment.newInstance(), "settings")
                    .commit();
        }

    }






    //----------------------------------- Interface ---------------------------------------//

    //-----Undo-----------------------//

    @Override
    public void onUndo() {
       if(deleted.getForFavList()) {
            favorites.add(deletedPosition, deleted);
            notifyFavHist();
        } else {
            diceHistories.add(deletedPosition, deleted);
            notifyDiceHist();
        }
    }



    //---------------------------------- DialogFragment ----------------------------------//

    /*private MyDialogFragment overlay;

    public void onOpenDialog(View view) {

        // Attempting to not allow a dialog to open if there is one open
        if(getSupportFragmentManager().findFragmentByTag("FragmentDialog") == null) {
            overlay = new MyDialogFragment();

            overlay.show(getSupportFragmentManager(), "FragmentDialog");
        }
    }

    public void closeTheDialog() {

        overlay.dismiss();
    }*/

    //-----JSON-------------------//

    /*@Override
    public String[] getFileList() {
        return fileList();
    }*/


    //------PreviousRoll-------------//

    // Converts a dice roll in dice history to a fav to be uploaded
    // Returns false if it is too large, true if it succeeds
    public PreviousRoll convertToFav(PreviousRoll prevRoll) {
        if(prevRoll.getPreviousRoll().size() > 7) {
            Toast.makeText(getBaseContext(), R.string.too_many_dice, Toast.LENGTH_SHORT).show();
        } else {

            PreviousRoll newRoll = new PreviousRoll(true);

            if(prevRoll.getFave()) {
                newRoll.setRules(prevRoll.getAllRules());
                newRoll.setName(prevRoll.getName());
                newRoll.setColor(prevRoll.getColor());
            }

            int space = 0;

            for (Map.Entry<Integer, ArrayList<Integer>> entry : prevRoll.getPreviousRoll().entrySet()) {
                Integer numSides = entry.getKey();
                ArrayList<Integer> numDice = entry.getValue();

                if (numSides == 0) {
                    newRoll.addFavRoll(space, 0, numDice.get(0));
                    space++;

                } else if (numSides != -1) {
                    newRoll.addFavRoll(space, numSides, numDice.size());
                    space++;
                }

            }
            return newRoll;
        }
        return null;
    }




    //-------------------------------- Animations --------------------------------------------//

    public void getAnimateRemoval(int position) {
        DiceHistFragment dHF= (DiceHistFragment) getHistFrag(0);

        animateRemoval(getDiceListView(), dHF.diceHistAdapter, position, false);
    }

    public void getAnimateRemovalFav(int position) {
        FavHistFragment fHF = (FavHistFragment) getHistFrag(1);

        animateRemoval(getFavListView(), fHF.favHistAdapter, position, true);
    }

    //List remove
    private void animateRemoval(final ListView listView, final ArrayAdapter adapter, int position, boolean forFav) {
        final HashMap<Long, Integer> itemIdTopMap = new HashMap<>();


        int firstVisiblePosition = listView.getFirstVisiblePosition();
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            int fVPosition = firstVisiblePosition + i;
            long itemID = adapter.getItemId(fVPosition);
            itemIdTopMap.put(itemID, child.getTop());
        }

        if(forFav) {
            removeFavHistory(position);
        } else {
            removeDiceHistory(position);
        }

        final ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listView.getFirstVisiblePosition();
                for (int i = 0; i < listView.getChildCount(); ++i) {
                    final View child = listView.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemID = adapter.getItemId(position);
                    Integer startTop = itemIdTopMap.get(itemID);
                    int top = child.getTop();
                    if (startTop != null) {
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setEnabled(true);
                                    // didn't add this part. don't know what it does
//                                    mBackgroundContainer.hideBackground();
//                                    mSwiping = false;
                                }
                            });
                            firstAnimation = false;
                        }
                    } else {
                        int childHeight = child.getHeight() + listView.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                itemIdTopMap.clear();
                return true;
            }
        });

    }

    //List add
    private void animateAdd(final ListView listView, final ArrayAdapter adapter, PreviousRoll prevRoll, boolean forFav) {
        final HashMap<Long, Integer> itemIdTopMap = new HashMap<>();

        int firstVisiblePosition = listView.getFirstVisiblePosition();
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            int fVPosition = firstVisiblePosition + i;
            long itemID = adapter.getItemId(fVPosition);
            //System.out.println("MA| ID: " + itemID);

            itemIdTopMap.put(itemID, child.getTop());
        }

        if(forFav) {
            favorites.add(0, prevRoll);
        } else {
            diceHistories.add(0, prevRoll);
        }

        final ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listView.getFirstVisiblePosition();
                for (int i = 0; i < listView.getChildCount(); ++i) {
                    final View child = listView.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemID = adapter.getItemId(position);
                    //System.out.println("MA| itemID " + itemID);
                    Integer startTop = itemIdTopMap.get(itemID);
                    int top = child.getTop();
                    if (startTop != null) {
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setEnabled(true);
                                    // didn't add this part.
//                                    mBackgroundContainer.hideBackground();
//                                    mSwiping = false;
                                }
                            });
                            firstAnimation = false;
                        }
                    }else {
                        int childHeight = child.getHeight() + listView.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                itemIdTopMap.clear();
                return true;
            }
        });

    }

}
