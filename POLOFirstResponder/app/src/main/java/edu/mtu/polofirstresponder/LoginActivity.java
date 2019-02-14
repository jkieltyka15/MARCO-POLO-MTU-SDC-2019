package edu.mtu.polofirstresponder;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;         //used for authenticating firebase users
    EditText emailText, passwordText;   //used for authentication
    Button loginButton;                 //button used to initialize an authentication

    private String TAG = "Login Activity"; //used for logging purposes

    /**
     * Initialize this activity.
     * @param savedInstanceState = black boxed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("POLO First Responder Login");

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance(); //initialize the firebase authenticator

        /**
         * When the login is clicked, attempt an authentication.
         */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(emailText.getText().toString(), passwordText.getText().toString());
            }
        });
    }

    /**
     * Check to see if the Firebase user is logged in, and update the UI accordingly.
     */
    @Override
    public void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), NavigationActivity.class));
        }
    }

    /**
     * Authenticate the user with the given credentials.
     */
    private void signIn(String email, String password){

        //Attempt to authenticate using the Firebase Authenticator
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //Authentication Successful
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getApplicationContext(), NavigationActivity.class));
                            finish();
                        }

                        //Authentication Failed
                        else {
                            // TO-DO: tell them it failed
                        }
                    }
                });
    }


}