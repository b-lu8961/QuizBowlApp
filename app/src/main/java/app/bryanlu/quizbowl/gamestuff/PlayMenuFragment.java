package app.bryanlu.quizbowl.gamestuff;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.bryanlu.quizbowl.R;

/**
 * Created by Bryan Lu on 5/21/2017.
 *
 * Fragment that holds the tabs for the game part of the app.
 */

public class PlayMenuFragment extends Fragment {
    public PlayMenuFragment() {
        // Required default fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_play_menu, container, false);

        ViewPager mViewPager = (ViewPager) mView.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new PlayPagerAdapter(getFragmentManager(), getContext()));

        TabLayout mTabLayout = (TabLayout) mView.findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        return mView;
    }
}
