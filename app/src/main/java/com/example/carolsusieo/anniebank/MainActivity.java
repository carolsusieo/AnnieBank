package com.example.carolsusieo.anniebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    UserData userData;
    HostComm hostComm;
    Button _accountBtn;
    Button _loginBtn;
    Button _updateBtn;
    Context context;
    boolean needLogin;
    Boolean isConnectionExist = false;
    Boolean needAmount;
    String lastAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        hostComm = new HostComm(this);
        context = getApplicationContext();
        needAmount = true;
        // if first time ever, or never gotten a good hook up,
        // you need to set up the account stuff,
        // otherwise just show the account, and allow user to make a
        // deposit or withdrawal

        getMySharedPreferences();

        // I think this is happening over and again, because we've set it previously
        if (userData == null || !(userData.getLoggedIn())) {
            //the app is being launched for first time, do something
            Log.d("Comments", "Login_info");

            // get the info required to login to the host site

            //WAIT - note.... this didn't STOP to actually allow us to do anything... isn't acting like an activity...
            //View view =  findViewById(R.id.btn_login_main);
            // starting an activity from OnCreate seems to be a problem
            needLogin = true;


        }
        else
            // we haven't actually logged in this session.... just verfied that a login has been done in the past
            userData.setLoggedIn(false);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Get The Info", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startTest();

            }
        });

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

        if(data != null) {
            // check if the request code is same as what is passed  here it is 2
            userData = (UserData) data.getSerializableExtra("userdata");
            // if we don't reset, we will get in a loop until we do
            needLogin = false;
            putMySharedPreferences();

            if (requestCode == 2 || requestCode == 4) {
                String message = data.getStringExtra("MESSAGE");
                if (lastAmt == null)
                    lastAmt = new String();
                if(message != null) {
                    lastAmt = message;
                    // it's possible the communications never happened....
                    String a = String.valueOf(new StringBuilder("Annie\'s Bank Balance is ").append(message));
                    TextView thisText = (TextView) findViewById(R.id.txt_annie_bank);
                    thisText.setText(a);
                }
            }
        }

        else {
            TextView thisText = (TextView) findViewById(R.id.txt_annie_bank);
            thisText.setText("error");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(needLogin)
            logindata(/*view*/);
        //now see if we can test the recently entered login information....
        // if it hasn't already been tested
//        else if (needAmount && checkConnection(context)) {
//            isConnectionExist = true;
            // perform a check on the item... call it up over the internet.
//            startTest(this);
//        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        // set up the last know amount....
        String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(lastAmt != null)
            editor.putString("lastamt", lastAmt);
        editor.commit();
    }

    private void putMySharedPreferences() {
        String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", userData.getUsername());
        editor.putString("password", userData.getPassword());
        if(userData.getLoggedIn())
            editor.putBoolean("login_info_verified", userData.getLoggedIn());
        editor.putString("website1", userData.getWebsite1());
        editor.putString("website2", userData.getWebsite2());
        editor.putBoolean("useweb1",userData.useWeb1());
        editor.commit();
    }
    private void getMySharedPreferences() {
        String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        if(sharedPreferences.getString("username",null) != null)
        {
            if(userData == null)
                userData = new UserData();
            userData.setUsername(sharedPreferences.getString("username", null));
            userData.setPassword(sharedPreferences.getString("password", null), sharedPreferences.getString("password", null));
            userData.setLoggedIn(sharedPreferences.getBoolean("login_info_verified",true));
            userData.setWebsite1(sharedPreferences.getString("website1",null));
            userData.setWebsite2(sharedPreferences.getString("website2",null));
            userData.setUseWeb1(sharedPreferences.getBoolean("useweb1",true));
            if(lastAmt == null)
                lastAmt = new String();
            lastAmt = sharedPreferences.getString("lastamt",null);
            String a = String.valueOf(new StringBuilder("Annie\'s Bank Balance may be ").append(lastAmt));
            TextView thisText = (TextView) findViewById(R.id.txt_annie_bank);
            thisText.setText(a);
        }
    }


    public void startTest(Context context) {
        startTest();
    }

    public void startTest() {
        // for now, don't get the account total

        if(userData == null)
            userData = new UserData();
        Intent intent = new Intent(this, GetAccountTotal.class);
        intent.putExtra("userdata", userData);
        startActivityForResult(intent, 2);
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
    boolean checkConnection(Context incontext)
    {
        TextView wifiText = (TextView) findViewById(R.id.txt_wifi_available);
        if(hostComm.checkConnection(incontext) == false){
             // Internet connection doesn't exist
             showAlertDialog(this, "No Internet Connection",
                    "Your device doesn't have WIFI internet access", false);
            wifiText.setText("No WIFI.  Application needs WIFI to complete tasks.");
            return false;
        }
        else {
            // update the content wifi text item to indicate WIFI tested ok.
            wifiText.setText("WIFI tested and available.");
            return true;
        }

    }


    // do we have internet connectivity?
     public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.agt_action_success : R.drawable.agt_action_fail);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
         // it blows here.
        alertDialog.show();
    }


    public void logindata(/*View view*/) {
        // start activity that allows user to input user name, and password (twice with verification)
        if(userData == null) {
            userData = new UserData();
        }
        // we should do all the Pref stuff in the Main I think.
        Intent intent = new Intent(this, UpdateLoginDataActivity.class);
        intent.putExtra("userdata", userData);
        startActivityForResult(intent, 3);
    }
    public void update(/*View view*/) {
        // start activity that allows user to input user name, and password (twice with verification)
        if(userData == null) {
            userData = new UserData();
        }
        // we should do all the Pref stuff in the Main I think.
        Intent intent = new Intent(this, InputTransaction.class);
        intent.putExtra("userdata", userData);
        startActivityForResult(intent, 4);
    }
    public void viewaccount()
    {
        if(checkConnection(context))
            startTest();
    }
}
