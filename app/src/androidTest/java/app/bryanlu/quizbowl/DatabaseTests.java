package app.bryanlu.quizbowl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.bryanlu.quizbowl.dbobjects.GameRoom;
import app.bryanlu.quizbowl.dbobjects.Question;
import app.bryanlu.quizbowl.dbobjects.Stats;
import app.bryanlu.quizbowl.dbobjects.User;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTests {
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = mDatabase.getReference();
    private Stats testStats = new Stats(100, 15, 11, 2);
    private User testUser = new User("testUsername", "test@test.tst", testStats);
    private Question testQuestion = new Question("test answer", "test body");
    private Stats readStats;
    private boolean readBuzz;
    private boolean readGame;
    private boolean readQuestionProgress;
    private Question readQuestion;
    private boolean readRestrictedUser;
    private boolean readCurrentUser;
    private int questionNumber;

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("app.bryanlu.quizbowl", appContext.getPackageName());
    }

    @Test
    public void readUserStats() throws InterruptedException {
        addUserToDatabase();
        mRef.child("users").child("testID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readStats = dataSnapshot.child("personalStats").getValue(Stats.class);
                assertEquals(100, readStats.getTotalScore());
                assertEquals(15, readStats.getQuestionsSeen());
                assertEquals(11, readStats.getQuestionsCorrect());
                assertEquals(2, readStats.getQuestionsIncorrect());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addUserToDatabase() throws InterruptedException{
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mRef.child("users").child("testID").setValue(testUser).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(2, TimeUnit.SECONDS);
    }

    @Test
    public void updateUserStats() {
        updateStats(false, true);
    }

    @Test
    public void readBooleans() throws InterruptedException {
        makeBooleansTrue();
        mRef.child(GameRoom.GAMEROOM).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readBuzz = dataSnapshot.child(GameRoom.BUZZER_ID).getValue(Boolean.class);
                readGame = dataSnapshot.child("gameInProgress").getValue(Boolean.class);
                readQuestionProgress = dataSnapshot.child(GameRoom.Q_PROGRESS).getValue(Boolean.class);
                assertTrue(readBuzz);
                assertTrue(readGame);
                assertTrue(readQuestionProgress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void makeBooleansTrue() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        runBuzzTransaction();
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.Q_PROGRESS).setValue(true);
        mRef.child(GameRoom.GAMEROOM).child("gameInProgress").setValue(true).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(2, TimeUnit.SECONDS);
    }

    /**
     * Copy of method in PlayDBUtils, but without the fragment parameter.
     * Adapted from https://github.com/zillesc/Matchmaker
     */
    private void runBuzzTransaction() {
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.BUZZER_ID).runTransaction(
                new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData.getValue() == null) {
                            return Transaction.success(mutableData);
                        }
                        else if (!mutableData.getValue(Boolean.class)) {
                            mutableData.setValue(true);
                            return Transaction.success(mutableData);
                        }
                        return Transaction.abort();
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean commit,
                                           DataSnapshot dataSnapshot) {

                    }
                });
    }

    @Test
    public void readCurrentQuestion() throws InterruptedException {
        setCurrentQuestion();
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.QUESTION).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readQuestion = dataSnapshot.getValue(Question.class);
                assertEquals(testQuestion.getAnswer(), readQuestion.getAnswer());
                assertEquals(testQuestion.getQuestion(), readQuestion.getQuestion());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setCurrentQuestion() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.QUESTION).setValue(testQuestion)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(2, TimeUnit.SECONDS);
    }

    @Test
    public void readQuestionNumber() throws InterruptedException {
        incrementQuestionNumber();
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.Q_NUMBER).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int databaseNumber = dataSnapshot.getValue(Integer.class);
                assertEquals(questionNumber, databaseNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Copy of method in PlayDBUtils but with a CountDownLatch.
     */
    private void incrementQuestionNumber() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        DatabaseReference questionNumberRef = mRef.child(GameRoom.GAMEROOM).child(GameRoom.Q_NUMBER);
        questionNumberRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    return Transaction.success(mutableData);
                }
                questionNumber = mutableData.getValue(Integer.class);
                questionNumber++;
                mutableData.setValue(questionNumber);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot){

            }
        });
        writeSignal.await(2, TimeUnit.SECONDS);
    }

    @Test
    public void readUsers() throws InterruptedException {
        addUsers();
        mRef.child(GameRoom.GAMEROOM).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        readCurrentUser = dataSnapshot.child(GameRoom.CURRENT)
                                .child("testID").exists();
                        readRestrictedUser = dataSnapshot.child(GameRoom.RESTRICTED)
                                .child("testID").exists();
                        assertTrue(readCurrentUser);
                        assertTrue(readRestrictedUser);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private void addUsers() throws InterruptedException {
        final CountDownLatch writeSignal = new CountDownLatch(1);
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.CURRENT).child("testID").setValue(true);
        mRef.child(GameRoom.GAMEROOM).child(GameRoom.RESTRICTED).child("testID").setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        writeSignal.countDown();
                    }
                });
        writeSignal.await(2, TimeUnit.SECONDS);
    }

    /**
     * Copy of method in PlayDBUtils only with references changed.
     * @param correct is user answer correct
     * @param running was question still running at buzz
     */
    private void updateStats(boolean correct, boolean running) {
        final boolean answerCorrect = correct;
        final boolean stillRunning = running;
        DatabaseReference statsReference = mRef.child("users").child("testID")
                .child("personalStats");
        statsReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int totalScore;
                int questionsSeen;
                int questionsCorrect;
                int questionsIncorrect;
                if (mutableData.getValue(Stats.class) == null) {
                    return Transaction.success(mutableData);
                }
                else {
                    questionsSeen = mutableData.child(Stats.SEEN).getValue(Integer.class);
                    totalScore = mutableData.child(Stats.SCORE).getValue(Integer.class);
                    questionsCorrect = mutableData.child(Stats.CORRECT)
                            .getValue(Integer.class);
                    questionsIncorrect = mutableData.child(Stats.INCORRECT)
                            .getValue(Integer.class);
                }

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
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {

            }
        });

    }
}
