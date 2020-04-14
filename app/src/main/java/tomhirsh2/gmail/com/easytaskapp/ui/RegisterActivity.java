package tomhirsh2.gmail.com.easytaskapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import tomhirsh2.gmail.com.easytaskapp.R;
import tomhirsh2.gmail.com.easytaskapp.models.User;

import static android.text.TextUtils.isEmpty;
import static tomhirsh2.gmail.com.easytaskapp.util.Check.doStringsMatch;

public class RegisterActivity extends AppCompatActivity {


    private static final String TAG = "RegisterActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmail, mPassword, mConfirmPassword;
    private Button btnRegister, btnSignIn;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        btnSignIn = findViewById(R.id.link_sign_in);
        btnRegister = findViewById(R.id.btn_register);
        mProgressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginActivity);
            }
        });
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        //finish();
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Disables back button
    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.DisableBackButton), Toast.LENGTH_SHORT).show();
    }

    /*
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }
    */

    private void updateUI(FirebaseUser user) {
        if (user != null) {

        } else {

        }
    }

    private void register() {
        Log.d(TAG, "onClick: attempting to register.");
        // Check if the fields are filled out
        if (!isEmpty(mEmail.getText().toString()) && !isEmpty(mPassword.getText().toString()) && !isEmpty(mConfirmPassword.getText().toString())) {
            Log.d(TAG, "onClick: attempting to authenticate");

            // Check if passwords match
            if (doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                // Initiate registration task
                registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.PasswordsDoNotMatch), Toast.LENGTH_LONG).show();
            }
            showDialog();
        }
    }

    private void registerNewEmail(final String email, String password) {
        showDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.UserCreated), Toast.LENGTH_SHORT).show();

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // Create a new user with email and specific user id
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    db.collection("users")
                            .document(uid)
                            .set(user);
                    redirectLoginScreen();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.AuthenticationFailed), Toast.LENGTH_SHORT).show();
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, getResources().getString(R.string.AuthenticationFailed), Snackbar.LENGTH_SHORT).show();
                    hideDialog();
                }
            }
        });
    }

}
