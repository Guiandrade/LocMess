package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_location);

        final EditText etLocationName = (EditText) findViewById(R.id.etLocationName);
        final EditText etLatitude = (EditText) findViewById(R.id.etLatitude);
        final EditText etLongitude = (EditText) findViewById(R.id.etLongitude);
        final EditText etRadius = (EditText) findViewById(R.id.etRadius);
        final Button bAddLocation = (Button) findViewById(R.id.bAddLocation);

        bAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Coordinates coordinates = new Coordinates(etLatitude.getText().toString(),
                        etLongitude.getText().toString(),
                        etRadius.getText().toString());
                Location location = new Location(etLocationName.getText().toString(),coordinates);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("locationCreated",location);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }
}
