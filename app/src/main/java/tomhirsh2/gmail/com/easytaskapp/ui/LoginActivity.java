package tomhirsh2.gmail.com.easytaskapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import tomhirsh2.gmail.com.easytaskapp.R;
import tomhirsh2.gmail.com.easytaskapp.TaskHome;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmail, mPassword;
    private Button btnSignIn, btnRegister;
    private ProgressBar mProgressBar;
    //private ImageView imageLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        btnSignIn = findViewById(R.id.email_sign_in_button);
        btnRegister = findViewById(R.id.link_register);
        mProgressBar = findViewById(R.id.progressBar);
        //imageLanguage = findViewById(R.id.login_language);

        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerActivity);
            }
        });
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Disables back button
    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.DisableBackButton), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /*
    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }
     */

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent taskHome = new Intent(LoginActivity.this, TaskHome.class);
            startActivity(taskHome);
        }
    }

    private void signIn() {
        // Check if the fields are filled out
        if(!isEmpty(mEmail.getText().toString()) && !isEmpty(mPassword.getText().toString())) {
            Log.d(TAG, "onClick: attempting to authenticate");
            showDialog();

            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        hideDialog();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        hideDialog();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.ErrorSignIn), Toast.LENGTH_LONG).show();
                        //updateUI(null);
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.NotAllFilled), Toast.LENGTH_LONG).show();
        }
    }
}
