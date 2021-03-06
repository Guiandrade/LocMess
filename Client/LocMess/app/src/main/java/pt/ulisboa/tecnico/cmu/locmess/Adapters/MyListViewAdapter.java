package pt.ulisboa.tecnico.cmu.locmess.Adapters;

import  java.util.List;

import  android.content.Context;
import  android.util.SparseBooleanArray;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.ArrayAdapter;
import  android.widget.ImageView;
import  android.widget.TextView;

import pt.ulisboa.tecnico.cmu.locmess.R;

public class  MyListViewAdapter extends ArrayAdapter<String> {
    Context myContext;
    LayoutInflater inflater;
    List<String> DataList;
    private  SparseBooleanArray mSelectedItemsIds;

    // Constructor for get Context and  list
    public  MyListViewAdapter(Context context, int resourceId,  List<String> lists) {
        super(context,  resourceId, lists);
        mSelectedItemsIds = new  SparseBooleanArray();
        myContext = context;
        DataList = lists;
        inflater =  LayoutInflater.from(context);
    }

    // Container Class for item
    private class ViewHolder {
        TextView text1;
        TextView text2;
    }

    public View getView(int position,  View view, ViewGroup parent) {
        final ViewHolder  holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_item, null);
            // Locate the TextViews in  listview_item.xml
            holder.text1 = (TextView) view.findViewById(R.id.text1);
            holder.text2 = (TextView) view.findViewById(R.id.text2);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)  view.getTag();
        }
        // Capture position and set to the  TextViews
        holder.text1.setText(DataList.get(position).toString().split(" = ")[0]);
        holder.text2.setText(DataList.get(position).toString().split(" = ")[1]);
        return view;
    }

    @Override
    public void remove(String  object) {
        DataList.remove(object);
        notifyDataSetChanged();
    }

    // get List after update or delete
    public  List<String> getMyList() {
        return DataList;
    }

    public void  toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position,  value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public  SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}