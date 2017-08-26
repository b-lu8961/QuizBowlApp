package app.bryanlu.quizbowl.gamestuff;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import app.bryanlu.quizbowl.R;
import app.bryanlu.quizbowl.sqlite.Category;

/**
 * Created by Bryan Lu on 5/21/2017.
 *
 * Fragment that holds the tabs for the game part of the app.
 */

public class PlayMenuFragment extends Fragment {
    public static final int POSITION = 0;
    public static final String POSITION_KEY = "POSITION_KEY";
    public static final String TAG = "PLAY_MENU_FRAGMENT";
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private SetupFragment setupFragment = new SetupFragment();
    private PlayFragment playFragment = new PlayFragment();
    private ScoresFragment scoresFragment = new ScoresFragment();

    public PlayMenuFragment() {
        // Required default fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_play_menu, container, false);

        mViewPager = (ViewPager) mView.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new PlayPagerAdapter(getFragmentManager(), setupFragment,
                playFragment, scoresFragment));

        mTabLayout = (TabLayout) mView.findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION_KEY, mTabLayout.getSelectedTabPosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
                mViewPager.setCurrentItem(savedInstanceState.getInt(POSITION_KEY));
        }
    }

    public void updatePlayFragment(ArrayList<Category> categories) {
        playFragment.updateParameters(categories);
    }

}
