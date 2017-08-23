package app.bryanlu.quizbowl.sqlite;

import android.provider.BaseColumns;

/**
 * Created by Bryan Lu on 5/25/2017.
 *
 * Contract for the questions table of the sqlite database.
 */

public final class QuizBowlContract {
    // Private constructor to prevent instantiation
    private QuizBowlContract() {}

    /* Inner class that describes table content */
    public static class BaseTable implements BaseColumns {
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_SUBCATEGORY = "subcategory";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_ANSWER = "answer";
        public static final String COLUMN_DIFFICULTY = "difficulty";
        public static final String COLUMN_TOURNAMENT = "tournament";
        public static final String COLUMN_YEAR = "year";
    }

    public static class CurrentEvents extends BaseTable {
        public static final String TABLE_NAME = "CurrentEvents";
    }

    public static class FineArts extends BaseTable {
        public static final String TABLE_NAME = "FineArts";
    }

    public static class Geography extends BaseTable {
        public static final String TABLE_NAME = "Geography";
    }

    public static class History extends BaseTable {
        public static final String TABLE_NAME = "History";
    }

    public static class Literature extends BaseTable {
        public static final String TABLE_NAME = "Literature";
    }

    public static class Mythology extends BaseTable {
        public static final String TABLE_NAME = "Mythology";
    }

    public static class Philosophy extends BaseTable {
        public static final String TABLE_NAME = "Philosophy";
    }

    public static class Religion extends BaseTable {
        public static final String TABLE_NAME = "Religion";
    }

    public static class Science extends BaseTable {
        public static final String TABLE_NAME = "Science";
    }

    public static class SocialScience extends BaseTable {
        public static final String TABLE_NAME = "SocialScience";
    }

    public static class Trash extends BaseTable {
        public static final String TABLE_NAME = "Trash";
    }
}
