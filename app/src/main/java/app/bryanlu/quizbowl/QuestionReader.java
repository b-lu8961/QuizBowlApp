package app.bryanlu.quizbowl;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import app.bryanlu.quizbowl.firebase.Question;

/**
 * Created by Bryan Lu on 4/4/2017.
 *
 * Class that allows a TextView to display text one word at a time.
 * Adapted from
 * http://stackoverflow.com/questions/6700374/android-character-by-character-display-text-animation
 */

public class QuestionReader extends AppCompatTextView {
    private String[] questionText;
    private int wordIndex;
    private String textToDisplay;
    private static int DELAY_IN_MS = 150;
    private boolean isRunning;
    private boolean questionInProgress;

    public QuestionReader(Context context) {
        super(context);
    }

    public QuestionReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler handler = new Handler();

    private Runnable wordAdder = new Runnable() {
        /**
         * Adds one word to textToDisplay, sets the text, then calls itself to keep adding words.
         */
        @Override
        public void run() {
            textToDisplay = textToDisplay + questionText[wordIndex] + " ";
            setText(textToDisplay);
            wordIndex++;
            if (wordIndex >= questionText.length) {
                questionInProgress = false;
            }
            if (!questionInProgress) {
                wordIndex = questionText.length;
                setText(generateText());
            }
            else if (wordIndex < questionText.length && isRunning) {
                handler.postDelayed(wordAdder, DELAY_IN_MS);
            }
        }
    };

    public void setQuestionInProgress(boolean inProgress) {
        questionInProgress = inProgress;
    }

    public boolean isQuestionInProgress() {
        return questionInProgress;
    }

    /**
     * Starts reading the question by using the Runnable defined in this class.
     * @param currentQuestion the question to read
     */
    public void read(Question currentQuestion) {
        questionText = currentQuestion.getQuestion().split(" ");
        wordIndex = 0;
        textToDisplay = "";
        isRunning = true;
        questionInProgress = true;

        setText(textToDisplay);
        handler.removeCallbacks(wordAdder);
        handler.postDelayed(wordAdder, DELAY_IN_MS);
    }

    /**
     * Temporarily stop reading the question (and display partial question text).
     * @return the word index at pause time
     */
    public int pause() {
        isRunning = false;
        setText(textToDisplay);
        return wordIndex;
    }

    /**
     * Resume reading the question at the word index specified by the parameter currentIndex.
     * @param currentQuestion question to resume reading
     * @param currentIndex spot at which to resume
     */
    public void resume(Question currentQuestion, int currentIndex) {
        questionText = currentQuestion.getQuestion().split(" ");
        if (currentIndex < questionText.length) {
            wordIndex = currentIndex;
            textToDisplay = generateText();
            isRunning = true;
            questionInProgress = true;

            setText(textToDisplay);
            handler.removeCallbacks(wordAdder);
            handler.postDelayed(wordAdder, DELAY_IN_MS);
        }
        else {
            setText(currentQuestion.getQuestion());
        }
    }

    /**
     * Stop reading the question and display the entire question text (if not connected).
     * @param isConnected true if device is connected to the internet, false if not
     */
    public void kill(boolean isConnected) {
        isRunning = false;
        if (!isConnected) {
            wordIndex = questionText.length;
            setText(generateText());
        }
    }

    /**
     * Creates a string of the words in the questionText array up to the index specified by
     * wordIndex.
     * @return the question text up until the word at wordIndex
     */
    private String generateText() {
        StringBuilder pastText = new StringBuilder();
        for (int i = 0; i < wordIndex; i++) {
            pastText.append(questionText[i]);
            pastText.append(" ");
        }
        return pastText.toString();
    }
}
