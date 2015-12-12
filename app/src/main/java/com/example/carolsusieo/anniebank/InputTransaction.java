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
import android.widget.EditText;

import org.springframework.http.HttpRequest;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InputTransaction extends AppCompatActivity {

    Button _sendBtn;
    Button _anotherBtn;

    //CommResult commResult;
    TransactionData tranData;
    Intent intentIn;
    UserData userData;
    CommResult commResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //commResult = new CommResult();
        _sendBtn = (Button) findViewById(R.id.btn_send);
        _anotherBtn = (Button) findViewById(R.id.btn_another);

        intentIn = getIntent();
        Bundle bundle = intentIn.getExtras();
        userData = (UserData) bundle.getSerializable("userdata");



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the information and store it.  Start the communication
                EditText desc   = (EditText)findViewById(R.id.fld_tran_desc);
                String fld_desc = desc.getText().toString();
                EditText amt   = (EditText)findViewById(R.id.fld_amt);
                String fld_amt = amt.getText().toString();
                tranData.putAmount(fld_amt);
                tranData.putDesc(fld_desc);
                // verify that we are already logged in
                new TranHttpRequest(userData, commResult, tranData, v.getContext()).execute();


            }

        });

        _anotherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if previous information was not sent to the host due to some sort of wifi problem, ask if they want to store info until
                // wifi is ready, or if they just want to clear the data



                // clear out the information that is currently stored in preparation to input more\

                EditText desc   = (EditText)findViewById(R.id.fld_tran_desc);
                desc.setText("");
                EditText amt   = (EditText)findViewById(R.id.fld_amt);
                amt.setText("");
                // change the button to display Send Transaction
                _sendBtn.setText("Send Transaction");
            }

        });
    }

    public void onBackPressed()
    {
        if(intentIn != null) {
            if (userData != null)
                intentIn.putExtra("userdata", userData);
            if (commResult != null)
                intentIn.putExtra("MESSAGE", commResult.getContent());
            else
                intentIn.putExtra("MESSAGE", "unknown");
            setResult(RESULT_OK, intentIn);
        }
        finish();
    }

    private class TranHttpRequest extends AsyncTask<Void, Void, CommResult> {

        CommResult postTran;
        HostComm hostComm;
        UserData userData;

        public TranHttpRequest ( UserData userDatain,CommResult inget, TransactionData in,Context contextIn) {
            // we already have access to transaction data, but it won't hurt....
            tranData = in;
            postTran = inget;
            userData = userDatain;
            hostComm = new HostComm(contextIn);
        }



        @Override
        protected CommResult doInBackground(Void... params) {

            postTran.put("stage", 1);
            CommResult retval = Stage1(postTran);

            if (!isCancelled() && retval != null  && retval.getCode() == HttpURLConnection.HTTP_OK) {
                doProgress(33);
                if (userData == null || userData.getLoggedIn() == false)
                    retval = Login(retval);
                if (!isCancelled() && retval.getCode() == HttpURLConnection.HTTP_OK) {
                    if (userData == null) {
                        userData = new UserData();
                    }
                    userData.setLoggedIn(true);
                    doProgress(66);
                    //publishProgress(66);
                    retval.put("stage", 3);
                    try {
                        retval = SendTran(retval);
                        doProgress(100);
                        //publishProgress(100);
                    } catch (Exception e) {

                    }
                }
            }
            return retval;
        }

        public void doProgress(int value){
            publishProgress();
        }


        @Override
        protected void onPostExecute(CommResult postTran) {

            // we need to pass the information in CommResult to the calling routine if it was set

            if (postTran != null) {
                if (postTran.getStage() == 2) {

                    // we are logged in
                    //
                    _sendBtn.setText("Transaction Sent");
                    // indicate that the log in information as been verified

                }
                else
                    _sendBtn.setText("Transaction Failed, Try Again");
            }
            else
                _sendBtn.setText("Transaction Failed");

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

        // hopefully we are already logged in...  We should be in order to get the amount already there... ASSUMPTION



        private CommResult SendTran(CommResult output) throws XmlPullParserException, IOException {

            URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpPut));

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
                // new result....
                if(hostComm.getInputStream() != null)
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
            String a;
            a = String.valueOf(new StringBuilder("amt=").append(tranData.getAmount()).append("&desc").append(tranData.getDesc()));

            return hostComm.putCommunication(url,a);

        }
    }


}
