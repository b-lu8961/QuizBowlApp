package app.bryanlu.quizbowl.gamestuff;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.bryanlu.quizbowl.R;

/**
 * Created by Bryan Lu on 5/21/2017.
 *
 * Fragment that allows users to choose different types of questions.
 */

public class SetupFragment extends Fragment {
    public SetupFragment() {
        // Required default fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_setup, container, false);

        return mView;
    }
}
