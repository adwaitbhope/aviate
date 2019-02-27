package com.piedpipergeeks.aviate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore fsClient;
    String userType = "user";

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

        defaultLogin();
        DefaultLogin();

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

    public void toSignUpdeAsraActivity(View view) {
        startActivity(new Intent(MainActivity.this, RegisterdeAsraActivity.class));
    }

    public void DefaultLogin() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent homeScreenIntent;
            if (userType.equals("user")) {
                Toast.makeText(this, "Default is user", Toast.LENGTH_SHORT).show();
                homeScreenIntent = new Intent(MainActivity.this, HomeScreenUserActivity.class);
            } else {
                homeScreenIntent = new Intent(MainActivity.this, HomeScreenActivity.class);
            }
            startActivity(homeScreenIntent);
            finish();
        }
    }

    public void defaultLogin() {
        currentUser = mAuth.getCurrentUser();
        String userId;
        if (currentUser != null && currentUser.isEmailVerified()) {
            userId = currentUser.getUid();
        } else {
            userId = "null";
        }
        fsClient.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Query successful", Toast.LENGTH_SHORT).show();
                            DocumentSnapshot snapshot = task.getResult();
                            try {
                                if (snapshot.get("userType").equals("user")) {
//                                function call is checked
//                                startActivity(new Intent(MainActivity.this, HomeScreenUserActivity.class));
//                                finish();
                                } else {
//                                startActivity(new Intent(MainActivity.this, HomeScreenActivity.class));
//                                finish();
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
                        Toast.makeText(MainActivity.this, "Query failed, check logs", Toast.LENGTH_SHORT).show();
                        Log.d("QUERY", e.toString());
                    }
                });
    }
}
