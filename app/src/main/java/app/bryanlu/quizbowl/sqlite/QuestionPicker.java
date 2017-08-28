package app.bryanlu.quizbowl.sqlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import app.bryanlu.quizbowl.R;
import app.bryanlu.quizbowl.firebase.Question;
import app.bryanlu.quizbowl.gamestuff.GameUtils;

/**
 * Class to help choose questions from the sqlite database.
 * Created by Bryan Lu on 8/17/2017.
 */

public class QuestionPicker {
    private Context mContext;
    private boolean simpleSelect = true;
    private ArrayList<Category> selectedCategories = new ArrayList<>();
    private ArrayList<String> selectedDifficulties = new ArrayList<>();
    private SharedPreferences preferences;
    private final String[] columns = {
            QuizBowlContract.BaseTable.COLUMN_QUESTION,
            QuizBowlContract.BaseTable.COLUMN_ANSWER
    };

    public QuestionPicker(Context mContext) {
        this.mContext = mContext;
        preferences = mContext.getSharedPreferences(mContext.getString(R.string.pref_file_key),
                Context.MODE_PRIVATE);
    }

    private void checkForSimpleSelect() {
        if (selectedCategories.size() == 0) {
            if (selectedDifficulties.size() == 0) {
                simpleSelect = true;
            }
        }
        else {
            simpleSelect = false;
        }
    }
    /**
     * Sets the correct categories to be available for use in question picking.
     * @param categories list of categories obtained from the Setup fragment
     */
    public void setSelectedCategories(ArrayList<Category> categories) {
        selectedCategories = categories;
        for (int i = 0; i < selectedCategories.size(); i++) {
            String category = selectedCategories.get(i).getName();
            if (category.contains(" ")) {
                selectedCategories.get(i).setName(category.replace(" ", ""));
            }
        }
        checkForSimpleSelect();
        //GameUtils.setCategories(new CategoryList(selectedCategories));
    }

    /**
     * Sets the correct difficulties to be available.
     * @param difficulty difficulty level to update
     * @param add true to add, false to remove
     */
    public void setSelectedDifficulties(String difficulty, boolean add) {
        if (add) {
            selectedDifficulties.add(difficulty);
        }
        else {
            selectedDifficulties.remove(difficulty);
        }
        checkForSimpleSelect();
    }

    /**
     * Chooses a category from the selected ones. Weighted by how many questions are in the
     * category.
     * @return name of the category to pick a question from
     */
    private String chooseCategory() {
        String[] tableNames;
        if (selectedCategories == null || selectedCategories.size() == 0) {
            tableNames = mContext.getResources().getStringArray(R.array.table_names);
        }
        else {
            tableNames = new String[selectedCategories.size()];
            for (int i = 0; i < selectedCategories.size(); i++) {
                tableNames[i] = selectedCategories.get(i).getName();
            }
        }

        int totalNumQuestions = 0;
        for (String name : tableNames) {
            totalNumQuestions += preferences.getInt(name, 0);
        }
        int randomInt = new java.util.Random().nextInt(totalNumQuestions + 1);
        String tableName = "";
        for (String name : tableNames) {
            int categoryMax = preferences.getInt(name, 0);
            if (randomInt <= categoryMax) {
                tableName = name;
                break;
            }
            randomInt -= categoryMax;
        }
        return tableName;
    }

    /**
     * Gets a random question id from the selected category.
     * @param tableName name of category to use
     * @return int id that represents a question (put into a String[])
     */
    private String[] getQuestionId(String tableName) {
        int categoryMax = preferences.getInt(tableName, 0);
        int questionId = new java.util.Random().nextInt(categoryMax);
        return new String[] {Integer.toString(questionId)};
    }



    /**
     * Chooses a question from the database.
     * @return the question chosen from the database
     */
    public Question getQuestion() {
        QuizBowlDbHelper helper = new QuizBowlDbHelper(mContext);
        String tableName = chooseCategory();
        Category chosenCategory = Category.findByName(selectedCategories, tableName);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;
        if (simpleSelect) {
            String selection = QuizBowlContract.BaseTable.COLUMN_ID + " = ?";
            String[] selectionArgs = getQuestionId(tableName);
            cursor = db.query(
                    tableName,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );
        }
        else {
            String selection = "";
            ArrayList<String> selectionArgs = new ArrayList<>();

            // Check for subcategories
            ArrayList<String> subcategoryList = chosenCategory.getSubcategories();
            if (subcategoryList.size() != 0) {
                selection = "(" + QuizBowlContract.BaseTable.COLUMN_SUBCATEGORY + " = ";
                for (int i = 0; i < subcategoryList.size(); i++) {
                    if (i < subcategoryList.size() - 1) {
                        selection += "? OR "
                                + QuizBowlContract.BaseTable.COLUMN_SUBCATEGORY + " = ";
                    }
                    else {
                        selection += "?)";
                    }
                    selectionArgs.add(subcategoryList.get(i));
                }
            }

            // Check for difficulties
            if (selectedDifficulties.size() != 0) {
                selection += subcategoryList.size() != 0 ? " AND " : "";
                selection += "(" + QuizBowlContract.BaseTable.COLUMN_DIFFICULTY + " = ";
                for (int i = 0; i < selectedDifficulties.size(); i++) {
                    if (i < selectedDifficulties.size() - 1) {
                        selection += "? OR "
                                + QuizBowlContract.BaseTable.COLUMN_DIFFICULTY + " = ";
                    }
                    else {
                        selection += "?)";
                    }
                    selectionArgs.add(selectedDifficulties.get(i));
                }
            }

            selection += " ORDER BY RANDOM() LIMIT 1";
            cursor = db.query(
                    tableName,
                    columns,
                    selection,
                    selectionArgs.toArray(new String[selectionArgs.size()]),
                    null, null, null
            );

        }
        String question = "";
        String answer = "";
        while (cursor.moveToNext()) {
            question = cursor.getString(
                    cursor.getColumnIndex(QuizBowlContract.BaseTable.COLUMN_QUESTION)
            );
            answer = cursor.getString(
                    cursor.getColumnIndex(QuizBowlContract.BaseTable.COLUMN_ANSWER)
            );
        }
        cursor.close();
        db.close();

        Question selectedQuestion = new Question(answer, question);
        GameUtils.setDatabaseQuestion(selectedQuestion);
        return selectedQuestion;
    }
}
