package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import static android.R.attr.data;

public class UserAreaActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
    int MAIN_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);
        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");
        final ImageButton ibGridMenu = (ImageButton) findViewById(R.id.ibGridMenu);

        ibGridMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(UserAreaActivity.this, MainMenuActivity.class);
                mainMenuIntent.putExtra("locations", locations);
                startActivityForResult(mainMenuIntent,MAIN_ACTIVITY_REQUEST_CODE);
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
    }
}
