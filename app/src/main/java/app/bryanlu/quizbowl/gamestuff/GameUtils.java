package app.bryanlu.quizbowl.gamestuff;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import app.bryanlu.quizbowl.firebase.CategoryList;
import app.bryanlu.quizbowl.firebase.GameRoom;
import app.bryanlu.quizbowl.firebase.Question;
import app.bryanlu.quizbowl.firebase.Stats;
import app.bryanlu.quizbowl.firebase.User;

import static app.bryanlu.quizbowl.MainActivity.mDatabase;
import static app.bryanlu.quizbowl.MainActivity.mUser;

/**
 * Created by Bryan Lu on 5/21/2017.
 */

public class GameUtils {
    private static DatabaseReference gameroomRef = mDatabase.child(GameRoom.GAMEROOM);

    /**
     * Checks if there is a gameroom in the database.
     */
    static void checkForGameRoom() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(GameRoom.GAMEROOM).exists()) {
                    GameUtils.createGameRoom();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Creates a gameroom in the database.
     */
    private static void createGameRoom() {
        gameroomRef.setValue(new GameRoom());
    }

    /**
     * Adds the current user to the currentUsers part of the gameroom.
     * Calls initializeLocalGame() after this has been done.
     * @param fragment fragment to initialize after connection
     */
    static void connectToGameRoom(PlayFragment fragment) {
        if (mUser != null) {
            final PlayFragment mFragment = fragment;
            gameroomRef.child(GameRoom.CURRENT).child(mUser.getUid()).setValue(true)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFragment.initializeLocalGame();
                        }
                    });
        }
    }

    /**
     * Removes the current user from the gameroom.
     */
    static void disconnectFromGameRoom() {
        if (mUser != null) {
            gameroomRef.child(GameRoom.CURRENT).child(mUser.getUid())
                    .removeValue();
            gameroomRef.child(GameRoom.RESTRICTED).child(mUser.getUid())
                    .removeValue();
        }
    }

    /**
     * Selects the appropriate categories in the database. This makes the database value of the
     * category true.
     * @param selectedCategories categories to be selected
     */
    static void setCategories(CategoryList selectedCategories) {
        gameroomRef.child(GameRoom.CATEGORIES).setValue(selectedCategories);
    }

    /**
     * Makes every category not selected in the database.
     */
    static void resetCategories() {
        gameroomRef.child(GameRoom.CATEGORIES).setValue(new CategoryList());
    }


    static void setGameInProgress(boolean gameStatus) {
        gameroomRef.child(GameRoom.G_PROGRESS).setValue(gameStatus);
    }

    /**
     * Attaches the current user's game score to their unique Uid and sends it to the database.
     * Then initiates the final score comparison.
     * @param context context passed to compareScore for the toast
     * @param numUsers passed to waitForAllScores for the number of entries needed
     * @param username username to link gameScore with
     * @param gameScore score to be sent to the database
     */
    static void sendGameScore(Context context, int numUsers, String username, int gameScore) {
        final Context mContext = context;
        final int mNumUsers = numUsers;
        gameroomRef.child(GameRoom.SCORES).child(username).setValue(gameScore)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitForAllScores(mContext, mNumUsers);
                    }
                });
    }

    /**
     * Uses a transaction (and recursion) to wait for the scores from every user to come in
     * before comparing them.
     * @param context context passed to compareScore for the toast
     * @param numUsers number of entries needed in the database to move on
     */
    private static void waitForAllScores(Context context, int numUsers) {
        final Context mContext = context;
        final int mNumUsers = numUsers;
        gameroomRef.child(GameRoom.SCORES).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean b, DataSnapshot dataSnapshot) {
                if ((int)dataSnapshot.getChildrenCount() == mNumUsers) {
                    compareScores(mContext);
                }
                else {
                    waitForAllScores(mContext, mNumUsers);
                }
            }
        });


    }

    /**
     * Sets up a database query that orders children by their score value, then shows a toast that
     * displays the winner's username and score.
     * @param context context to show the toast in
     */
    private static void compareScores(Context context) {
        final Context mContext = context;
        Query scoreQuery = gameroomRef.child(GameRoom.SCORES).orderByValue();
        scoreQuery.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot winner = dataSnapshot.getChildren().iterator().next();
                String winnerName = winner.getKey();
                int winnerScore = winner.getValue(Integer.class);
                String winString = winnerName + " has won with a score of " +
                        Integer.toString(winnerScore) + ".";
                Toast.makeText(mContext, winString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Updates the current user's database stats.
     * 10 points for a correct answer.
     * -5 for an incorrect answer while the question is still running.
     * 0 for an incorrect after the question has finished reading.
     * @param correct is answer correct
     * @param running is buzz before end of question
     */
    static void updateStats(boolean correct, boolean running) {
        final boolean answerCorrect = correct;
        final boolean stillRunning = running;
        if (mUser != null) {
            DatabaseReference statsReference = mDatabase.child(User.USERS).child(mUser.getUid())
                    .child(User.STATS);
            statsReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue(Stats.class) == null) {
                        return Transaction.success(mutableData);
                    }
                    int totalScore = mutableData.child(Stats.SCORE).getValue(Integer.class);
                    int questionsSeen = mutableData.child(Stats.SEEN).getValue(Integer.class);
                    int questionsCorrect = mutableData.child(Stats.CORRECT)
                            .getValue(Integer.class);
                    int questionsIncorrect = mutableData.child(Stats.INCORRECT)
                            .getValue(Integer.class);

                    questionsSeen++;
                    if (answerCorrect) {
                        totalScore += 10;
                        questionsCorrect++;
                    }
                    else {
                        if (stillRunning) {
                            totalScore -= 5;
                        }
                        questionsIncorrect++;
                    }

                    mutableData.child(Stats.SCORE).setValue(totalScore);
                    mutableData.child(Stats.SEEN).setValue(questionsSeen);
                    mutableData.child(Stats.CORRECT).setValue(questionsCorrect);
                    mutableData.child(Stats.INCORRECT).setValue(questionsIncorrect);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean b, DataSnapshot dataSnapshot) {

                }
            });
        }
    }

    /**
     * Sets the database's question to the question passed in.
     * @param currentQuestion question to update the database with
     */
    public static void setDatabaseQuestion(Question currentQuestion) {
        gameroomRef.child(GameRoom.QUESTION).setValue(currentQuestion)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        incrementQuestionNumber();
                    }
                });
    }

    /**
     * Increments the database's question number by one after a new question has bet set.
     */
    private static void incrementQuestionNumber() {
        DatabaseReference questionNumberRef = gameroomRef.child(GameRoom.Q_NUMBER);
        questionNumberRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }
                int questionNumber = mutableData.getValue(Integer.class);
                questionNumber++;
                mutableData.setValue(questionNumber);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    static void setQuestionInProgress(boolean inProgress) {
        gameroomRef.child(GameRoom.Q_PROGRESS).setValue(inProgress);
    }

    /**
     * Adapted from https://github.com/zillesc/Matchmaker.
     * Tries to signal a user buzz based on the status of buzzerId.
     */
    static void tryBuzz(PlayFragment fragment) {
        final PlayFragment mFragment = fragment;
        gameroomRef.child(GameRoom.BUZZER_ID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String buzzerId = dataSnapshot.getValue(String.class);
                        if (buzzerId.equals("none")) {
                            runBuzzTransaction(mFragment);
                        }
                        else {
                            mFragment.handleBuzz(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Adapted from https://github.com/zillesc/Matchmaker
     * Runs a transaction to change the database's buzzerId in a way that is concurrency-safe.
     */
    private static void runBuzzTransaction(PlayFragment fragment) {
        final PlayFragment mFragment = fragment;
        gameroomRef.child(GameRoom.BUZZER_ID).runTransaction(
                new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData.getValue() == null) {
                            return Transaction.success(mutableData);
                        }
                        else if (mutableData.getValue(String.class).equals("none")) {
                            mutableData.setValue(mUser.getUid());
                            return Transaction.success(mutableData);
                        }
                        else {
                            return Transaction.abort();
                        }
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean commit,
                                           DataSnapshot dataSnapshot) {
                        mFragment.handleBuzz(commit);
                    }
                });
    }

    /**
     * Sets buzzerId in the database to "none".
     */
    static void resetBuzzInProgress() {
        gameroomRef.child(GameRoom.BUZZER_ID).setValue("none");
    }

    /**
     * Notifies the user that a different user has buzzed in.
     * @param userId unique Id of user who has buzzed
     * @param context context to show the Toast on
     */
    static void makeBuzzToast(String userId, Context context) {
        final Context mContext = context;
        mDatabase.child(User.USERS).child(userId).child(User.NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.getValue(String.class);
                        String toastText = username + " has buzzed.";
                        Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Adds the current user to the database's restricted list.
     */
    static void addToRestrictedList() {
        if (mUser != null) {
            gameroomRef.child(GameRoom.RESTRICTED).child(mUser.getUid()).setValue(true);
        }
    }

    /**
     * Removes all users from the restricted list (and the userScores entry if there is one),
     * changes buzzerId to none and questionInProgress to false as a safeguard.
     */
    static void resetGameRoom() {
        gameroomRef.child(GameRoom.BUZZER_ID).setValue("none");
        gameroomRef.child(GameRoom.Q_PROGRESS).setValue(false);
        gameroomRef.child(GameRoom.RESTRICTED).removeValue();
        gameroomRef.child(GameRoom.SCORES).removeValue();
    }
}
