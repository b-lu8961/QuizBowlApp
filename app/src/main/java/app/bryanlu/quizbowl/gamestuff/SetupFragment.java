package app.bryanlu.quizbowl.gamestuff;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.ArrayList;

import app.bryanlu.quizbowl.R;

/**
 * Created by Bryan Lu on 5/21/2017.
 *
 * Fragment that allows users to choose different types of questions.
 */

public class SetupFragment extends Fragment {
    public static final int POSITION = 0;

    private CheckBox currentBox;
    private CheckBox artBox;
    private CheckBox geoBox;
    private CheckBox historyBox;
    private CheckBox litBox;
    private CheckBox mythBox;
    private CheckBox philBox;
    private CheckBox relBox;
    private CheckBox sciBox;
    private CheckBox socSciBox;
    private CheckBox trashBox;
    private ArrayList<String> selectedCategories = new ArrayList<>();
    private View.OnClickListener checkboxListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox selected = (CheckBox)view;
            String checkboxText = selected.getText().toString().replace(" ", "");

            if (selected.isChecked()) {
                selectedCategories.add(checkboxText);
            }
            else {
                selectedCategories.remove(checkboxText);
            }
            mCallback.onCategoryChange(selectedCategories);
        }
    };

    private OnCheckboxClickedListener mCallback;
    public interface OnCheckboxClickedListener {
        void onCategoryChange(ArrayList<String> categories);
    }

    public SetupFragment() {
        // Required default fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_setup, container, false);

        currentBox = (CheckBox) mView.findViewById(R.id.currentCheckBox);
        artBox = (CheckBox) mView.findViewById(R.id.artCheckBox);
        geoBox = (CheckBox) mView.findViewById(R.id.geoCheckBox);
        historyBox = (CheckBox) mView.findViewById(R.id.historyCheckBox);
        litBox = (CheckBox) mView.findViewById(R.id.litCheckBox);
        mythBox = (CheckBox) mView.findViewById(R.id.mythCheckBox);
        philBox = (CheckBox) mView.findViewById(R.id.philCheckBox);
        relBox = (CheckBox) mView.findViewById(R.id.relCheckBox);
        sciBox = (CheckBox) mView.findViewById(R.id.sciCheckBox);
        socSciBox = (CheckBox) mView.findViewById(R.id.socSciCheckBox);
        trashBox = (CheckBox) mView.findViewById(R.id.trashCheckBox);

        attachCategoryListeners();

        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnCheckboxClickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement " +
                    "OnCheckboxSelectedListener");
        }
    }

    private void attachCategoryListeners() {
        currentBox.setOnClickListener(checkboxListener);
        artBox.setOnClickListener(checkboxListener);
        geoBox.setOnClickListener(checkboxListener);
        historyBox.setOnClickListener(checkboxListener);
        litBox.setOnClickListener(checkboxListener);
        mythBox.setOnClickListener(checkboxListener);
        philBox.setOnClickListener(checkboxListener);
        relBox.setOnClickListener(checkboxListener);
        sciBox.setOnClickListener(checkboxListener);
        socSciBox.setOnClickListener(checkboxListener);
        trashBox.setOnClickListener(checkboxListener);
    }
}
