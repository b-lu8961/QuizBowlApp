package app.bryanlu.quizbowl.dbobjects;

import java.util.ArrayList;

/**
 * Created by Bryan Lu on 3/29/2017.
 *
 * Representation of a quiz bowl question.
 */

public class Question {
    private String category;
    private String subcategory;
    private String difficulty;
    private String tournament;
    private String source;
    private int num;
    private int year;
    private String round;
    private String answer;
    private String question;
    private ArrayList<String> alternateAnswers;

    public Question() {
        answer = "answer";
        question = "question";
    }

    public Question(String answer, String question) {
        this.answer = answer;
        this.question = question;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getTournament() {
        return tournament;
    }

    public String getSource() {
        return source;
    }

    public int getYear() {
        return year;
    }

    public int getNum() {
        return num;
    }

    public String getRound() {
        return round;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestion() {
        return question;
    }

    /**
     * Concatenates the torunament, year, round, and question number into one string, with each
     * element separated by an underscore.
     * @return unique string id for use in the Firebase database
     */
    public String makeStringId() {
        String id = tournament + "_";
        id += year + "_";
        id += round + "_";
        id += num;
        return id;
    }

    /**
     * Removes parenthetical info and trims spaces from this question's answer.
     * @return answer string to be compared with the user's answer
     */
    public String makeFormattedAnswer() {
        String realAnswer;
        if (answer.contains("[")) {
            realAnswer = answer.split("\\[")[0];
        }
        else if (answer.contains("(")) {
            realAnswer = answer.split("\\(")[0];
        }
        else {
            realAnswer = answer;
        }
        return realAnswer.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (answer != null ? !answer.equals(question.answer) : question.answer != null)
            return false;
        return this.question != null ? this.question.equals(question.question) : question.question == null;

    }

    @Override
    public int hashCode() {
        int result = answer != null ? answer.hashCode() : 0;
        result = 31 * result + (question != null ? question.hashCode() : 0);
        return result;
    }
}
