package app.bryanlu.quizbowl.gamestuff;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Bryan Lu on 5/21/2017.
 *
 * Adapter that returns fragments for each of the Play Menu tabs.
 */

class PlayPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 3;
    private SetupFragment setup;
    private PlayFragment play;
    private ScoresFragment scores;
    private String[] tabTitles = {"Setup", "Play", "Scores"};

    PlayPagerAdapter(FragmentManager fm, SetupFragment setup, PlayFragment play,
                     ScoresFragment scores) {
        super(fm);
        this.setup = setup;
        this.play = play;
        this.scores = scores;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case SetupFragment.POSITION:
                return setup;
            case PlayFragment.POSITION:
                return play;
            case ScoresFragment.POSITION:
                return scores;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
