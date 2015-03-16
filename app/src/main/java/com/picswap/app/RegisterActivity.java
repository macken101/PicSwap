package com.picswap.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.picswap.app.helper.SQLLiteHandler;
import com.picswap.app.helper.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mark on 13/03/2015.
 */
public class RegisterActivity extends Activity{
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private Button btnRegister;
    private Button btnToLogin;

    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private SQLLiteHandler database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        inputUsername = (EditText) findViewById(R.id.username);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen); // TODO: CHeck these two lines btnRegister
        btnToLogin = (Button) findViewById(R.id.btnLogin); //       btnLinkToRegisterScreen

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Session Manager
        sessionManager = new SessionManager(getApplicationContext());

        // SQLite Database Handler
        database = new SQLLiteHandler(getApplicationContext());

        // Check if user is already logged into the application
        if(sessionManager.isLoggedIn()) {
            // If the User is logged in - redirect to main Activity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Click Listener for the registration button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputUsername.getText().toString();
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    registerUser(name, email, password);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter all your required user details.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Link to Login Screen
        btnToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     *  Function used to store new user into MySQL Database.
     *  Posts parameters - tag, name, email, password) to registration URL
     */
    private void registerUser(final String name, final String email, final String password) {
        String tag_str_request = "req_register";

        progressDialog.setMessage("Registering User Information...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.REGISTRATION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Registration Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        // User is Successfully stored in the Database
                        // Now user is to be stored in SQLite on the device
                        String user_id = jsonObject.getString("user_id");

                        JSONObject user = jsonObject.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String date_created = user.getString("date_created");

                        // Insert into Users Table
                        database.addUserToDB(name, email, user_id, date_created);

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        //Error occured in the registration process. Retrieve error message.
                        String errorMessage = jsonObject.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Registration Error: " + volleyError.getMessage());

                Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to registration URL.
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("tag", "register");
                parameters.put("name", name);
                parameters.put("email", email);
                parameters.put("password", password);

                return parameters;
            }
        };

        // Add request to RequestQueue.
        AppController.getInstance().addToRequestQueue(stringRequest, tag_str_request);
    }

    // To display a progress dialog to the user
    private void showDialog() {
        if(!progressDialog.isShowing())
            progressDialog.show();
    }

    // Used to hide the progress diaglog from the view
    private void hideDialog(){
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }
}