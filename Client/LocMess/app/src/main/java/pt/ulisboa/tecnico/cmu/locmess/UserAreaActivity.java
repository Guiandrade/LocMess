package pt.ulisboa.tecnico.cmu.locmess;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class UserAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        final ImageButton ibGridMenu = (ImageButton) findViewById(R.id.ibGridMenu);

        ibGridMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(UserAreaActivity.this, MainMenuActivity.class);
                UserAreaActivity.this.startActivity(mainMenuIntent);
            }
        });
    }
}
