package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.Location;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class ReadMessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_messages);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("Read Messages");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView readMessagesListView = (ListView) findViewById(R.id.lvReadMessages);

        SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
        Set<String> messagesSet = prefs.getStringSet("messages", null);
        ArrayList<Message> messages = new ArrayList<Message>();

        for(String message : messagesSet){
            try{
                JSONObject arr = new JSONObject(message);
                Location location = new Location(arr.get("location").toString(),(Coordinates) null);
                int initHour = Integer.parseInt(arr.get("initTime").toString().split(":")[0]);
                int initMinute = Integer.parseInt(arr.get("initTime").toString().split(":")[1].split("-")[0]);
                int initDay = Integer.parseInt(arr.get("initTime").toString().split("/")[0].split("-")[1]);
                int initMonth = Integer.parseInt(arr.get("initTime").toString().split("/")[1]);
                int initYear = Integer.parseInt(arr.get("initTime").toString().split("/")[2]);
                int endHour = Integer.parseInt(arr.get("endTime").toString().split(":")[0]);
                int endMinute = Integer.parseInt(arr.get("endTime").toString().split(":")[1].split("-")[0]);
                int endDay = Integer.parseInt(arr.get("endTime").toString().split("/")[0].split("-")[1]);
                int endMonth = Integer.parseInt(arr.get("endTime").toString().split("/")[1]);
                int endYear = Integer.parseInt(arr.get("endTime").toString().split("/")[2]);

                TimeWindow timeWindow = new TimeWindow(initHour,initMinute,initDay,initMonth,
                        initYear,endHour,endMinute,endDay,endMonth,endYear);

                String id = arr.get("id").toString();
                String msg = arr.get("body").toString();
                String owner = arr.get("username").toString();
                String title = arr.get("title").toString();

                Message mssg = new Message(id,title,msg,owner,location,null,null,timeWindow);
                messages.add(mssg);
            }
            catch (Exception e){

            }
        }

        List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1,R.id.text2});

        for(Message msg : messages){
            HashMap<String, String> resultsMap = new HashMap<>();
            resultsMap.put("First Line", msg.getTitle());
            String coordinates = "Loc: " + msg.getLocation().getName() + ", Eding Date/Time: " +
                    msg.getTimeWindow().getEndingDay() + "/" +
                    msg.getTimeWindow().getEndingMonth() + "/" +
                    msg.getTimeWindow().getEndingYear() + " " +
                    msg.getTimeWindow().getEndingHour() + ":" +
                    msg.getTimeWindow().getEndingMinutes() + ", Id: " + msg.getId();
            resultsMap.put("Second Line", coordinates);
            listItems.add(resultsMap);
        }
        readMessagesListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
