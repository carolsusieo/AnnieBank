package com.example.carolsusieo.anniebank;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ViewStoredTransactions extends AppCompatActivity {

    static ArrayList<String> list;
    static ArrayList<Object>objArray;
    int recDeleted;
    private static final int MAX_DELETES = 20;
    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stored_transactions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recDeleted = 0;

        objArray = getTransactionData();
        list = new ArrayList<String>();
        for(Object obj: objArray)
        {
            TranDataData in = (TranDataData) obj;
            list.add(in.desc + " " + in.amount + " " +  in.which);
        }


        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        ListView listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                if (recDeleted < MAX_DELETES) {
                    final String item = (String) parent.getItemAtPosition(position);
                    // make sure the item is still there...
                    Show_Alert_box(view.getContext(), "Please select action.", item, list, adapter);
                    // view.setAlpha(1);
                } else
                    Show_Alert_box(view.getContext(),"Delete not allowed",null,null,null);

            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private ArrayList<Object> getTransactionData() {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        return(tinydb.getListObject("TranDataData", TranDataData.class));
        /*
        TranData data = (TranData)tinydb.getObject("TranData",TranData.class);
        ArrayList<TranDataData> tranArray = data.getTranArrayData();
        ArrayList<Object> oArray = new ArrayList<>();

        for (TranDataData tran : tranArray) {
            Object in = (Object) tran;
            oArray.add(in);
        }
         return oArray;
        */
    }

    private void putTransactionData() {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        tinydb.putListObject("TranDataData", objArray);

        /*
        TranData data = (TranData)tinydb.getObject("TranData",TranData.class);

        ArrayList<TranDataData> oArray = new ArrayList<>();

        for (Object obj : objArray) {
            TranDataData in = (TranDataData) obj;
            oArray.add(in);
        }
        data.remakeStoredTrans(oArray);
        tinydb.putObject("TranData",data);
        */

    }

    public void onBackPressed()
    {
        int [] intarray = new int[MAX_DELETES];
        if(recDeleted > 0) {

            int j = 0;
            int k = 0;
            int numDeletes = 0;
            String test;
             for(Object obj: objArray)
            {
                if(j < list.size()) {
                    TranDataData in = (TranDataData) obj;
                    test = String.valueOf(new StringBuilder(in.desc + " " + in.amount + " " + in.which));
                    if (!test.contentEquals(list.get(j))) {
                        intarray[numDeletes++] = k;
                    } else
                        j++;
                    k++;
                }
                else {
                    intarray[numDeletes++] = k++;
                }
            }
            for(j = numDeletes-1; j >= 0; j--)
            {
                objArray.remove(intarray[j]);
            }
            putTransactionData();
        }
        finish();
    }
    public void Show_Alert_box(Context context, String message,final String item,final ArrayList<String> list,final StableArrayAdapter adapter)
    {

        final AlertDialog alertDialog = new  AlertDialog.Builder(context).create();
        if(item == null) {
            alertDialog.setTitle(message);
        }
        else {
            alertDialog.setTitle(getString(R.string.delete_alert_Dialog));
            alertDialog.setButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    list.remove(item);
                    recDeleted++;
                    adapter.notifyDataSetChanged();
                }
            });
        }
        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
        Context context;
        int textViewHld;
        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            textViewHld = textViewResourceId;
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
