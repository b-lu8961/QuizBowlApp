package app.bryanlu.quizbowl;

import android.app.Activity;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import app.bryanlu.quizbowl.firebase.User;

import static app.bryanlu.quizbowl.MainActivity.mUser;
import static app.bryanlu.quizbowl.MainActivity.mDatabase;

/**
 * Created by Bryan Lu on 4/17/2017.
 *
 * Utility class containing methods for reading and writing data in the Firebase database.
 */
public class DBUtils {
    /**
     * Gets the current user's username and sets the TextView to show it with a prefix put in front.
     * @param activity activity needed to access string resources
     * @param usernameText TextView to change
     * @param prefix string to put in front of the username
     */
    public static void getUsername(Activity activity, TextView usernameText, String prefix) {
        if (mUser != null) {
            final TextView mUsernameText = usernameText;
            final String mPrefix = prefix;
            mDatabase.child(User.USERS).child(mUser.getUid()).child(User.NAME).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String username = mPrefix + dataSnapshot.getValue(String.class);
                    mUsernameText.setText(username);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            String noUsername = prefix + activity.getString(R.string.no_user);
            usernameText.setText(noUsername);
        }
    }

}
