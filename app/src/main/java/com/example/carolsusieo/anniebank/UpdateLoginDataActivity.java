package com.example.carolsusieo.anniebank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateLoginDataActivity extends AppCompatActivity {
    private Button _loginBtn;
    private Button _loginTestBtn;
    private TextView _loginErrorText;

    private UserData userData;
    private CommResult commResult;
    private Intent intentIn;
    //Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentIn = getIntent();
        Bundle bundle = intentIn.getExtras();
        userData = (UserData) bundle.getSerializable(getString(R.string.userFile));
        commResult = new CommResult();



        setContentView(R.layout.activity_update_login_data);
        SetValues(userData);

        _loginBtn = (Button) findViewById(R.id.btn_login);
        _loginTestBtn = (Button) findViewById(R.id.btn_login_test);
        _loginErrorText = (TextView) findViewById(R.id.loginErrorReason);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Test Saved Login Information", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        _loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TestUsername(userData) && TestPassword(userData)) {
                        GetWebsites(userData);
                      _loginBtn.setText(getString(R.string.logStored));
                    _loginErrorText.setText(getString(R.string.empty));

                } else {
                    _loginBtn.setText(getString(R.string.reenter));
                    _loginErrorText.setText(getString(R.string.userError));
                }
            }

        });

        _loginTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TestUsername(userData) && TestPassword(userData)) {
                    GetWebsites(userData);
                    _loginErrorText.setText(getString(R.string.empty));
                    // store the information so it can be used.
                    // now do a test of the communications.....
                    new LoginHttpRequest(userData, commResult, v.getContext()).execute();

                } else {
                    _loginBtn.setText(getString(R.string.reenter));
                    _loginErrorText.setText(getString(R.string.userError));
                }
            }

        });

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
    public void onBackPressed()
    {
        if(userData != null)
         intentIn.putExtra(getString(R.string.userFile), userData);
        setResult(RESULT_OK, intentIn);
        finish();
    }


    // we want to test the password and the reenter password have been filled out as expected and that the password and the reenter password match.
    // we might also want to add whatever other tests are specific to password and username on the host side of this system.
    private void setItem(int in, String inStr) {
        EditText item = (EditText)findViewById(in);
        item.setText(inStr);
    }
    private void setItem(int in,boolean inVal)
    {
        CheckBox checkBox = (CheckBox) findViewById(in);
        checkBox.setChecked(inVal);
    }
    private void SetWebsites(UserData userData) {
        setItem(R.id.fld_web1,userData.getWebsite1());
        setItem(R.id.fld_web2, userData.getWebsite2());
        setItem(R.id.checkbox_use_web1, userData.useWeb1());
    }
    private String getItem(int in) {
        EditText item = (EditText)findViewById(in);
        return item.getText().toString();
    }
    private void GetWebsites(UserData userData){
        userData.setWebsite1(getItem(R.id.fld_web1));
        userData.setWebsite2(getItem(R.id.fld_web2));
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_use_web1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if((getItem(R.id.fld_web2)).isEmpty() || !checkBox.isChecked())
                userData.setUseWeb1(false);
            else
                userData.setUseWeb1(true);
        }
    }
    private boolean TestPassword(UserData userData) {
        // for now, let's make sure it's filled in with a least one character
        return(userData.setPassword(getItem(R.id.fld_pwd), getItem(R.id.fld_pwd2)));
    }
    private boolean TestUsername(UserData userData) {
        return(userData.setUsername(getItem(R.id.fld_username)));
    }
    private void SetValues(UserData userData) {
         if(userData != null && userData.getPassword() != null) {
             setItem(R.id.fld_pwd,userData.getPassword());
             setItem(R.id.fld_pwd2,userData.getPassword());
             setItem(R.id.fld_username,userData.getUsername());
             SetWebsites(userData);
        }
    }



    // this doesn't happen without the entire activity starting
    // so, when the GetAccountTotal activity completes, we see it's screen up
    // even though we didn't start that activity
    // also getting errors on exiting out of this
    private class LoginHttpRequest extends AsyncTask<Void, Void, CommResult> {

        private final UserData userData;
        final CommResult getcommresult;
        final HostComm hostComm;
        final ProgressDialog progress;

        public LoginHttpRequest(UserData in, CommResult inget, Context contextIn) {
            userData = in;
            getcommresult = inget;
            hostComm = new HostComm(contextIn);
            progress = new ProgressDialog(contextIn);
            initiateProgress();
        }

        @Override
        protected CommResult doInBackground(Void... params) {
            /*return DontNeedToSee(getcommresult);*/
            getcommresult.putStage(1);
            doProgress(1);
            CommResult retval = Stage1(getcommresult);
            if (!isCancelled() && retval != null && retval.getCode() == HttpURLConnection.HTTP_OK) {
                doProgress(2);
                //publishProgress(33);
                getcommresult.putStage(2);
                retval = Login(retval);
                doProgress(3);
            }
            return retval;
        }

        public void initiateProgress() {
            progress.setMessage(getString(R.string.GetBalance));
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(true);
            progress.show();
            progress.setProgress(0);
        }

        public void doProgress(int value) {
            // todo determine why this is not working
            progress.setProgress(100 / 4 * value);

        }

        public CommResult updateOutput(CommResult in){

            in.putMessage(hostComm.getRM());
            in.putCode( hostComm.getRC());
            return in;
        }
        @Override
        protected void onPostExecute(CommResult commresult) {
            // we need to pass the information in CommResult to the calling routine if it was set
            if (commresult != null) {
                if (commresult.getStage() == 2) {
                    // we are logged in
                    userData.setLoggedIn(true);
                    _loginTestBtn.setText(getString(R.string.logOk));
                    _loginErrorText.setText(getString(R.string.empty));
                    // indicate that the log in information as been verified
                }
                else {
                    _loginTestBtn.setText(getString(R.string.logFailedRetry));
                    _loginErrorText.setText(commresult.getRespMessage());
                }
            }
            else {
                _loginTestBtn.setText(getString(R.string.logFail));
                _loginErrorText.setText(getString(R.string.checkComm));
            }
            progress.dismiss();
        }
        private CommResult Stage1(CommResult output) {

            try {
                URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpSessionToken));
                hostComm.RequestCSRFToken(url);
                return(updateOutput(output));
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        private CommResult Login(CommResult output) {

            try {
                 URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpLogin));
                int ret = hostComm.Login(url,userData);
                if(ret == HttpURLConnection.HTTP_OK) {
                    return updateOutput(output);
                }
                else
                    return null;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

/*
        private CommResult GetLoginData(CommResult output) throws XmlPullParserException, IOException {
             URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpLogin));

            String result = loadXmlFromNetwork(url);

            if (result.length() > 0) {
                output.putContent( result);
            }

            return output;
        }
*/
        // Uploads XML, parses it, and combines it with
        // HTML markup. Returns HTML string.

        private String loadXmlFromNetwork(URL url) //throws XmlPullParserException, IOException
        {
            // Instantiate the parser
            CarolOdiorneXmlParser carolOdiorneXmlParser = new CarolOdiorneXmlParser();
            String result = null;
            HttpURLConnection conn = null;
            try {
                conn = downloadUrl(url);
                if(hostComm.getInputStream() != null)
                    // it fails in here....
                    try {
                        result = carolOdiorneXmlParser.parse(hostComm.getInputStream(),getResources());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
            } finally {
                if (hostComm.getInputStream() != null) {
                    try {
                        (hostComm.getInputStream()).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(conn != null)
                        conn.disconnect();
                }
            }
            return result;
        }

        private HttpURLConnection downloadUrl(URL url) //throws IOException {
        {

             // if loginOnly is set, we need to get this information from the userData array
            String a = getString(R.string.xmlUsername) + userData.getUsername() + getString(R.string.xmlSeparator) + getString(R.string.xmlPassword) + userData.getPassword() + getString(R.string.xmlSeparator) + getString(R.string.xmlLoginFrm);
            return hostComm.putCommunication(url,a);

        }
    }

}
