package pt.ulisboa.tecnico.cmu.locmess;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.AbsListView.MultiChoiceModeListener;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
    ArrayList<User> users = new ArrayList<User>();

    public void populateLocations (){
        Coordinates coordinates1 = new Coordinates("123","456","10m");
        Location location1 = new Location("Arco do Cego", coordinates1);

        Coordinates coordinates2 = new Coordinates("789","987","20m");
        Location location2 = new Location("Taguspark", coordinates2);

        Coordinates coordinates3 = new Coordinates("654","321","30m");
        Location location3 = new Location("Estádio da Luz", coordinates3);

        locations.add(location1);
        locations.add(location2);
        locations.add(location3);
    }

    public void populateUsers (){
        HashMap<String,String> keyPairs1 = new HashMap<String, String>();
        keyPairs1.put("Club","Benfica");
        keyPairs1.put("Sport","Football");
        keyPairs1.put("Color","Red");
        User user1 = new User("jonas","222");
        user1.setKeyPairs(keyPairs1);

        HashMap<String,String> keyPairs2 = new HashMap<String, String>();
        keyPairs2.put("Club","Sporting");
        keyPairs2.put("Sport","Football");
        keyPairs2.put("Color","Red");
        User user2 = new User("dost","333");
        user2.setKeyPairs(keyPairs2);

        HashMap<String,String> keyPairs3 = new HashMap<String, String>();
        keyPairs3.put("Club","Porto");
        keyPairs3.put("Sport","Ballet");
        keyPairs3.put("Color","Red");
        User user3 = new User("andre","444");
        user3.setKeyPairs(keyPairs3);

        users.add(user1);
        users.add(user2);
        users.add(user3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        populateLocations();

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final TextView registerLink = (TextView) findViewById(R.id.tvRegister);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                registerIntent.putExtra("locations", locations);
                registerIntent.putExtra("users", users);
                startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(LoginActivity.this, UserAreaActivity.class);
                loginIntent.putExtra("locations", locations);
                loginIntent.putExtra("users", users);
                startActivity(loginIntent);
            }
        });
    }
}
