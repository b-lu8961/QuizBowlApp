package app.bryanlu.quizbowl;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.bryanlu.quizbowl.dbobjects.Question;

public class MainActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> drawerAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment playFragment;
    private Fragment signInFragment;
    private Fragment statsFragment;
    static FirebaseAuth mAuth;
    static FirebaseUser mUser;
    static DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        setupDrawer();

        mDrawerList = (ListView) findViewById(R.id.drawerList);
        addDrawerItems();
        mDrawerList.setItemChecked(PlayFragment.POSITION, true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        playFragment = new PlayFragment();
        signInFragment = new AccountFragment();
        statsFragment = new StatsFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, playFragment)
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Run one time only to initialize database
        //addProtobowlQuestions();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Sets the adapter and listener for the navigation drawer. This puts data in and allows
     * each item to be clicked.
     */
    private void addDrawerItems() {
        final String[] drawerItems = getResources().getStringArray(R.array.drawer_options);
        drawerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerItems);
        mDrawerList.setAdapter(drawerAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectItem(position);
            }
        });
    }

    /**
     * Changes fragments based on which item is selected in the navigation drawer/
     * @param position positional number of the item selected in the navigation drawer
     */
    private void selectItem(int position) {
        Fragment fragmentToUse = null;
        switch(position) {
            case PlayFragment.POSITION:
                fragmentToUse = playFragment;
                break;
            case AccountFragment.POSITION:
                fragmentToUse = signInFragment;
                break;
            case StatsFragment.POSITION:
                fragmentToUse = statsFragment;
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragmentToUse)
                .commit();

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Sets up necessary things for the navigation drawer to function.
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    /* Everything below is run only once for initialization */
    /**
     * Reads the ProtobowlQuestions file and passes the lines to the
     * AddQuestions AsyncTask. Two AsyncTasks are used to reduce memory load.
     */
    private void addProtobowlQuestions() {
        try {
            // The json file is removed from the assets directory to make the app smaller
            InputStream stream = null;
            //stream = getAssets().open("ProtobowlQuestions.json");
            ArrayList<String> fileLines = QuestionParser.getLines(stream);
            String[] lineArray = fileLines.toArray(new String[0]);
            String[] firstHalf = Arrays.copyOfRange(lineArray, 0, lineArray.length / 2);
            String[] secondHalf = Arrays.copyOfRange(lineArray, (lineArray.length / 2) + 1,
                    lineArray.length - 1);

            Toast.makeText(this, "Question parsing starting.", Toast.LENGTH_SHORT).show();
            new AddQuestionsTask().execute(firstHalf);
            new AddQuestionsTask().execute(secondHalf);

        } catch (IOException e) {
            Toast.makeText(this, "Protobowl file not found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Puts questions into the Firebase database.
     * @param questionList arraylist of questions to add
     */
    private static void addQuestionToDatabase(Question[] questionList) {
        DatabaseReference questionsRef = mDatabase.child("questions");
        for (Question question : questionList) {
            questionsRef.child(question.getCategory()).child(question.getDifficulty())
                    .child(question.makeStringId()).setValue(question);
        }
    }

    /**
     * Recursively parses questions from the lines of the ProtobowlQuestions file using smaller
     * sections of lines to avoid running out of memory.
     */
    private class AddQuestionsTask extends AsyncTask<String, Void, String[]> {
        protected String[] doInBackground(String... lines) {
            String[] subArray;
            String[] leftover;
            if (lines.length > 9999) {
                subArray = Arrays.copyOfRange(lines, 0, 9999);
                leftover = Arrays.copyOfRange(lines, 10000, lines.length);
            }
            else {
                subArray = Arrays.copyOfRange(lines, 0, lines.length);
                leftover = null;
            }

            Question[] questionList = QuestionParser.parseQuestionsFromFile(subArray);
            addQuestionToDatabase(questionList);
            return leftover;
        }

        @Override
        protected void onPostExecute(String[] leftover) {
            super.onPostExecute(leftover);
            if (leftover != null) {
                Toast.makeText(getApplicationContext(), "Parsing progress.",
                        Toast.LENGTH_SHORT).show();
                new AddQuestionsTask().execute(leftover);
            }
            else {
                Toast.makeText(getApplicationContext(), "Parsing complete.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

