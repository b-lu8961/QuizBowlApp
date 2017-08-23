package app.bryanlu.quizbowl.firebase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bryan Lu on 4/16/2017.
 *
 * Database object that manages player interactions.
 */

public class GameRoom {
    private String buzzerId;
    private boolean gameInProgress;
    private Question question;
    private boolean questionInProgress;
    private int questionNumber;
    private ArrayList<User> currentUsers;
    private ArrayList<User> restrictedUsers;
    private CategoryList categories;
    private HashMap<String, Integer> userScores;

    public static final String GAMEROOM = "gameroom";
    public static final String BUZZER_ID = "buzzerId";
    public static final String G_PROGRESS = "gameInProgress";
    public static final String QUESTION = "question";
    public static final String Q_PROGRESS = "questionInProgress";
    public static final String Q_NUMBER = "questionNumber";
    public static final String CURRENT = "currentUsers";
    public static final String RESTRICTED = "restrictedUsers";
    public static final String CATEGORIES = "categories";
    public static final String SCORES = "userScores";

    public GameRoom() {
        buzzerId = "none";
        gameInProgress = false;
        question = new Question();
        questionInProgress = false;
        questionNumber = 0;
        categories = new CategoryList();
    }

    public String getBuzzerId() {
        return buzzerId;
    }

    public boolean getGameInProgress() {
        return gameInProgress;
    }

    public Question getQuestion() {
        return question;
    }

    public boolean isQuestionInProgress() {
        return questionInProgress;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public ArrayList<User> getCurrentUsers() {
        return currentUsers;
    }

    public ArrayList<User> getRestrictedUsers() {
        return restrictedUsers;
    }

    public CategoryList getCategories() {
        return categories;
    }

    public HashMap<String, Integer> getUserScores() {
        return userScores;
    }
}
