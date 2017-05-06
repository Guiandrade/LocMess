package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("Message");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Message message = (Message) getIntent().getSerializableExtra("Message");

        TextView tvMessageFrom = (TextView) findViewById(R.id.tvMessageFrom2);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle2);
        TextView tvMessage = (TextView) findViewById(R.id.tvMessage2);
        TextView tvLocation = (TextView) findViewById(R.id.tvLocation2);
        TextView tvPostDate = (TextView) findViewById(R.id.tvPostDate2);

        tvMessageFrom.setText(message.getOwner());
        tvTitle.setText(message.getTitle());
        tvMessage.setText(message.getMessage());
        if(!(message.getLocation().getSSID()==null)){
            tvLocation.setText(message.getLocation().getSSID());
        }
        else{
            tvLocation.setText(message.getLocation().getName());
        }
        tvPostDate.setText(message.getTimeWindow().getStartingDay() + "/" +
                message.getTimeWindow().getStartingMonth() + "/" +
                message.getTimeWindow().getStartingYear() + " " +
                message.getTimeWindow().getStartingHour() + ":" +
                message.getTimeWindow().getStartingMinute());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }
}
