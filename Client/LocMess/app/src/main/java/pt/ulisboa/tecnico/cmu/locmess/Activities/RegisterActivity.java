package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import pt.ulisboa.tecnico.cmu.locmess.R;

public class RegisterActivity extends AppCompatActivity {

    String SERVER_IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.setTitle("LocMess - Register");

        SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bRegister = (Button) findViewById(R.id.bRegister);

        //Display back button on top
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                register(username,password,v);
            }
        });

    }

    public void register(final String username, String password, View v){

        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/signup";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("username",username);
            jsonBody.put("password",password);
        }catch (Exception e){

        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    try{
                        if(response.get("status").toString().equals("ok")){
                            SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("token",response.getString("token"));
                            editor.putString("username",username);
                            editor.apply();
                            //Toast.makeText(RegisterActivity.this, "Status: "+ response.toString(), Toast.LENGTH_LONG).show();
                            Intent loginIntent = new Intent(RegisterActivity.this, UserAreaActivity.class);
                            loginIntent.putExtra("serverIP", SERVER_IP);
                            startActivity(loginIntent);
                        }
                        else{
                            try{
                                Toast.makeText(RegisterActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
                            Toast.makeText(RegisterActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        queue.add(jsObjRequest);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
