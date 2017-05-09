package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("LocMess - Login");

        //Display back button on top
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        if(!(token.equals(""))){
            Intent loginIntent = new Intent(LoginActivity.this, UserAreaActivity.class);
            loginIntent.putExtra("serverIP", new Http().getServerIp());
            startActivity(loginIntent);
        }

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final TextView registerLink = (TextView) findViewById(R.id.tvRegister);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                login(username,password);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent loginActivityIntent = new Intent(LoginActivity.this, LoginActivity.class);
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginActivityIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void login(final String username, String password){

        JSONObject jsonBody = new JSONObject();

        try{
            jsonBody.put("username",username);
            jsonBody.put("password",password);
        }catch (Exception e){

        }

        new Http().session(jsonBody,this,"login");
    }
}
