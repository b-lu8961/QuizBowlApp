package app.bryanlu.quizbowl.gamestuff;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.bryanlu.quizbowl.DBUtils;
import app.bryanlu.quizbowl.QuestionReader;
import app.bryanlu.quizbowl.R;
import app.bryanlu.quizbowl.firebase.CategoryList;
import app.bryanlu.quizbowl.firebase.GameRoom;
import app.bryanlu.quizbowl.firebase.Question;
import app.bryanlu.quizbowl.sqlite.Category;
import app.bryanlu.quizbowl.sqlite.QuestionPicker;

import static app.bryanlu.quizbowl.MainActivity.mUser;

/**
 * Created by Bryan Lu on 4/14/2017.
 *
 * Fragment that allows users to play questions.
 */
public class PlayFragment extends Fragment {
    public static final int POSITION = 1;
    private TextView answerText;
    private TextView scoreText;
    private TextView connectedText;
    private TextView usernameText;
    private QuestionReader questionReader;
    private Button mainButton;
    private Button gameToggleButton;
    private EditText answerEntry;
    private ImageView resultImageLeft;
    private ImageView resultImageRight;

    private QuestionPicker picker;
    private Question currentQuestion;
    private int wordIndex;
    private ButtonState buttonState = ButtonState.NEXT;

    private int questionNumber = 0;
    private int numCurrentUsers = 0;
    private int numRestrictedUsers = 0;
    private boolean gameInProgress = false;
    private int localScore = 0;
    private int startOfGameScore;
    private String username;
    private PlayListener listener;
    private DatabaseReference gameRoomRef = FirebaseDatabase.getInstance().getReference()
            .child(GameRoom.GAMEROOM);

    public PlayFragment() {
        // Required constructor for fragments
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_play, container, false);
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        usernameText = (TextView) mView.findViewById(R.id.usernameText);
        DBUtils.getUsername(getActivity(), usernameText, getString(R.string.user_prefix));
        scoreText = (TextView) mView.findViewById(R.id.scoreText);
        String scoreString = getString(R.string.score) + Integer.toString(localScore);
        scoreText.setText(scoreString);

