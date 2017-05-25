package app.bryanlu.quizbowl.sqlite;

import android.provider.BaseColumns;

/**
 * Created by Bryan Lu on 5/25/2017.
 *
 * Contract for the questions table of the sqlite database.
 */

public final class QuestionsContract {
    // Private constructor to prevent instantiation
    private QuestionsContract() {}

    /* Inner class that describes table content */
    public static class QuestionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "questions";
        public static final String COLUMN_NAME_QUESTION = "question";
        public static final String COLUMN_NAME_ANSWER = "answer";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
        public static final String COLUMN_NAME_DIFFICULTY = "difficulty";
        public static final String COLUMN_NAME_TOURNAMENT = "tournament";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_ROUND = "round";
        public static final String COLUMN_NAME_NUM = "num";
    }
}
