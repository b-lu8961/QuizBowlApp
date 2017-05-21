package app.bryanlu.quizbowl;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import app.bryanlu.quizbowl.dbobjects.Stats;
import app.bryanlu.quizbowl.dbobjects.User;

import static app.bryanlu.quizbowl.MainActivity.mUser;
import static app.bryanlu.quizbowl.MainActivity.mDatabase;

/**
 * Created by Bryan Lu on 4/14/2017.
 *
 * Fragment that displays the stats of the currently logged in user.
 */

public class StatsFragment extends Fragment {
    public static final int POSITION = 2;

    private TextView usernameText;
    private TextView scoreText;
    private TextView questionsSeenText;
    private TextView questionsCorrectText;
    private TextView questionsIncorrectText;
    private Button resetStatsButton;

    public StatsFragment() {
        // Required constructor for fragments
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_stats, container, false);

        usernameText = (TextView) mView.findViewById(R.id.usernameStats);
        scoreText = (TextView) mView.findViewById(R.id.totalScore);
        questionsSeenText = (TextView) mView.findViewById(R.id.questionsSeen);
        questionsCorrectText = (TextView) mView.findViewById(R.id.questionsCorrect);
        questionsIncorrectText = (TextView) mView.findViewById(R.id.questionsIncorrect);

        DBUtils.getUsername(getActivity(), usernameText, getString(R.string.user_prefix));
        getStats();

        resetStatsButton = (Button) mView.findViewById(R.id.resetStatsButton);
        resetStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetDialog();
            }
        });

        return mView;
    }

    /**
     * Reads the current user's stats from the database.
     */
    private void getStats() {
        if (mUser != null) {
            mDatabase.child(User.USERS).child(mUser.getUid()).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Stats personalStats = dataSnapshot.child(User.STATS).getValue(Stats.class);
                            changeText(personalStats);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Database read failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Changes the UI to reflect the stats obtained by getStats().
     * @param personalStats stats from the getStats() method
     */
    private void changeText(Stats personalStats) {
        int totalScore = personalStats.getTotalScore();
        String scoreString = getString(R.string.total_score) +
                Integer.toString(totalScore);
        scoreText.setText(scoreString);

        int questionsSeen = personalStats.getQuestionsSeen();
        String seenString = getString(R.string.questions_seen) +
                Integer.toString(questionsSeen);
        questionsSeenText.setText(seenString);

        int questionsCorrect = personalStats.getQuestionsCorrect();
        String correctString = getString(R.string.questions_correct) +
                Integer.toString(questionsCorrect);
        questionsCorrectText.setText(correctString);

        int questionsIncorrect = personalStats.getQuestionsIncorrect();
        String incorrectString = getString(R.string.questions_incorrect) +
                Integer.toString(questionsIncorrect);
        questionsIncorrectText.setText(incorrectString);
    }

    /**
     * Resets all of the current user's stats in the database to 0.
     */
    private void resetStats() {
        mDatabase.child(User.USERS).child(mUser.getUid()).child(User.STATS).setValue(new Stats());
        changeText(new Stats());
    }

    /**
     * Shows a confirmation popup to users who want to reset their stats.
     */
    private void showResetDialog() {
        if (mUser != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.stats_alert_title)
                    .setMessage(R.string.stats_alert_message)
                    .setPositiveButton(R.string.dialog_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            resetStats();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        }
    }
}
