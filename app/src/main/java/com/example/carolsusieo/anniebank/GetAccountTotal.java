package com.example.carolsusieo.anniebank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class GetAccountTotal extends AppCompatActivity {

    UserData userData;
    CommResult commResult;
    int whichTime =0;
    Intent intentIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_get_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intentIn = getIntent();
        Bundle bundle = intentIn.getExtras();
        userData = (UserData) bundle.getSerializable("userdata");


        commResult = new CommResult();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(whichTime ==0) {
                    onStart();
                     whichTime = 1;
                }
                else
                    updateScreen();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new LoginHttpRequest2(userData,commResult,this.getApplicationContext()).execute();

        // hopefully commResult and userData are now updated... let's see.

        // when do we know the action has completed?

        // we need to pass the information in CommResult to the calling routine if it was set

    }
    @Override
    protected void onStop() {
        super.onStop();

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

    protected void updateScreen() {

            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
            TextView greetingContentText = (TextView) findViewById(R.id.content_value);
            if (commResult != null) {
                if (commResult.getStage() == 3) {
                    greetingIdText.setText(commResult.getId());
                    greetingContentText.setText(commResult.getContent());
                } else {
                    greetingIdText.setText(commResult.getStage());
                    greetingContentText.setText(commResult.getCodeString());
                }

            } else {
                greetingIdText.setText("error");
                greetingContentText.setText("error also");
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        // need to address this TODO
        //getMenuInflater().inflate(R.menu.rest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new LoginHttpRequest2(userData,commResult,this).execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }







    private class LoginHttpRequest2 extends AsyncTask<Void, Void, CommResult> {

        private UserData userData;
        CommResult commResult;

        HostComm hostComm;
        public LoginHttpRequest2 (UserData in, CommResult inget, Context contextIn) {
            userData = in;
            commResult = inget;
            hostComm = new HostComm(contextIn);
        }



        @Override
        protected CommResult doInBackground(Void... params) {
            commResult.put("stage", 1);
            CommResult retval = Stage1(commResult);

            if (!isCancelled() && retval.getCode() == HttpURLConnection.HTTP_OK) {
                doProgress(33);
                //publishProgress(33);
                commResult.put("stage", 2);
                if(userData == null || userData.getLoggedIn() == false )
                    retval = Login(retval);
                if (retval != null && !isCancelled() && retval.getCode() == HttpURLConnection.HTTP_OK) {
                    if(userData == null) {
                        userData = new UserData();
                    }
                    userData.setLoggedIn(true);
                    doProgress(66);
                    //publishProgress(66);
                    commResult.put("stage", 3);
                    try {
                        retval = GetData(retval);
                        doProgress(100);
                        //publishProgress(100);
                    }catch (Exception e) {

                    }
                }
            }
            return retval;
        }

        public void doProgress(int value){
            publishProgress();
        }


        @Override
        protected void onPostExecute(CommResult commResult) {

            // we need to pass the information in CommResult to the calling routine if it was set

            TextView greetingIdText = (TextView) findViewById(R.id.id_value);
            TextView greetingContentText = (TextView) findViewById(R.id.content_value);
            if (commResult != null) {
                if (commResult.getStage() == 3) {
                    greetingIdText.setText(commResult.getId());
                    greetingContentText.setText(commResult.getContent());
                } else {
                    greetingIdText.setText(commResult.getStage());
                    greetingContentText.setText(commResult.getCodeString());
                }

            } else {
                greetingIdText.setText("error");
                greetingContentText.setText("error also");
            }

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

        private CommResult GetData(CommResult output) throws XmlPullParserException, IOException {

            URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpGetAmt));

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

            return hostComm.getCommunication(url);

        }
    }

 }

