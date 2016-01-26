package com.example.carolsusieo.anniebank;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private UserData userData;
    private TranData tranData;
    private HostComm hostComm;
    private Button _accountBtn;
    private Button _loginBtn;
    private Button _updateBtn;
    private Context context;
    private boolean needLogin;
    //Boolean isConnectionExist = false;
    //Boolean needAmount;
    //Resources resources;


    private static final int LOGIN_ACTIVITY = 3;
    private static final int BALANCE_ACTIVITY = 2;
    private static final int TRANSACTION_ACTIVITY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //resources = getResources();
        hostComm = new HostComm(this);
        context = getApplicationContext();
        //needAmount = true;

        getMySharedPreferences();

        // I think this is happening over and again, because we've set it previously
        if (userData == null || !(userData.getLoggedIn())) {
            //the app is being launched for first time, do something
            needLogin = true;
        }
        else {
            // we haven't actually logged in this session.... just verified that a login has been done in the past
            userData.setLoggedIn(false);
        }

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Get The Info", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startTest();

            }

        });
*/
        _accountBtn = (Button) findViewById(R.id.btn_view_account);
        _loginBtn = (Button) findViewById(R.id.btn_login_main);
        _updateBtn = (Button) findViewById(R.id.btn_update_account);

        _accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection(getApplicationContext())) {
                    startTest();
                }
             }
        });
        _loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logindata();
              }

        });
        _updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // different data is shuffled back and forth depending on activity
        if(data != null) {
            switch(requestCode) {
                case LOGIN_ACTIVITY:
                    // check if the request code is same as what is passed  here it is 2
                    userData = (UserData) data.getSerializableExtra(getString(R.string.userFile));
                    // if we don't reset, we will get in a loop until we do
                    needLogin = false;
                    putMySharedPreferences();

                    break;
                case BALANCE_ACTIVITY:
                case TRANSACTION_ACTIVITY:
                    userData = (UserData) data.getSerializableExtra(getString(R.string.userFile));
                    needLogin = false;
                    getTinyDBTrans();
                    //tranData = (TranData) data.getSerializableExtra(getString(R.string.tranFile));
                    //needAmount = false;
                    putMySharedPreferences();

                    // ?  not sure about all this message stuff, now that I have the serializable tran data.
                         // it's possible the communications never happened....
                    String a = String.valueOf(new StringBuilder(getString(R.string.title_activity_rest)).append(getString(R.string.colon)).append(tranData.getLastAmt()));
                    TextView thisText = (TextView) findViewById(R.id.txt_annie_bank);
                    thisText.setText(a);
                     break;
            }
        }

        else {
            TextView thisText = (TextView) findViewById(R.id.txt_annie_bank);
            thisText.setText(getString(R.string.error));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(needLogin)
            logindata(/*view*/);
      }

    @Override
    protected void onStop() {
        super.onStop();
        // set up the last know amount....
        SharedPreferences sharedPreferences = getMySharedPreferencesFile();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(tranData.getLastAmt() != null)
            editor.putString(getString(R.string.preflastamt), tranData.getLastAmt());
        editor.commit();
    }
    private String nameObject() {
        return getString(R.string.prefArrayList)  ;
    }

    private SharedPreferences getMySharedPreferencesFile() {
        String PREFS_NAME = getString(R.string.prefsFile);
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void putMySharedPreferences() {
        SharedPreferences sharedPreferences = getMySharedPreferencesFile();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.prefUsername), userData.getUsername());
        editor.putString(getString(R.string.prefPassword), userData.getPassword());
        if(userData.getLoggedIn())
            editor.putBoolean(getString(R.string.prefLoginVerified), userData.getLoggedIn());
        editor.putString(getString(R.string.prefWeb1), userData.getWebsite1());
        editor.putString(getString(R.string.prefWeb2), userData.getWebsite2());
        editor.putBoolean(getString(R.string.prefUse1), userData.useWeb1());
        editor.putString(getString(R.string.preflastamt), tranData.getLastAmt());

        // TinyDB handling transactions
        // need to store the stored transactions.
        //editor.putString(nameObject(), ObjectSerializer.serialize(tranData));
         editor.commit();

    }
    private void getTinyDBTrans() {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        ArrayList<Object> objArray = tinydb.getListObject("TranDataData", TranDataData.class);
        ArrayList<TranDataData> tranArray = new ArrayList<>();
        for(Object obj: objArray) {
            TranDataData in = (TranDataData) obj;
            tranArray.add(in);
        }
        tranData.remakeStoredTrans(tranArray);
    }

    private void getMySharedPreferences() {
        SharedPreferences sharedPreferences = getMySharedPreferencesFile();
        if(sharedPreferences.getString(getString(R.string.prefUsername),null) != null)
        {
            if(userData == null) {
                userData = new UserData();
            }
            userData.setUsername(sharedPreferences.getString(getString(R.string.prefUsername), getString(R.string.defUser)));
            userData.setPassword(sharedPreferences.getString(getString(R.string.prefPassword), getString(R.string.defPass)),
                    sharedPreferences.getString(getString(R.string.prefPassword), getString(R.string.defPass)));
            userData.setLoggedIn(sharedPreferences.getBoolean(getString(R.string.prefLoginVerified), true));
            userData.setWebsite1(sharedPreferences.getString(getString(R.string.prefWeb1), getString(R.string.defURL)));
            userData.setWebsite2(sharedPreferences.getString(getString(R.string.prefWeb2), getString(R.string.defURL)));
            userData.setUseWeb1(sharedPreferences.getBoolean(getString(R.string.prefUse1), true));

            if(tranData == null) {
                tranData = new TranData();
            }
            getTinyDBTrans();
//            tranData = (TranData)ObjectSerializer.deserialize(sharedPreferences.getString(nameObject(),
//                    ObjectSerializer.serialize(new TranData())));
            String a = getString(R.string.title_activity_rest);
            if(tranData != null)
                a = a + getString(R.string.maybe) + tranData.getLastAmt();
            TextView thisText = (TextView) findViewById(R.id.txt_annie_bank);
            thisText.setText(a);
        }
        else {
            if(userData == null) {
                userData = new UserData();
            }
            userData.setUsername(getString(R.string.defUser));
            userData.setPassword(getString(R.string.defPass), getString(R.string.defPass));
            userData.setLoggedIn(false);
            userData.setWebsite1(getString(R.string.defURL));
            userData.setWebsite2(getString(R.string.defURL));
            userData.setUseWeb1(true);

            if(tranData == null) {
                tranData = new TranData();
            }
            tranData.setLastAmt( "unknown");

        }
    }


 /*   public void startTest(Context context) {
        startTest();
    }
*/
 private void startTest() {
        // for now, don't get the account total

        if(userData == null)
            userData = new UserData();
        if(tranData == null)
            tranData = new TranData();
        Intent intent = new Intent(this, GetAccountTotal.class);
        intent.putExtra(getString(R.string.userFile), userData);
      //  intent.putExtra(getString(R.string.tranFile),tranData);
        startActivityForResult(intent, BALANCE_ACTIVITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }


  // check internet connection
  private boolean checkConnection(Context incontext)
    {
        TextView wifiText = (TextView) findViewById(R.id.txt_wifi_available);
        if(!hostComm.checkConnection(incontext)){
             // Internet connection doesn't exist
             showAlertDialog(this,getString(R.string.noInternet),
                    getString(R.string.noInternet2), false);
            wifiText.setText(getString(R.string.noInternet3));
            return false;
        }
        else {
            // update the content wifi text item to indicate WIFI tested ok.
            wifiText.setText(getString(R.string.wifiGood));
            return true;
        }

    }


    // do we have internet connectivity?
    private void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.agt_action_success : R.drawable.agt_action_fail);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE ,getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
         // it blows here.
        alertDialog.show();
    }


    private void logindata(/*View view*/) {
        // start activity that allows user to input user name, and password (twice with verification)
        if(userData == null) {
            userData = new UserData();
        }
         Intent intent = new Intent(this, UpdateLoginDataActivity.class);
        intent.putExtra(getString(R.string.userFile), userData);
        startActivityForResult(intent, LOGIN_ACTIVITY);
    }

    private void update(/*View view*/) {
        // start activity that allows user to input user name, and password (twice with verification)
        if(userData == null) {
            userData = new UserData();
        }
         Intent intent = new Intent(this, InputTransaction.class);
        intent.putExtra(getString(R.string.userFile), userData);
        // when this happens, what happens to arrayList?
   //     intent.putExtra(getString(R.string.tranFile),tranData);
        startActivityForResult(intent, TRANSACTION_ACTIVITY);
    }
/*
    public void viewaccount()
    {
        if(checkConnection(context))
            startTest();
    }
*/
}
