package app.bryanlu.quizbowl.sqlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import app.bryanlu.quizbowl.R;

/**
 * Created by Bryan Lu on 8/13/2017.
 * Helper class for the sqlite database of questions.
 */

public class QuizBowlDbHelper extends SQLiteAssetHelper {
    private static final String DATABASE_PATH = "/data/data/app.bryanlu.quizbowl/databases";
    private static final String DATABASE_NAME = "Questions.db";
    private static final int DATABASE_VERSION = 1;
    private Context mContext;
    public QuizBowlDbHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_PATH, null, DATABASE_VERSION);
        mContext = context;
        initializePreferences();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    /**
     * Creates the shared preferences file that holds how many questions each category has.
     */
    private void initializePreferences() {
        SharedPreferences preferences = mContext.getSharedPreferences(
                mContext.getString(R.string.pref_file_key), Context.MODE_PRIVATE
        );
        final String[] tableNames = mContext.getResources().getStringArray(R.array.table_names);
        if (!preferences.contains(tableNames[0])) {
            SQLiteDatabase db = getReadableDatabase();

            for (String name : tableNames) {
                int numEntries = (int)DatabaseUtils.queryNumEntries(db, name);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt(name, numEntries);
                editor.apply();
            }

            db.close();
        }
    }
}
