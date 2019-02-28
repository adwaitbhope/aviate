package com.piedpipergeeks.aviate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore fsClient;
    FirebaseDatabase database;

    ProgressBar progressBar;
    String userType = "user";

    SharedPreferences preferences;
    private static final String MyPREFERENCES = "MyPrefs";
    private static final String USER_ID = "userId";
    private static final String EMAIL = "email";
    private static final String USER_TYPE = "userType";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHONE_NUMBER = "phoneNumber";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        startActivity(new Intent(MainActivity.this, PickClubActivity.class));

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//             public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        mAuth = FirebaseAuth.getInstance();

        fsClient = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        fsClient.setFirestoreSettings(settings);

        progressBar = (ProgressBar) findViewById(R.id.auto_login_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

//        database = FirebaseDatabase.getInstance();
//
//        database.getReference("deAsra")
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                        String child = dataSnapshot.toString();
////                        Toast.makeText(MainActivity.this, child, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
//        String uniqueKey = database.getReference("deAsra")
//                .push()
//                .getKey();
//
//        Map<String, Object> message = new HashMap<>();
//        message.put("text", "Hello how are you?");
//        message.put("senderId", "afhdksgfisd");
//
//        database.getReference("deAsra")
//                .child(uniqueKey)
//                .setValue(message);



        sharedPreferencesLogin();

//        defaultLogin();
////        DefaultLogin();

    }


//    @Override
//    public void onFragmentInteraction(Uri uri) {
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void toSignInActivity(View view) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void toSignUpeEntrepreneurActivity(View view) {
        startActivity(new Intent(MainActivity.this, RegisterEntrepreneurActivity.class));
    }

    public void DefaultLogin() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent homeScreenIntent;
            if (userType.equals("user")) {
//                Toast.makeText(this, "Default is user", Toast.LENGTH_SHORT).show();
                homeScreenIntent = new Intent(MainActivity.this, HomeScreenUserActivity.class);
            } else {
                homeScreenIntent = new Intent(MainActivity.this, HomeScreenActivity.class);
            }
            startActivity(homeScreenIntent);
            finish();
        }
    }

    private void sharedPreferencesLogin() {
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("UserId")) {
            Log.d("SHARED PREF", "SHARED PREFERENCES CONTAINS USER ID");
            if (sharedPreferences.getString("UserType", "").equals("user")) {
                startActivity(new Intent(MainActivity.this, HomeScreenUserActivity.class));
            } else if (sharedPreferences.getString("UserType", "").equals("admin")) {
                startActivity(new Intent(MainActivity.this, HomeScreenActivity.class));
            }
        } else {
            defaultLogin();
        }
    }

    private void getUserData() {

        String userId = mAuth.getUid();

        fsClient.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Profile user = document.toObject(Profile.class);
                            saveDataLocally(user);
                        }
                    }
                });


    }

    public void saveDataLocally(Profile user) {
        preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FIRST_NAME, user.getFirstName());
        editor.putString(LAST_NAME, user.getLastName());
        editor.putString(USER_ID, user.getUserId());
        editor.putString(EMAIL, user.getPhoneNumber());
        editor.putString(USER_TYPE, user.getUserType());
        editor.putString(PHONE_NUMBER, user.getPhoneNumber());
        editor.apply();
    }

    public void defaultLogin() {
        currentUser = mAuth.getCurrentUser();
        String userId;

        if (currentUser != null && currentUser.isEmailVerified()) {
            userId = currentUser.getUid();

            fsClient.collection("Users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
//                                Toast.makeText(MainActivity.this, "Query successful", Toast.LENGTH_SHORT).show();
                                DocumentSnapshot snapshot = task.getResult();
                                try {
                                    if (snapshot.get("userType").equals("user")) {
                                        getUserData();
                                        startActivity(new Intent(MainActivity.this, HomeScreenUserActivity.class));
                                        finish();
                                    } else if (snapshot.get("userType").equals("admin")) {
                                        getUserData();
                                        startActivity(new Intent(MainActivity.this, HomeScreenActivity.class));
                                        finish();
                                    }
                                } catch (Exception e) {
                                    Log.d("QUERY", e.toString());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(MainActivity.this, "Query failed, check logs", Toast.LENGTH_SHORT).show();
                            Log.d("QUERY", e.toString());
                        }
                    });
        }
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
