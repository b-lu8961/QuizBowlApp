package app.bryanlu.quizbowl.dbobjects;

/**
 * Created by Bryan Lu on 4/16/2017.
 *
 * Class containing the stats for each user.
 */

public class Stats {
    public static final String SCORE = "totalScore";
    public static final String SEEN = "questionsSeen";
    public static final String CORRECT = "questionsCorrect";
    public static final String INCORRECT = "questionsIncorrect";
    private int totalScore;
    private int questionsSeen;
    private int questionsCorrect;
    private int questionsIncorrect;

    public Stats() {
        this.totalScore = 0;
        this.questionsSeen = 0;
        this.questionsCorrect = 0;
        this.questionsIncorrect = 0;
    }

    public Stats(int totalScore, int questionsSeen, int questionsCorrect, int questionsIncorrect) {
        this.totalScore = totalScore;
        this.questionsSeen = questionsSeen;
        this.questionsCorrect = questionsCorrect;
        this.questionsIncorrect = questionsIncorrect;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getQuestionsSeen() {
        return questionsSeen;
    }

    public int getQuestionsCorrect() {
        return questionsCorrect;
    }

    public int getQuestionsIncorrect() {
        return questionsIncorrect;
    }


}
