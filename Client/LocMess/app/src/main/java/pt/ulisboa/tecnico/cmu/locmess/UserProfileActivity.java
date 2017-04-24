package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserProfileActivity extends AppCompatActivity {

    HashMap<String, Set<String>> keyPairs = new HashMap<String, Set<String>>();
    List<String> pairs = new ArrayList<String>();
    ListView lvKeyPairs;
    Map<String,Boolean> checkedStatus = new LinkedHashMap<String,Boolean>();
    ArrayAdapter<String> adapterAutoComplete;
    int bColor = Color.TRANSPARENT;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("User Profile");


        Set<String> setClubs = new HashSet<String>(Arrays.asList("Benfica", "Real Madrid"));
        keyPairs.put("Club", setClubs);
        Set<String> setColors = new HashSet<String>(Arrays.asList("Red"));
        keyPairs.put("Color", setColors);
        Set<String> setPet = new HashSet<String>(Arrays.asList("Dog"));
        keyPairs.put("Pet", setPet);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final TextView tvUsername = (TextView) findViewById(R.id.tvUsername);
        final Button bDeleteKeyPairs = (Button) findViewById(R.id.bDeleteKeyPairs);
        final Button bAddKeyPair = (Button) findViewById(R.id.bAddKeyPair);
        lvKeyPairs = (ListView) findViewById(R.id.lvKeyPairs);
        lvKeyPairs.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        tvUsername.setText("Tiago");

        Drawable background = lvKeyPairs.getBackground();
        if (background instanceof ColorDrawable)
            bColor = ((ColorDrawable) background).getColor();

        for (Map.Entry<String, Set<String>> entry : keyPairs.entrySet()) {
            for (String str : entry.getValue()) {
                String pair = entry.getKey() + " = " + str;
                pairs.add(pair);
                checkedStatus.put(pair,false);
            }
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, pairs);

        lvKeyPairs.setAdapter(adapter);

        lvKeyPairs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String pair = (String) parent.getItemAtPosition(position);

                if (checkedStatus.get(pair).equals(true)) {
                    parent.getChildAt(position).setBackgroundColor(bColor);
                    checkedStatus.put(pair, false);
                } else {
                    parent.getChildAt(position).setBackgroundColor(Color.RED);
                    checkedStatus.put(pair, true);
                }
            }
        });

        bAddKeyPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog keyPairsDialog = new Dialog(UserProfileActivity.this);
                keyPairsDialog.setContentView(R.layout.add_key_pair_layout);
                keyPairsDialog.show();

                final AutoCompleteTextView acKey = (AutoCompleteTextView) keyPairsDialog.findViewById(R.id.acKey);
                final EditText etPair = (EditText) keyPairsDialog.findViewById(R.id.etPair);
                final Button bAddPair = (Button) keyPairsDialog.findViewById(R.id.bAddPair);

                ArrayList<String> keys = new ArrayList<String>();
                keys.add("Club");
                keys.add("Favourite Colour");
                keys.add("Relationship Status");

                adapterAutoComplete = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_dropdown_item_1line, keys);
                acKey.setAdapter(adapterAutoComplete);

                acKey.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        acKey.showDropDown();
                        return false;
                    }
                });

                bAddPair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = acKey.getText().toString();
                        String value = etPair.getText().toString();
                        String pair = key + " = " + value;
                        addKey(key,value);
                        pairs.add(pair);
                        checkedStatus.put(pair,false);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(UserProfileActivity.this, "Added Key Pair: " + pair, Toast.LENGTH_LONG).show();
                        keyPairsDialog.cancel();
                    }
                });
            }
        });


        bDeleteKeyPairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedItems();
            }
        });
    }

    public void deleteSelectedItems() {
        int i = 0;
        int e = 0;
        for (LinkedHashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
            if(entry.getValue().equals(true)){
                lvKeyPairs.getChildAt(i).setBackgroundColor(bColor);
                pairs.remove(i-e);
                e++;
            }
            i++;
        }
        checkedStatus.values().removeAll(Collections.singleton(true));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addKey(String key, String value){
        if(keyPairs.containsKey(key)){
            keyPairs.get(key).add(value);
        }
        else{
            Set<String> val = new HashSet<String>();
            val.add(value);
            keyPairs.put(key,val);
        }
    }
}
