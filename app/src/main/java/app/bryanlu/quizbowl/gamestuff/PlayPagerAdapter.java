package app.bryanlu.quizbowl.gamestuff;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Bryan Lu on 5/21/2017.
 *
 * Adapter that adds for the Play Menu tabs.
 */

class PlayPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 3;
    private String[] tabTitles = {"Setup", "Play", "Scores"};

    PlayPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case SetupFragment.POSITION:
                return new SetupFragment();
            case PlayFragment.POSITION:
                return new PlayFragment();
            case ScoresFragment.POSITION:
                return new ScoresFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
