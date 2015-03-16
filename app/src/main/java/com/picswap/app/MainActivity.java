package com.picswap.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.picswap.app.helper.SQLLiteHandler;
import com.picswap.app.helper.SessionManager;

import java.util.HashMap;

/**
 * Created by mark on 13/03/2015.
 */
public class MainActivity extends Activity {
    private TextView nameText;
    private TextView emailText;
    private TextView scoreText;

    private Button btnLogout;

    private SQLLiteHandler database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);

        nameText = (TextView) findViewById(R.id.username);
        emailText = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        // SQLite Database Handler
        database = new SQLLiteHandler(getApplicationContext());

        // Session Manager
        sessionManager = new SessionManager(getApplicationContext());

        if(!sessionManager.isLoggedIn()){
            logoutUser();
        }

        //Fetching User Details from SQLite
        HashMap<String, String> user = database.getUserDetails();

        String name = user.get("username");
        String email = user.get("email_address");
        String score = user.get("score");
        // TODO: show other user information here (Possibly unread messages notification etc.)

        // Displaying the user details on the screen
        nameText.setText(name);
        emailText.setText(email);
        scoreText.setText("Current Points: " + score);
        if(scoreText.equals("0")){
            scoreText.setText("Current points: 0" + "\n" + "Send some pictures to get more!");
        }

        // Logout Button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
    }

    /**
     * Logging out. Sets isLoggedIn to false in shared preferences &
     * Also clears the user data from teh sqllite users table
     */
    private void logoutUser(){
        sessionManager.setLogin(false);

        database.deleteUsers();

        //Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
