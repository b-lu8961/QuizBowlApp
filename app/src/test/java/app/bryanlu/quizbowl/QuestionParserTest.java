package app.bryanlu.quizbowl;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import app.bryanlu.quizbowl.dbobjects.Question;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class QuestionParserTest {
    @Test
    public void testGetLines() throws IOException {
        String testFile = "test.txt";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(testFile);
        ArrayList<String> lines = QuestionParser.getLines(in);

        assertEquals(5, lines.size());

        String answerLine = lines.get(lines.size() - 1);
        assertTrue(answerLine.startsWith("ANSWER:"));
    }

    @Test
    public void testParseQuestions() throws IOException {
        String testFile = "test.txt";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(testFile);
        ArrayList<String> lines = QuestionParser.getLines(in);

        ArrayList<Question> questions = QuestionParser.parseQuestions(lines);
        Question testQuestion = questions.get(0);
        assertEquals("tests", testQuestion.getAnswer());
        String answer = "These things are used to make sure a program's logic is correct. " +
                "In CS 125 and CS 126, the JUnit framework is used to implement these " +
                "things. For 10 points, name this thing that shares its name with a " +
                "method of evaluating a student's knowledge. ";
        assertEquals(answer, testQuestion.getQuestion());
    }
}