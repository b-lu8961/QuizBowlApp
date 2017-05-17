package app.bryanlu.quizbowl;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import app.bryanlu.quizbowl.dbobjects.Stats;
import app.bryanlu.quizbowl.dbobjects.User;

import static app.bryanlu.quizbowl.MainActivity.mAuth;
import static app.bryanlu.quizbowl.MainActivity.mUser;
import static app.bryanlu.quizbowl.MainActivity.mDatabase;

/**
 * Created by Bryan Lu on 4/14/2017.
 *
 * Fragment that allows users to sign in and out using their Google account.
 */

public class AccountFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener {
    public static final int POSITION = 1;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "Google Sign-In Activity";

    private TextView accountEmail;
    private TextView changeUsernameText;
    private SignInButton signInButton;
    private Button signOutButton;
    private EditText usernameEntry;

    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleClient;

    private FirebaseAuth.AuthStateListener mAuthListener;

    public AccountFragment() {
        // Required constructor for fragments
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (gso == null) {
            gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
        }
        if (mGoogleClient == null) {
            mGoogleClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity() /* Activity */,
                            this /* OnConnectionFailed Listener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    Log.d(TAG, "onAuthStateChanged: " + mUser.getUid());
                    updateUI(true);
                }
                else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_account, container, false);

        accountEmail = (TextView) mView.findViewById(R.id.accountEmail);
        signInButton = (SignInButton) mView.findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        signInButton.setVisibility(View.INVISIBLE);

        signOutButton = (Button) mView.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        changeUsernameText = (TextView) mView.findViewById(R.id.changeUsernameText);
        usernameEntry = (EditText) mView.findViewById(R.id.usernameEntry);
        DBUtils.getUsername(getActivity(), usernameEntry, "");
        usernameEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    changeUsername();
                }
                return false;
            }
        });

        if (MainActivity.mUser == null) {
            signIn();
        }
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Shows the Google account selection window so the user can sign in.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Signs the user out from their Google account and Firebase.
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(false);
                    }
                }
        );
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Links the selected Google account to the app and to Firebase using the result passed in.
     * Also calls for UI changes.
     * @param result sign in result passed in from onActivityResult()
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            authenticateFirebaseWithGoogle(account);
        }
        else {
            updateUI(false);
        }
    }


    /**
     * Handles the Firebase side of user authentication using Google accounts.
     * @param account account selected from the Google authentication process
     */
    private void authenticateFirebaseWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "authenticateFirebaseWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        mUser = mAuth.getCurrentUser();
                        if (mUser != null) {
                            checkForUserInDB(mUser.getUid());
                        }
                        updateUI(true);

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential: " + task.getException());
                            Toast.makeText(getContext(), "Auth failed", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    /**
     * Creates a new user in the database if the current user is not already in it.
     * @param userExists true if user already exists in the database, false if not
     */
    private void addUserToDatabase(boolean userExists) {
        if (!userExists) {
            User dbUser = new User(mUser.getDisplayName(), mUser.getEmail(), new Stats());
            mDatabase.child(User.USERS).child(mUser.getUid()).setValue(dbUser);
        }
        else {
            // Do nothing if mUser is null
        }
    }

    /**
     * Checks to see if there is a user with the userId specified by the parameter
     * @param userId unique id of currently logged in user
     */
    private void checkForUserInDB(String userId) {
        final String currentID = userId;
        mDatabase.child(User.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean check = dataSnapshot.child(currentID).exists();
                addUserToDatabase(check);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Updates the components of the UI based on whether there is a user that is signed in
     * @param signedIn true if a user is signed in, false if not
     */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            signInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            changeUsernameText.setVisibility(View.VISIBLE);
            usernameEntry.setVisibility(View.VISIBLE);
            accountEmail.setText(mUser.getEmail());
        }
        else {
            signOutButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            changeUsernameText.setVisibility(View.INVISIBLE);
            usernameEntry.setVisibility(View.INVISIBLE);
            accountEmail.setText(getString(R.string.not_logged_in));
        }
    }

    /**
     * Changes the username in the database of the current user to the text entered
     * in the usernameEntry EditText.
     */
    private void changeUsername() {
        if (mUser != null) {
            String newUsername = usernameEntry.getText().toString();
            mDatabase.child(User.USERS).child(mUser.getUid()).child(User.NAME)
                    .setValue(newUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Username changed.", Toast.LENGTH_SHORT)
                                .show();
                    }
                    else {
                        Toast.makeText(getContext(), "Error changing username", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }
        else {
            Toast.makeText(getContext(),R.string.not_logged_in, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }
}
