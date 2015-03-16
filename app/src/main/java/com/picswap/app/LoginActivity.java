package com.picswap.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.picswap.app.helper.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    //LogCat tag
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress Dialog for user
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Session Manager
        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.isLoggedIn()) {
            // User is already Logged in. Redirect to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Link To Register Screen.
    public void redirectToRegistration(View view){
        Intent intent =  new Intent(getApplicationContext(),
                RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    // Login to the System method
    public void LoginToSystem(View view){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        // Check for empty Input Data.
        if(email.trim().length() > 0 && password.trim().length() > 0){
            // login User
            checkLogin(email, password);
        }else {
            // Prompt user to put in details if empty with a toast
            Toast.makeText(getApplicationContext(), "Please Enter All Valid User Information.", Toast.LENGTH_LONG).show();
        }
    }

    // Function to verify login details in mySQL database
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel request
        String tag_string_request = "req_login";

        progressDialog.setMessage("Logging into PicSwap...");
        showDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.REGISTRATION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                progressDialog.hide();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    // Check for error node in JSON
                    if(!error){
                        // User successfully logged in
                        // Create login session
                        sessionManager.setLogin(true);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMessage = jsonObject.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException exception) {
                    // Json Error
                    exception.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                // Posting params to register URL
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_request);
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
}