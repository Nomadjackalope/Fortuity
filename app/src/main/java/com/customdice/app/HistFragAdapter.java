package com.customdice.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Benjamin on 4/15/2015.
 *
 * This returns the DiceHistFragment or FavHistFragment instances
 */
public class HistFragAdapter extends FragmentPagerAdapter {
    public HistFragAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return DiceHistFragment.newInstance(position);
        } else {
            return FavHistFragment.newInstance(position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Roll History";
            case 1:
                return "Favorites";
        }

        return null;
    }
}
