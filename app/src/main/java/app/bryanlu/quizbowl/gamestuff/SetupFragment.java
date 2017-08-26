package app.bryanlu.quizbowl.gamestuff;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import app.bryanlu.quizbowl.R;
import app.bryanlu.quizbowl.sqlite.Category;
import app.bryanlu.quizbowl.sqlite.QuizBowlContract;
import app.bryanlu.quizbowl.sqlite.QuizBowlDbHelper;

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
    private TextView subcategoryText;
    private LinearLayout subcategoryLayout;

    private ArrayList<Category> selectedCategories = new ArrayList<>();
    private View.OnClickListener categoryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox selected = (CheckBox) view;
            String checkboxText = selected.getText().toString();

            if (selected.isChecked()) {
                selectedCategories.add(new Category(checkboxText));
                addSubcategoryViews(checkboxText);
            }

            if (!selected.isChecked()) {
                for (Category category : selectedCategories) {
                    if (category.getName().equals(checkboxText)) {
                        selectedCategories.remove(category);
                        break;
                    }
                }
                removeSubcategoryViews(checkboxText);
            }

            if (selectedCategories.size() == 0) {
                subcategoryText.setVisibility(View.INVISIBLE);
            }
            else {
                subcategoryText.setVisibility(View.VISIBLE);
            }
            mCallback.onCategoryChange(selectedCategories);
        }
    };
    private View.OnClickListener subcategoryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox selected = (CheckBox) view;
            String tag = (String) selected.getTag();
            Category category = Category.findByName(selectedCategories, tag);
            String subcategory = selected.getText().toString();

            if (selected.isChecked() && category != null) {
                 if (subcategory.equals("None")) {
                     category.addSubcategory("");
                 }
                 else {
                     category.addSubcategory(subcategory);
                 }
            }
            else if (category != null) {
                category.removeSubcategory(subcategory);
            }
            mCallback.onCategoryChange(selectedCategories);
        }
    };

    private OnCheckboxClickedListener mCallback;
    public interface OnCheckboxClickedListener {
        void onCategoryChange(ArrayList<Category> categories);
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
        subcategoryText = (TextView) mView.findViewById(R.id.subcategoryText);
        subcategoryLayout = (LinearLayout) mView.findViewById(R.id.subcategoryLayout);

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
        currentBox.setOnClickListener(categoryListener);
        artBox.setOnClickListener(categoryListener);
        geoBox.setOnClickListener(categoryListener);
        historyBox.setOnClickListener(categoryListener);
        litBox.setOnClickListener(categoryListener);
        mythBox.setOnClickListener(categoryListener);
        philBox.setOnClickListener(categoryListener);
        relBox.setOnClickListener(categoryListener);
        sciBox.setOnClickListener(categoryListener);
        socSciBox.setOnClickListener(categoryListener);
        trashBox.setOnClickListener(categoryListener);
    }

    /**
     * Creates a title text and subcategory checkboxes for the selected category.
     * @param category category whose checkbox was selected
     */
    private void addSubcategoryViews(String category) {
        QuizBowlDbHelper helper = new QuizBowlDbHelper(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        // Add category name
        TextView subcategoryText = new TextView(getContext());
        subcategoryText.setText("• " + category);
        subcategoryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        subcategoryText.setContentDescription(category);
        subcategoryLayout.addView(subcategoryText);

        // Get subcategories from the database
        String query = "SELECT DISTINCT " + QuizBowlContract.BaseTable.COLUMN_SUBCATEGORY +
                " FROM " + category.replace(" ", "");
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<String> subcategories = new ArrayList<>();
        while (cursor.moveToNext()) {
            String subcategory = cursor.getString(cursor.getColumnIndex(
                    QuizBowlContract.BaseTable.COLUMN_SUBCATEGORY
            ));
            subcategories.add(subcategory);
        }
        cursor.close();

        //Make checkboxes for each subcategory in the category
        for (String subcategory : subcategories) {
            CheckBox subCheckBox = new CheckBox(getContext());
            if (subcategory.equals("")) {
                subCheckBox.setText("None");
            }
            else {
                subCheckBox.setText(subcategory);
            }
            subCheckBox.setContentDescription(category);
            subCheckBox.setOnClickListener(subcategoryListener);
            subcategoryLayout.addView(subCheckBox);
        }
        db.close();
    }

    /**
     * Removes the title and checkboxes of the specified category
     * @param category category whose checkbox was deselected
     */
    private void removeSubcategoryViews(String category) {
        ArrayList<View> viewsToRemove = new ArrayList<>();
        subcategoryLayout.findViewsWithText(viewsToRemove, category,
                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        for (View v : viewsToRemove) {
            subcategoryLayout.removeView(v);
        }
    }
}
