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

import app.bryanlu.quizbowl.dbobjects.Question;
import app.bryanlu.quizbowl.gamestuff.PlayFragment;
import app.bryanlu.quizbowl.gamestuff.PlayMenuFragment;
import app.bryanlu.quizbowl.gamestuff.SetupFragment;

public class MainActivity extends AppCompatActivity
        implements SetupFragment.OnCheckboxClickedListener {
    private ListView mDrawerList;
    private ArrayAdapter<String> drawerAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment playMenuFragment;
    private Fragment signInFragment;
    private Fragment statsFragment;
    private final String PLAY_TAG = "PLAY_MENU_FRAGMENT";
    private final String SIGN_IN_TAG = "SIGN_IN_FRAGMENT";
    private final String STATS_TAG = "STATS_FRAGMENT";
    public static FirebaseAuth mAuth;
    public static FirebaseUser mUser;
    public static DatabaseReference mDatabase;


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
        mDatabase.child("questions").removeValue();

        playMenuFragment = new PlayMenuFragment();
        signInFragment = new AccountFragment();
        statsFragment = new StatsFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, playMenuFragment, PLAY_TAG)
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
        String tagToUse = null;
        switch(position) {
            case PlayMenuFragment.POSITION:
                fragmentToUse = playMenuFragment;
                tagToUse = PLAY_TAG;
                break;
            case AccountFragment.POSITION:
                fragmentToUse = signInFragment;
                tagToUse = SIGN_IN_TAG;
                break;
            case StatsFragment.POSITION:
                fragmentToUse = statsFragment;
                tagToUse = STATS_TAG;
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragmentToUse, tagToUse)
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

    @Override
    public void onCategoryChange(ArrayList<String> categories) {
        PlayMenuFragment fragment = (PlayMenuFragment)getSupportFragmentManager()
                .findFragmentByTag(PLAY_TAG);

        fragment.setSelectedCategories(categories);
    }
}

