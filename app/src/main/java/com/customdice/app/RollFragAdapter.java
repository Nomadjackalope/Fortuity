package com.customdice.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ben on 4/14/2015.
 *
 */
public class RollFragAdapter extends FragmentPagerAdapter {
    public RollFragAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new DiceRollFragment();
        } else if (position == 1) {
            return new FavRollFragment();
        } else {
            return new SettingsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Standard Roller";
            case 1:
                return "Custom Roller";
            case 2:
                return "Settings";
        }

        return null;
    }
}
