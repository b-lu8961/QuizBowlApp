package app.bryanlu.quizbowl.dbobjects;

import java.util.ArrayList;

/**
 * Created by Bryan Lu on 3/29/2017.
 *
 * Representation of a quiz bowl question.
 */

public class Question {
    private String answer;
    private String body;
    private ArrayList<String> alternateAnswers;

    public Question() {
        answer = "answer";
        body = "body";
    }

    public Question(String answer, String body) {
        this.answer = answer;
        this.body = body;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Removes parenthetical info and trims spaces from this question's answer.
     * @return answer string to be compared with the user's answer
     */
    public String getFormattedAnswer() {
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
        return body != null ? body.equals(question.body) : question.body == null;

    }

    @Override
    public int hashCode() {
        int result = answer != null ? answer.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}
