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

import java.util.ArrayList;



// todo best practices...

//

// latest ui design implementation....

// get it in the app store.

public class MainActivity extends AppCompatActivity {

    private UserData userData;
    //private UserData userData2;
    private TranData tranData;
    private HostComm hostComm;
    private Button _accountBtn;
    private Button _loginBtn;
    private Button _updateBtn;
    private Context context;
    private boolean needLogin;

    private static final int LOGIN_ACTIVITY = 3;
    private static final int BALANCE_ACTIVITY = 2;
    private static final int TRANSACTION_ACTIVITY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        hostComm = new HostComm(this);
        context = getApplicationContext();

        getMySharedPreferences();

        if (userData == null || !(userData.getLoggedIn())) {
            needLogin = true;
        }
        else {
            userData.setLoggedIn(false);
        }
        _accountBtn = (Button) findViewById(R.id.btn_view_account);
        _accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection(getApplicationContext())) {
                    startTest();
                }
             }
        });
        _loginBtn = (Button) findViewById(R.id.btn_login_main);
        _loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logindata();
              }

        });
        _updateBtn = (Button) findViewById(R.id.btn_update_account);
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
        TinyDB tinydb = new TinyDB(getApplicationContext());

        // different data is shuffled back and forth depending on activity
        if(data != null) {
            switch(requestCode) {
                case LOGIN_ACTIVITY:
                    //userData = (UserData) data.getSerializableExtra(getString(R.string.userFile));
                    userData = (UserData) tinydb.getObject("UserData", UserData.class);
                    needLogin = false;
                    putMySharedPreferences();

                    break;
                case BALANCE_ACTIVITY:
                case TRANSACTION_ACTIVITY:
                    //userData = (UserData) data.getSerializableExtra(getString(R.string.userFile));
                    userData = (UserData) tinydb.getObject("UserData",UserData.class);
                    needLogin = false;
                    // it's subtracting twice when it comes back from transaction.

                    getTinyDBTrans(requestCode == BALANCE_ACTIVITY? true:false);
                    putMySharedPreferences();

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

         editor.commit();

    }
    private void getTinyDBTrans(boolean adjust) {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        ArrayList<Object> objArray = tinydb.getListObject("TranDataData", TranDataData.class);
        ArrayList<TranDataData> tranArray = new ArrayList<>();
        for(Object obj: objArray) {
            TranDataData in = (TranDataData) obj;
            tranArray.add(in);
        }
        tranData.remakeStoredTrans(tranArray);

        float amt = tinydb.getFloat("TranDataDataAmt");
        // last amount should be a negative....
        float update = adjust ? tranData.getLastAmtFloat():0;
        tranData.setLastAmt(amt + update);
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
            getTinyDBTrans(false);
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

    private void startTest() {

        if(userData == null)
            userData = new UserData();
        if(tranData == null)
            tranData = new TranData();
        Intent intent = new Intent(this, GetAccountTotal.class);
        TinyDB tinydb = new TinyDB(getApplicationContext());
        tinydb.putObject("UserData",userData);

         //intent.putExtra(getString(R.string.userFile), userData);
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
        alertDialog.show();
    }


    private void logindata(/*View view*/) {
        if(userData == null) {
            userData = new UserData();
        }
         Intent intent = new Intent(this, UpdateLoginDataActivity.class);
        TinyDB tinydb = new TinyDB(getApplicationContext());
        tinydb.putObject("UserData", userData);
        //intent.putExtra(getString(R.string.userFile), userData);
        startActivityForResult(intent, LOGIN_ACTIVITY);
    }

    private void update(/*View view*/) {
        if(userData == null) {
            userData = new UserData();
        }
         Intent intent = new Intent(this, InputTransaction.class);
        //intent.putExtra(getString(R.string.userFile), userData);
        TinyDB tinydb = new TinyDB(getApplicationContext());
        tinydb.putObject("UserData",userData);
         startActivityForResult(intent, TRANSACTION_ACTIVITY);
    }
}
