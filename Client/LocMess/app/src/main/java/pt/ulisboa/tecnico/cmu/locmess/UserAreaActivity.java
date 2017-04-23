package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;

import static android.R.attr.data;

public class UserAreaActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
    ArrayList<Message> messages = new ArrayList<Message>();
    int MAIN_ACTIVITY_REQUEST_CODE = 1;
    int POST_MESSAGE_REQUEST_CODE = 2;
    int USER_PROFILE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);
        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");
        final ImageButton ibGridMenu = (ImageButton) findViewById(R.id.ibGridMenu);
        final Button btPostMessage = (Button) findViewById(R.id.btPostMessage);
        final ImageButton ibUserProfile = (ImageButton) findViewById(R.id.ibUserProfile);

        ibGridMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(UserAreaActivity.this, MainMenuActivity.class);
                mainMenuIntent.putExtra("locations", locations);
                startActivityForResult(mainMenuIntent,MAIN_ACTIVITY_REQUEST_CODE);
            }
        });

        btPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postMessageIntent = new Intent(UserAreaActivity.this, PostMessageActivity.class);
                postMessageIntent.putExtra("locations", locations);
                startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
            }
        });

        ibUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userProfileIntent = new Intent(UserAreaActivity.this, UserProfileActivity.class);
                userProfileIntent.putExtra("locations", locations);
                startActivityForResult(userProfileIntent,USER_PROFILE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Location> locationsUpdated = (ArrayList<Location>) data.getSerializableExtra("locationsUpdated");
                locations = locationsUpdated;
            }
        }
        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Message message = (Message) data.getSerializableExtra("messagePosted");
                messages.add(message);
            }
        }
    }
}
