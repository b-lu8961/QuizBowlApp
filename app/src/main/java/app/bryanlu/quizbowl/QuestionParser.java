package app.bryanlu.quizbowl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import app.bryanlu.quizbowl.dbobjects.Question;

/**
 * Created by Bryan Lu on 3/29/2017.
 *
 * Reads text files and creates the appropriate questions.
 */
class QuestionParser {
    private static final String ANSWER_START = "ANSWER: ";

    /**
     * Creates Question objects from the arraylist of lines passed in.
     * @param fileLines lines from a text file containing questions
     * @return an arraylist of Questions
     */
    static ArrayList<Question> parseQuestions(ArrayList<String> fileLines) {
        ArrayList<Question> questionList = new ArrayList<>();
        int startLineNum = 0;
        for (int i = 0; i < fileLines.size(); i++) {
            String currentLine = fileLines.get(i);
            if (currentLine.startsWith(ANSWER_START)) {
                String answer = currentLine.substring(ANSWER_START.length());

                StringBuilder bodyBuilder = new StringBuilder();
                for (int bodyLineNum = startLineNum; bodyLineNum < i; bodyLineNum++) {
                    String bodyLine = fileLines.get(bodyLineNum);
                    bodyBuilder.append(bodyLine);
                    if (!bodyLine.endsWith(" ")) {
                        bodyBuilder.append(" ");
                    }
                }
                startLineNum = i + 1;

                questionList.add(new Question(answer, bodyBuilder.toString()));
            }
        }

        return questionList;
    }

    /**
     * Gets lines of text from an input stream and returns them in an arraylist.
     * @param stream input stream from a text file
     * @return an arraylist of strings generated from the input stream
     * @throws FileNotFoundException bad input stream file reference
     * @throws IOException problem reading file
     */
    static ArrayList<String> getLines(InputStream stream)
            throws FileNotFoundException, IOException {
        ArrayList<String> fileLines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine();
            while (line != null) {
                fileLines.add(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found");
        } catch (IOException e) {
            throw new IOException("Problem reading file");
        }

        return fileLines;
    }
}