        picker = new QuestionPicker(getContext());
        questionReader = (QuestionReader) mView.findViewById(R.id.questionReader);
        questionReader.setMovementMethod(new ScrollingMovementMethod());
        answerText = (TextView) mView.findViewById(R.id.answerText);
        answerText.setVisibility(TextView.INVISIBLE);
        answerEntry = (EditText) mView.findViewById(R.id.answerEntry);
        resultImageLeft = (ImageView) mView.findViewById(R.id.resultImageLeft);
        resultImageRight = (ImageView) mView.findViewById(R.id.resultImageRight);
        mainButton = (Button) mView.findViewById(R.id.mainButton);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMainButtonClick();
            }
        });

        connectedText = (TextView) mView.findViewById(R.id.connectedText);
        gameToggleButton = (Button) mView.findViewById(R.id.gameToggleButton);
        gameToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGameToggleClick();
            }
        });
        if (isConnected()) {
            gameToggleButton.setVisibility(View.VISIBLE);
        }

        listener = new PlayListener();
        GameUtils.checkForGameRoom();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //changeCategoryImages(new ArrayList<String>());
        if (isConnected()) {
            GameUtils.connectToGameRoom(this);
            gameRoomRef.addChildEventListener(listener);
        }
        else {
            connectedText.setText(R.string.no_connection);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isConnected()) {
            if (numCurrentUsers <= 1) {
                GameUtils.resetCategories();
            }
            GameUtils.disconnectFromGameRoom();
            gameRoomRef.removeEventListener(listener);
        }
    }

    /**
     * Checks if the device is connected to the internet.
     * @return true if connected, false if not
     */
    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null;
    }

    /**
     * Gets the question number, current question, and number of users from the database.
     */
    void initializeLocalGame() {
        gameRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                questionNumber = dataSnapshot.child(GameRoom.Q_NUMBER).getValue(Integer.class);
                currentQuestion = dataSnapshot.child(GameRoom.QUESTION).getValue(Question.class);
                numCurrentUsers = (int) dataSnapshot.child(GameRoom.CURRENT).getChildrenCount();
                gameInProgress = dataSnapshot.child(GameRoom.G_PROGRESS).getValue(Boolean.class);
                username = usernameText.getText().toString().substring(getString(R.string.user_prefix).length());

                CategoryList categoryList = dataSnapshot.child(GameRoom.CATEGORIES)
                        .getValue(CategoryList.class);
                //changeCategoryImages(categoryList.toArrayList());
                String newText = Integer.toString(numCurrentUsers) + getString(R.string.connected);
                connectedText.setText(newText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Compares the selected categories to the full set to see which ones need to be colored
     * and which need to be greyed out.
     * @param selectedCategories question categories that were selected
     */
    private void changeCategoryImages(ArrayList<String> selectedCategories) {
        SourceType[] types = SourceType.values();
        for (SourceType type : types) {
            ImageView categoryImage = (ImageView) getActivity().findViewById(type.imageViewId);
            if (categoryImage != null) {
                if (selectedCategories.contains(type.name())) {
                    categoryImage.setColorFilter(null);
                }
                else {
                    categoryImage.setColorFilter(Color.argb(220, 200, 200, 200)); //transparent grey
                }
            }
        }
    }

    /**
     * Updates the question picker to have the right options chosen in the setup fragment.
     * @param categories question categories chosen
     */
    public void updateParameters(ArrayList<Category> categories) {
        picker.setSelectedCategories(categories);
    }

    /**
     * Gets the question that is currently in the database's gameroom.
     * Uses a transaction to make sure currentQuestion is updated properly.
     */
    private void getDatabaseQuestion() {
        gameRoomRef.child(GameRoom.QUESTION).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Question databaseQuestion = mutableData.getValue(Question.class);
                if (databaseQuestion == null) {
                    return Transaction.success(mutableData);
                }

                currentQuestion = databaseQuestion;
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean b, DataSnapshot dataSnapshot) {
                questionReader.read(currentQuestion);
            }
        });
    }

    /**
     * Changes the main button to the NEXT state, shows the entire question text,
     * and displays the answer.
     */
    private void killQuestion() {
        buttonState = ButtonState.NEXT;
        mainButton.setText(buttonState.name());
        questionReader.kill(isConnected());
        questionReader.setText(currentQuestion.getQuestion());
        answerText.setText(currentQuestion.getAnswer().replace("{", "").replace("}", ""));
        answerText.setVisibility(TextView.VISIBLE);
    }

    /**
     * Starts the appropriate action based on this user's buzz order.
     * Called from GameUtils.tryBuzz().
     * @param isFirst is user buzz first in line
     */
    void handleBuzz(boolean isFirst) {
        if (isFirst) {
            promptUserAnswer();
        }
        else {
            Toast.makeText(getContext(), "You lost the buzzer race!", Toast.LENGTH_SHORT).show();
            buttonState = ButtonState.BUZZ;
            mainButton.setText(buttonState.name());
        }
    }

    /**
     * Opens the keyboard and allows the user to enter an answer.
     */
    private void promptUserAnswer() {
        answerEntry.setVisibility(View.VISIBLE);
        answerEntry.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        answerEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkAnswer();
                }
                return false;
            }
        });
    }

    /**
     * Checks if the user's answer is correct and then calls the appropriate actions.
     */
    private void checkAnswer() {
        String realAnswer = currentQuestion.makeFormattedAnswer();
        String userAnswer = answerEntry.getText().toString().trim();
        if (userAnswer.equalsIgnoreCase(realAnswer)) {
            questionReader.setQuestionInProgress(false);
            updateScore(true);
            showResultImages(true);
            Toast.makeText(getContext(), getString(R.string.correct), Toast.LENGTH_SHORT).show();
            if (isConnected()) {
                GameUtils.setQuestionInProgress(false);
            }
            else {
                killQuestion();
            }
        }
        else {
            updateScore(false);
            showResultImages(false);
            Toast.makeText(getContext(), getString(R.string.incorrect), Toast.LENGTH_SHORT).show();
            GameUtils.addToRestrictedList();
            if (!isConnected()) {
                killQuestion();
            }
        }
        GameUtils.resetBuzzInProgress();
    }

    /**
     * Updates the local app's score text, then calls for a database update.
     * @param answerIsCorrect true for a correct answer, false otherwise
     */
    private void updateScore(boolean answerIsCorrect) {
        if (answerIsCorrect) {
            localScore += 10;
            String scoreString = getString(R.string.score) + Integer.toString(localScore);
            scoreText.setText(scoreString);
        }
        else if (questionReader.isQuestionInProgress()) {
            localScore -= 5;
            String scoreString = getString(R.string.score) + Integer.toString(localScore);
            scoreText.setText(scoreString);
        }

        GameUtils.updateStats(answerIsCorrect, questionReader.isQuestionInProgress());
    }

    /**
     * Displays the result images based on the answer outcome.
     * @param answerIsCorrect true for a correct answer, false otherwise
     */
    private void showResultImages(boolean answerIsCorrect) {
        if (answerIsCorrect) {
            resultImageLeft.setImageResource(R.drawable.correct);
            resultImageRight.setImageResource(R.drawable.correct);
            resultImageLeft.setVisibility(View.VISIBLE);
            resultImageRight.setVisibility(View.VISIBLE);
        }
        else {
            resultImageLeft.setImageResource(R.drawable.incorrect);
            resultImageRight.setImageResource(R.drawable.incorrect);
            resultImageLeft.setVisibility(View.VISIBLE);
            resultImageRight.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides the result images, answer text, and answer entry, then changes the main button.
     */
    private void resetGameScreen() {
        resultImageLeft.setVisibility(View.INVISIBLE);
        resultImageRight.setVisibility(View.INVISIBLE);
        answerEntry.setVisibility(View.INVISIBLE);
        answerEntry.getText().clear();
        answerText.setVisibility(TextView.INVISIBLE);
        buttonState = ButtonState.BUZZ;
        mainButton.setText(buttonState.name());
    }

    /**
     * Click listener for the main button.
     */
    private void onMainButtonClick() {
        switch (buttonState) {
            case NEXT:
                currentQuestion = picker.getQuestion();
                if (currentQuestion != null) {
                    resetGameScreen();
                    if (isConnected()) {
                        GameUtils.resetGameRoom();
                        GameUtils.setQuestionInProgress(true);
                    }
                    else {
                        questionReader.read(currentQuestion);
                    }
                }
                break;
            case BUZZ:
                buttonState = ButtonState.SHOW;
                mainButton.setText(buttonState.name());
                if (isConnected()) {
                    GameUtils.tryBuzz(this);
                }
                else {
                    wordIndex = questionReader.pause();
                    promptUserAnswer();
                }
                break;
            case SHOW:
                killQuestion();
                break;
        }
    }

    /**
     * Click listener for the game toggle button.
     */
    private void onGameToggleClick() {
        if (gameInProgress) {
            GameUtils.setGameInProgress(false);
        }
        else {
            GameUtils.setGameInProgress(true);
        }
    }

    /**
     * Contains the possible states for the main button in the Play fragment.
     */
    private enum ButtonState {
        NEXT, BUZZ, SHOW
    }

    /**
     * Has info for each of the asset files containing questions.
     */
    private enum SourceType {;

        final int imageViewId;
        SourceType(int imageViewId) {
            this.imageViewId = imageViewId;
        }
    }

    /**
     * Contains the gameroom database listener and its corresponding methods that are needed
     * by the Play fragment.
     */
    private class PlayListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();
            switch (key) {
                case GameRoom.CURRENT:
                    numCurrentUsers++;
                    break;
                case GameRoom.RESTRICTED:
                    numRestrictedUsers++;
                    checkForEndOfQuestion();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();
            switch (key) {
                case GameRoom.BUZZER_ID:
                    handleBuzzInProgressChange(dataSnapshot.getValue(String.class));
                    break;
                case GameRoom.G_PROGRESS:
                    handleGameInProgressChange(dataSnapshot.getValue(Boolean.class));
                    break;
                case GameRoom.Q_PROGRESS:
                    handleQuestionInProgressChange(dataSnapshot.getValue(Boolean.class));
                    break;
                case GameRoom.Q_NUMBER:
                    handleQuestionNumberChange(dataSnapshot.getValue(Integer.class));
                    break;
                case GameRoom.CURRENT:
                    numCurrentUsers = (int) dataSnapshot.getChildrenCount();
                    String newText = Integer.toString(numCurrentUsers) + getString(R.string.connected);
                    connectedText.setText(newText);
                    break;
                case GameRoom.RESTRICTED:
                    numRestrictedUsers = (int) dataSnapshot.getChildrenCount();
                    checkForEndOfQuestion();
                    break;
                case GameRoom.CATEGORIES:
                    CategoryList categoryList = dataSnapshot.getValue(CategoryList.class);
                    //changeCategoryImages(categoryList.toArrayList());
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            switch (key) {
                case GameRoom.RESTRICTED:
                    numRestrictedUsers = 0;
                    break;
                case GameRoom.CURRENT:
                    numCurrentUsers = 0;
                    break;
                case GameRoom.CATEGORIES:
                    //changeCategoryImages(new ArrayList<String>());
                    break;
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        /**
         * On user buzz, deactivates main button and pauses question.
         * On reset, reactivates main button, then calls actions based on question status.
         * @param buzzerId unique id of user who buzzed
         */
        private void handleBuzzInProgressChange(String buzzerId) {
            if (!buzzerId.equals("none")) {
                wordIndex = questionReader.pause();
                mainButton.setEnabled(false);
                if (!buzzerId.equals(mUser.getUid())) {
                    GameUtils.makeBuzzToast(buzzerId, getContext());
                }
            }
            else {
                mainButton.setEnabled(true);
                if (questionReader.isQuestionInProgress()) {
                    if (buttonState == ButtonState.NEXT) {
                        questionReader.setQuestionInProgress(false);
                        GameUtils.setQuestionInProgress(false);
                    }
                    else {
                        questionReader.resume(currentQuestion, wordIndex);
                    }
                }
            }
        }

        /**
         * On game start, changes the button, initializes startOfGameScore, and shows Toast.
         * On game end, changes the button and sends the score to the database (which starts
         * the process of determining a winner).
         * @param gameStatus true if game is in progress, false if not
         */
        private void handleGameInProgressChange(boolean gameStatus) {
            gameInProgress = gameStatus;
            if (gameInProgress) {
                gameToggleButton.setText(getString(R.string.end_game));
                startOfGameScore = localScore;
                Toast.makeText(getContext(), "Game started.", Toast.LENGTH_SHORT).show();
            }
            else {
                gameToggleButton.setText(getString(R.string.start_game));
                int gameScore = localScore - startOfGameScore;
                GameUtils.sendGameScore(getContext(), numCurrentUsers, username, gameScore);
            }
        }

        /**
         * Prepares local game on question start, kills question on question end.
         * @param questionInProgress true if question started, false otherwise
         */
        private void handleQuestionInProgressChange(boolean questionInProgress) {
            if (questionInProgress) {
                resetGameScreen();
                buttonState = ButtonState.BUZZ;
                mainButton.setText(buttonState.name());
            }
            else {
                killQuestion();
            }
        }

        /**
         * Checks for an update to the question number, then gets new question if there was one.
         * @param newQuestionNumber question number from the database
         */
        private void handleQuestionNumberChange(int newQuestionNumber) {
            if (questionNumber != newQuestionNumber) {
                questionNumber = newQuestionNumber;
                getDatabaseQuestion();
            }
        }

        /**
         * Compares the number of current users and restricted users. If equal, ends the question.
         */
        private void checkForEndOfQuestion() {
            if (numCurrentUsers == numRestrictedUsers) {
                GameUtils.setQuestionInProgress(false);
                questionReader.setQuestionInProgress(false);
            }
        }
    }
}
