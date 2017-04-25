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
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class LoginActivity extends AppCompatActivity {

    String SERVER_IP = "192.168.2.17:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("LocMess - Login");

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final TextView registerLink = (TextView) findViewById(R.id.tvRegister);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                registerIntent.putExtra("serverIP", SERVER_IP);
                startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                login(username,password,v);
            }
        });
    }

    public void login(final String username, String password, View v){
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);
        Ion.with(v.getContext())
        .load("http://" + SERVER_IP + "/login")
        .setJsonObjectBody(json)
        .asJsonObject()
        .setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if(result.get("status").toString().equals("\"ok\"")){
                    Intent loginIntent = new Intent(LoginActivity.this, UserAreaActivity.class);
                    //loginIntent.putExtra("locations", locations);
                    //loginIntent.putExtra("users", users);
                    loginIntent.putExtra("username", username);
                    loginIntent.putExtra("serverIP", SERVER_IP);
                    startActivity(loginIntent);
                }
                else{
                    Toast.makeText(LoginActivity.this, "Username or password incorrect", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
