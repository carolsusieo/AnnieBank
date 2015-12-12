package com.example.carolsusieo.anniebank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateLoginDataActivity extends AppCompatActivity {
    Button _loginBtn;
    Button _loginTestBtn;

    UserData userData;
    CommResult commResult;
    Intent intentIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentIn = getIntent();

 //       userData = new UserData();
        Bundle bundle = intentIn.getExtras();
        userData = (UserData) bundle.getSerializable("userdata");
        commResult = new CommResult();



        setContentView(R.layout.activity_update_login_data);
        SetValues(userData);

        _loginBtn = (Button) findViewById(R.id.btn_login);
        _loginTestBtn = (Button) findViewById(R.id.btn_login_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Test Saved Login Information", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TestUsername(userData) && TestPassword(userData)) {
                        GetWebsites(userData);
                      _loginBtn.setText("Login Data Stored");

                } else {
                    _loginBtn.setText("Reenter Information");
                    // nothing stored....
                }
            }

        });

        _loginTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(TestUsername(userData) && TestPassword(userData))
            {
                GetWebsites(userData);
                // store the information so it can be used.
                // now do a test of the communications.....
                new LoginHttpRequest(userData, commResult,v.getContext()).execute();

            }
            else
                _loginBtn.setText("Reenter Information");

            }

        });
 //       final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_use_web1);
 //       if (checkBox.isChecked()) {
 //           checkBox.setChecked(false);
 //       }

    }
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_use_web1:
                userData.setUseWeb1((checked));
                 break;
         }
    }
    @Override
    protected void onStop() {
        super.onStop();

    }
    public void onBackPressed()
    {
        if(userData != null)
         intentIn.putExtra("userdata", userData);
        setResult(RESULT_OK, intentIn);
        finish();
    }


    // we want to test the password and the reenter password have been filled out as expected and that the password and the reenter password match.
    // we might also want to add whatever other tests are specific to password and username on the host side of this system.


    // this doesn't appear to work
    private void SetWebsites(UserData userData) {
        EditText web1T = (EditText)findViewById(R.id.fld_web1);
        web1T.setText(userData.getWebsite1());
        EditText web2T = (EditText)findViewById(R.id.fld_web2);
        web2T.setText(userData.getWebsite2());
       // if(userData.useWeb1()) {
            final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_use_web1);
            checkBox.setChecked(userData.useWeb1());

       // }

    }
    private void GetWebsites(UserData userData){
        EditText web1T = (EditText)findViewById(R.id.fld_web1);
        userData.setWebsite1(web1T.getText().toString());
        EditText web2T = (EditText)findViewById(R.id.fld_web2);
        userData.setWebsite2((web2T.getText().toString()));
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_use_web1);
        if((web2T.getText().toString().isEmpty()) || !checkBox.isChecked())
            userData.setUseWeb1(false);
        else
            userData.setUseWeb1(true);

    }

    private boolean TestPassword(UserData userData) {
        // for now, let's make sure it's filled in with a least one character
        EditText pass1   = (EditText)findViewById(R.id.fld_pwd);
        String password1 = pass1.getText().toString();
        EditText pass2 = (EditText)findViewById(R.id.fld_pwd2);
        String password2 = pass2.getText().toString();
        return(userData.setPassword(password1,password2));

    }

    private boolean TestUsername(UserData userData) {
        EditText user   = (EditText)findViewById(R.id.fld_username);
        String username = user.getText().toString();
        return(userData.setUsername(username));
     }

    private void SetValues(UserData userData) {
         if(userData != null && userData.getPassword() != null) {
            EditText pass1 = (EditText) findViewById(R.id.fld_pwd);
            pass1.setText(userData.getPassword());
            EditText pass2 = (EditText) findViewById(R.id.fld_pwd2);
            pass2.setText(userData.getPassword());
            EditText user = (EditText) findViewById(R.id.fld_username);
            user.setText(userData.getUsername());
             SetWebsites(userData);
        }
    }



    // this doesn't happen without the entire activity starting
    // so, when the GetAccountTotal activity completes, we see it's screen up
    // even though we didn't start that activity
    // also getting errors on exiting out of this
    private class LoginHttpRequest extends AsyncTask<Void, Void, CommResult> {

        private UserData userData;
        CommResult getcommresult;
        HostComm hostComm;

        public LoginHttpRequest (UserData in, CommResult inget,Context contextIn) {
            userData = in;
            getcommresult = inget;
            hostComm = new HostComm(contextIn);
        }



        @Override
        protected CommResult doInBackground(Void... params) {

            /*return DontNeedToSee(getcommresult);*/
            getcommresult.put("stage", 1);
            CommResult retval = Stage1(getcommresult);


            if (!isCancelled() && retval != null  && retval.getCode() == HttpURLConnection.HTTP_OK) {
                doProgress(33);
                //publishProgress(33);
                getcommresult.put("stage", 2);
                retval = Login(retval);
             }
            return retval;
        }

        public void doProgress(int value){
            publishProgress();
        }


        @Override
        protected void onPostExecute(CommResult commresult) {

            // we need to pass the information in CommResult to the calling routine if it was set

            if (commresult != null) {
                if (commresult.getStage() == 2) {

                    // we are logged in
                    userData.setLoggedIn(true);
                    _loginTestBtn.setText("Logged In");
                    // indicate that the log in information as been verified

                }
                else
                    _loginTestBtn.setText("Log In Failed, Try Again");
            }
            else
                _loginTestBtn.setText("Log In Failed");

        }




        private CommResult Stage1(CommResult output) {

            try {
                URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpSessionToken));

                hostComm.RequestCSRFToken(url);

                output.put("ResponseMessage", hostComm.getRM());
                output.put("ResponseCode", hostComm.getRC());

                return output;
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        private CommResult Login(CommResult output) {

            try {

 //                 GetLoginData(output);

                 URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpLogin));


                int ret = hostComm.Login(url,userData);

                if(ret == HttpURLConnection.HTTP_OK) {
                    output.put("ResponseMessage", hostComm.getRM());
                    output.put("ResponseCode", hostComm.getRC());
                    return output;
                }
                else
                    return null;


            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;

        }


        private CommResult GetLoginData(CommResult output) throws XmlPullParserException, IOException {

             URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpLogin));

            String result = loadXmlFromNetwork(url, output);

            if (result.length() > 0) {
                output.put("Output", result);
            }

            return output;
        }


        // Uploads XML, parses it, and combines it with
        // HTML markup. Returns HTML string.

        private String loadXmlFromNetwork(URL url, CommResult output) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            CarolOdiorneXmlParser carolOdiorneXmlParser = new CarolOdiorneXmlParser();


            String result = null;
            HttpURLConnection conn = null;
            try {

                conn = downloadUrl(url, output);
                if(hostComm.getInputStream() != null)
                    // it fails in here....
                    result = carolOdiorneXmlParser.parse(hostComm.getInputStream());

            } finally {
                if (hostComm.getInputStream() != null) {
                    (hostComm.getInputStream()).close();
                    if(conn != null)
                        conn.disconnect();
                }
            }

            return result;
        }

        private HttpURLConnection downloadUrl(URL url, CommResult output) throws IOException {

             // if loginOnly is set, we need to get this information for the userData array
            String a = new StringBuilder("username=").append(userData.getUsername()).append("&password=").append(userData.getPassword()).append("&form_id-login_form").toString();

            return hostComm.putCommunication(url,a);

        }
    }







}
