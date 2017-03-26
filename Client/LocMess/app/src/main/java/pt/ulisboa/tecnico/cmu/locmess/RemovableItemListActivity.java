package pt.ulisboa.tecnico.cmu.locmess;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RemovableItemListActivity extends AppCompatActivity {
    ArrayList<String> names = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView listView;
    //private SparseBooleanArray mSelectedItemsIds;
    ArrayList<String> selectedItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removable_item_list);

        names.add("Arco do Cego");
        names.add("Estadio da Luz");
        names.add("Instituto Superior Tecnico");
        
        listView = (ListView) findViewById(R.id.lvRemovableItemList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        adapter = new ArrayAdapter<String>(this, R.layout.delete_row_layout,R.id.ctvDeleteItem, names);
        listView.setAdapter(adapter);
        final Button bDeleteItems = (Button) findViewById(R.id.bDeleteItems);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("CHECKED", "Checked " + ((CheckedTextView) view).isChecked());
                if(((CheckedTextView) view).isChecked()){
                    ((CheckedTextView) view).setChecked(false);
                }else{
                    ((CheckedTextView) view).setChecked(true);
                }
                String selectedItem = ((TextView) view).getText().toString();
                if (selectedItems.contains(selectedItem))
                    selectedItems.remove(selectedItem); //remove deselected item from the list of selected items
                else
                    selectedItems.add(selectedItem); //add selected item to the list of selected items

            }
        });

        bDeleteItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectedItems(v);
                deleteSelectedItems(v);
            }
        });
    }

    public void deleteSelectedItems(View view) {
        ArrayList<String> itemsToDelete = new ArrayList<String>();
        for (String item : selectedItems) {
            names.remove(item);
        }
        selectedItems.clear();
        adapter.notifyDataSetChanged();
    }

    public void showSelectedItems(View view) {
        String selItems = "";
        for (String item : selectedItems) {
            if (selItems == "")
                selItems = item;
            else
                selItems += "/" + item;
        }
        Toast.makeText(this, selItems, Toast.LENGTH_LONG).show();
    }
}