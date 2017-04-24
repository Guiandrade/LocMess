package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;

import static android.R.attr.data;

public class UserAreaActivity extends AppCompatActivity {

    String SERVER_IP;
    String username;
    int MAIN_ACTIVITY_REQUEST_CODE = 1;
    int POST_MESSAGE_REQUEST_CODE = 2;
    int USER_PROFILE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
        username = (String) getIntent().getSerializableExtra("username");

        final ImageButton ibGridMenu = (ImageButton) findViewById(R.id.ibGridMenu);
        final Button btPostMessage = (Button) findViewById(R.id.btPostMessage);
        final ImageButton ibUserProfile = (ImageButton) findViewById(R.id.ibUserProfile);

        ibGridMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(UserAreaActivity.this, MainMenuActivity.class);
                mainMenuIntent.putExtra("username", username);
                mainMenuIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(mainMenuIntent,MAIN_ACTIVITY_REQUEST_CODE);
            }
        });

        btPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postMessageIntent = new Intent(UserAreaActivity.this, PostMessageActivity.class);
                postMessageIntent.putExtra("username", username);
                postMessageIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
            }
        });

        ibUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userProfileIntent = new Intent(UserAreaActivity.this, UserProfileActivity.class);
                userProfileIntent.putExtra("username", username);
                userProfileIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(userProfileIntent,USER_PROFILE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                username = (String) getIntent().getSerializableExtra("username");
            }
        }
        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                username = (String) getIntent().getSerializableExtra("username");
            }
        }
        else if (requestCode == USER_PROFILE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                username = (String) getIntent().getSerializableExtra("username");
            }
        }
    }

    @Override
    public void onBackPressed() {
    }
}
