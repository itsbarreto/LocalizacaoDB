package priceranking.app.italobarreto.localizacaodb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import priceranking.app.italobarreto.localizacaodb.R;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "Login";
    private static final int LOGIN_FACEBOOK = 0;
    private static final int LOGIN_GOOGLE = 1;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private ProgressBar barraDeProgresso;
    private LoginButton btoLoginFB;
    private Button btoEntdFB;
    private Button btoEntdGoogle;
    private int tipoDeLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        barraDeProgresso = (ProgressBar) findViewById(R.id.login_progress);
        barraDeProgresso.setVisibility(View.GONE);
        configuraLoginGoogle();
        configuraLoginFB();




    }


    private void configuraLoginFB(){

        callbackManager = CallbackManager.Factory.create();
        btoLoginFB = (LoginButton) findViewById(R.id.id_bto_login_fb);
        btoLoginFB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tipoDeLogin = LOGIN_FACEBOOK;
                barraDeProgresso.setVisibility(View.VISIBLE);
            }
        });
        btoLoginFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                        barraDeProgresso.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Login com o Facebook cancelado.",
                                Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        barraDeProgresso.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Falha no login com o Facebook.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
        btoEntdFB = (Button) findViewById(R.id.id_bto_entd_fb);
        btoEntdFB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btoLoginFB.performClick();
            }
        });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            direcionaParaMainActivity(user.getDisplayName());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Falha no login com o Facebook.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private void configuraLoginGoogle(){
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, null /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        btoEntdGoogle = (Button) findViewById(R.id.id_bto_entd_google);
        btoEntdGoogle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                barraDeProgresso.setVisibility(View.VISIBLE);
                tipoDeLogin = LOGIN_GOOGLE;
                signIn();

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }


    }
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("googleSigin", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            direcionaParaMainActivity(acct.getDisplayName());
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(getApplicationContext(),"Usuario nao reconhecido",Toast.LENGTH_LONG);
        }
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }





    private void direcionaParaMainActivity(String nmUsuario){
        Toast.makeText(getApplicationContext(),"Login efetiado com sucesso",Toast.LENGTH_LONG);
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        myIntent.putExtra("usuario", nmUsuario);
        LoginActivity.this.startActivity(myIntent);
    }

}

