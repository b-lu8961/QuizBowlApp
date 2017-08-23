package app.bryanlu.quizbowl.sqlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
    private ArrayList<String> selectedCategories;
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

    /**
     *
     * @param categories
     */
    public void setSelectedCategories(ArrayList<String> categories) {
        selectedCategories = categories;
        //GameUtils.setCategories(new CategoryList(selectedCategories));
    }


    /**
     *
     * @return
     */
    private String chooseCategory() {
        String[] tableNames;
        if (selectedCategories == null || selectedCategories.size() == 0) {
            tableNames = mContext.getResources().getStringArray(R.array.table_names);
        }
        else {
            tableNames = selectedCategories.toArray(new String[selectedCategories.size()]);
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
    private String[] getSelectionArgs(String tableName) {
        int categoryMax = preferences.getInt(tableName, 0);
        int questionId = new java.util.Random().nextInt(categoryMax);
        return new String[] {Integer.toString(questionId)};
    }

    /**
     * Sets a random question from the question list to be the current question.
     * @return true if question was set, false if not
     */
    public Question getQuestion() {
        QuizBowlDbHelper helper = new QuizBowlDbHelper(mContext);
        String tableName = chooseCategory();
        String selection = QuizBowlContract.BaseTable.COLUMN_ID + " = ?";
        String[] selectionArgs = getSelectionArgs(tableName);

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                tableName,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );
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
        Question selectedQuestion = new Question(answer, question);
        GameUtils.setDatabaseQuestion(selectedQuestion);
        cursor.close();
        db.close();
        return selectedQuestion;
    }
}
