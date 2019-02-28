package com.piedpipergeeks.aviate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.w3c.dom.Text;

import android.app.ProgressDialog;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth lAuth;
    private FirebaseFirestore fsClient;
    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private FirebaseUser currentUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lAuth = FirebaseAuth.getInstance();

        fsClient = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        fsClient.setFirestoreSettings(settings);

        emailEditText = (EditText) findViewById(R.id.email_login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_login_edittext);
        signInButton = (Button) findViewById(R.id.signin_login_button);

        signIn();
    }

    private void showHelp() {
        TextView help = (TextView) findViewById(R.id.extra_text_textview);
        TextView reset_password = (TextView) findViewById(R.id.reset_password_textview);
        TextView resend_email = (TextView) findViewById(R.id.verify_email_textview);

        help.setVisibility(View.VISIBLE);
        reset_password.setVisibility(View.VISIBLE);
        resend_email.setVisibility(View.VISIBLE);
    }

    public void resetPassword(View view) {
        Toast.makeText(this, "Function called", Toast.LENGTH_SHORT).show();

        // Do something to update user's password

    }

    public void resendVerificationEmail(View view) {
        Toast.makeText(LoginActivity.this, "Function called", Toast.LENGTH_SHORT).show();
    }

    protected void signIn() {
        signInButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                pd.setMessage("Loading...");
                pd.show();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    lAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                if (lAuth.getCurrentUser().isEmailVerified()) {

                                    toHomeScreen();

                                    final SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                    if (pref.contains("emailVerified")) {
                                        if (! Boolean.valueOf(pref.getString("emailVerified", ""))) {
                                            fsClient.collection("Users")
                                                    .document(lAuth.getCurrentUser().getUid())
                                                    .update("emailVerified", true)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                        }
                                                    });
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("emailVerified", String.valueOf(true));
                                            editor.apply();
                                        }
                                    }
                                    else {

                                        fsClient.collection("Users")
                                                .document(lAuth.getUid())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot snapshot = task.getResult();
                                                            Profile user = snapshot.toObject(Profile.class);

                                                            SharedPreferences.Editor editor = pref.edit();
                                                            editor.putString("userId", user.getUserId());
                                                            editor.putString("userType", user.getUserType());
                                                            editor.putString("firstName", user.getFirstName());
                                                            editor.putString("lastName", user.getLastName());
                                                            editor.putString("email", user.getEmail());
                                                            editor.putString("emailVerified", String.valueOf(user.isEmailVerified()));
                                                            editor.putString("phoneNumber", user.getPhoneNumber());
                                                            editor.apply();
                                                        }
                                                    }
                                                });

                                    }

                                } else {
                                    Toast.makeText(LoginActivity.this, "Please verify your email first", Toast.LENGTH_SHORT).show();
                                    showHelp();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                showHelp();
                            }
                        }
                    });

                } else if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter the email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter the password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Problem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    protected void toHomeScreen() {
//        Intent homeScreenIntent;
//        if (userType.equals("user")) {
//            homeScreenIntent = new Intent(LoginActivity.this, HomeScreenUserActivity.class);
//        } else {
//            homeScreenIntent = new Intent(LoginActivity.this, HomeScreenActivity.class);
//        }
//        startActivity(homeScreenIntent);
//        finish();
//    }

    public void toHomeScreen() {
        currentUser = lAuth.getCurrentUser();
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
//                                Toast.makeText(LoginActivity.this, "Query successful", Toast.LENGTH_SHORT).show();
                                DocumentSnapshot snapshot = task.getResult();
                                try {
                                    if (snapshot.get("userType").equals("user")) {
                                        startActivity(new Intent(LoginActivity.this, HomeScreenUserActivity.class));
                                        finish();
                                    } else if (snapshot.get("userType").equals("admin")) {
                                        startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class));
                                        finish();
                                    }
                                } catch (Exception e) {
                                    Log.d("QUERY", e.toString());
                                    startActivity(new Intent(LoginActivity.this, HomeScreenUserActivity.class));
                                    }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(LoginActivity.this, "Query failed, check logs", Toast.LENGTH_SHORT).show();
                            Log.d("QUERY", e.toString());
                        }
                    });
        }
    }


}
