package com.example.sociallogin;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String SHARED_PREFS = "sahredprefs";
    public static final String FbprofileUrl = "PfbprofileUrl", Fbname = "Pfb_name", Fbemail = "Pfb_email", Fbid = "Pfb_id";
    public static final String FB_LOGIN = "fb_login";
    public static final String GMAIL_LOGIN = "gmail_login";
    public static final int SignIn_value = 1;
    Button fb_loginButton, gmail_loginButton;
    CallbackManager callbackManager;
    String name, id, profilePicUrl, email;
    SharedPreferences sharedPreferences;
    GoogleApiClient googleApiClient;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences fb_settings = getSharedPreferences(FB_LOGIN, 0);
        SharedPreferences email_settings = getSharedPreferences(GMAIL_LOGIN, 0);
        if (fb_settings.getString("fb_logged", "").toString().equals("fb_logged")) {
            startActivity(new Intent(MainActivity.this, UserProfile.class));
            this.finish();
        } else if (email_settings.getString("gmail_logged", "").toString().equals("gmail_logged")) {
            startActivity(new Intent(MainActivity.this, gmail_userprofile.class));
            this.finish();
        }

        TextView text_links = findViewById(R.id.link_texts);
        String text = "By clicking on login, you are accepting our Privacy Policy \nand Terms & Conditions.";
        SpannableString ss = new SpannableString(text);
        ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(66, 103, 178));
        ss.setSpan(fcs, 44, 58, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(fcs, 64, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://social-media-integr.flycricket.io/privacy.html")));
            }
        };

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://social-media-integr.flycricket.io/terms.html")));
            }
        };

        ss.setSpan(clickableSpan, 44, 58, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan1, 64, 82, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text_links.setText(ss);
        text_links.setMovementMethod(LinkMovementMethod.getInstance());

        fb_loginButton = findViewById(R.id.fblogin_button);
        callbackManager = CallbackManager.Factory.create();

        fb_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email"));

                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        fb_loginButton.setVisibility(View.INVISIBLE);
                        gmail_loginButton.setVisibility(View.INVISIBLE);
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setTitle("Loading data...");
                        progressDialog.show();

                        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d("Demo", object.toString());

                                try {
                                    name = object.getString("name");
                                    id = object.getString("id");
                                    if (object.getJSONObject("picture").getJSONObject("data").getString("url") != null) {
                                        profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    } else {
                                        profilePicUrl = "null";
                                    }

                                    if (object.has("email")) {
                                        email = object.getString("email");
                                    } else {
                                        email = " ";
                                    }

                                    SharedPreferences settings = getSharedPreferences(FB_LOGIN, 0);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString("fb_logged", "fb_logged");
                                    editor.commit();
                                    sendfbData();
                                    progressDialog.dismiss();

                                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, UserProfile.class));
                                    finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        Bundle bundle = new Bundle();
                        bundle.putString("fields", "picture.type(large),gender, name, id, birthday, friends, email");
                        graphRequest.setParameters(bundle);
                        graphRequest.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        fb_loginButton.setVisibility(View.VISIBLE);
                        gmail_loginButton.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Login Cancelled.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        fb_loginButton.setVisibility(View.VISIBLE);
                        gmail_loginButton.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Login Error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        gmail_loginButton = findViewById(R.id.gmaillogin_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        gmail_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SignIn_value);
            }
        });
    }

    private void sendfbData() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Fbname, name);
        editor.putString(Fbemail, email);
        editor.putString(FbprofileUrl, profilePicUrl);
        editor.putString(Fbid, id);
        editor.apply();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SignIn_value) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSigninResult(task);
        }
    }

    private void handleSigninResult(Task<GoogleSignInAccount> result) {
        try {
            GoogleSignInAccount account = result.getResult(ApiException.class);
            fb_loginButton.setVisibility(View.INVISIBLE);
            gmail_loginButton.setVisibility(View.INVISIBLE);
            SharedPreferences settings = getSharedPreferences(GMAIL_LOGIN, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("gmail_logged", "gmail_logged");
            editor.commit();

            Toast.makeText(MainActivity.this, "Login Successful!.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, gmail_userprofile.class));
            finish();

        } catch (ApiException e) {
            fb_loginButton.setVisibility(View.VISIBLE);
            gmail_loginButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show();
            Log.v("Error", "signInResult:failed code = " + e.getStatusCode());
        }
    }
}