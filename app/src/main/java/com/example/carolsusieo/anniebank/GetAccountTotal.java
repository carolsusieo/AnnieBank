package com.example.carolsusieo.anniebank;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class GetAccountTotal extends AppCompatActivity {

    private UserData userData;
    //private UserData userData2;
    private TranData tranData;
    private CommResult commResult;
//    private Intent intentIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //resources = getResources();
        setContentView(R.layout.activity_get_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

 //       intentIn = getIntent();


         // read trandata from the TinyDB
        tranData = new TranData();
        TinyDB tinydb = new TinyDB(getApplicationContext());
        userData = (UserData) tinydb.getObject("UserData",UserData.class);

        float lastamt = tinydb.getFloat("TranDataDataAmt");
        tranData.setLastAmt(lastamt);
        ArrayList<Object> objArray = tinydb.getListObject("TranDataData", TranDataData.class);
        ArrayList<TranDataData> tranArray = new ArrayList<>();
        for(Object obj: objArray) {
            TranDataData in = (TranDataData) obj;
            tranArray.add(in);
        }
        tranData.remakeStoredTrans(tranArray);

        commResult = new CommResult();
        Button _sendBtn = (Button) findViewById(R.id.btn_get_acct);

        _sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HttpRequest(userData, commResult, v.getContext()).execute();

            }
        });
    }

    public void onBackPressed()
    {
        TinyDB tinydb = new TinyDB(getApplicationContext());
//        if(intentIn != null) {
            if (userData != null) {
                //intentIn.putExtra(getString(R.string.userFile), userData);
                tinydb.putObject("UserData",userData);
            }
            if(tranData != null) {
                if(commResult != null) {
                    String cr = commResult.getContent();
                    if(cr != null) {
                        float f = Float.valueOf(commResult.getContent().trim());
                        tranData.setLastAmt(f);
                        tinydb.putFloat("TranDataDataAmt",f);
                    }
                }
            }
            setResult(RESULT_OK, getIntent());
  //      }
        finish();
    }

    protected void updateScreen() {
        // there are two text displays on top, and then two underneath them.

            TextView commResultText = (TextView) findViewById(R.id.commResultValue);
            TextView balanceText = (TextView) findViewById(R.id.balanceResultValue);
            if (commResult != null) {
                if (commResult.getStage() == 3) {
                    commResultText.setText(commResult.getRespMessage());
                    float f = Float.valueOf(commResult.getContent().trim());
                    f -= tranData.getValueOfStoredTransactions();
                    tranData.setLastAmt(f);
                    balanceText.setText(tranData.getLastAmt());
                } else {
                    commResultText.setText(getString(R.string.error));
                    balanceText.setText(commResult.getRespMessage());
                }

            } else {
                 commResultText.setText(getString(R.string.error));
                balanceText.setText(getString(R.string.error));
            }
    }
// todo want to be able to display current transactions not sent to host.




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        // need to address this TODO
        //getMenuInflater().inflate(R.menu.rest, menu);
        return true;
    }



    private class HttpRequest extends AsyncTask<Void, Void, CommResult> {

        private UserData userData;
        final CommResult commResult;
        ProgressDialog progress;
        final HostComm hostComm;
        public HttpRequest (UserData in, CommResult inget, Context contextIn) {
            userData = in;
            commResult = inget;
            hostComm = new HostComm(contextIn);
            progress = null;
            progress = new ProgressDialog(contextIn);
             initiateProgress();

        }



        @Override
        protected CommResult doInBackground(Void... params) {
            commResult.putStage(1);
            doProgress(1);
            CommResult retval = Stage1(commResult);

            if (!isCancelled() && retval != null && retval.getCode() == HttpURLConnection.HTTP_OK) {
                doProgress(2);
                //publishProgress(33);
                commResult.putStage( 2);
                if(userData == null || !userData.getLoggedIn() )
                    retval = Login(retval);
                if (retval != null && !isCancelled() && retval.getCode() == HttpURLConnection.HTTP_OK) {
                    if(userData == null) {
                        userData = new UserData();
                    }
                    userData.setLoggedIn(true);
                    doProgress(3);
                    //publishProgress(66);
                    commResult.putStage(3);
                    try {
                        retval = GetData(retval);
                        doProgress(4);
                        //publishProgress(100);
                    }catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }
            return retval;
        }

        public void initiateProgress() {
            if(progress != null) {
                progress.setMessage(getString(R.string.GetBalance));
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setIndeterminate(true);
                 progress.show();
                progress.setProgress(0);
            }
        }
        public void doProgress(int value){
            if(progress != null) progress.setProgress(100/4 * value);
        }


        @Override
        protected void onPostExecute(CommResult commResult) {

            // we need to pass the information in CommResult to the calling routine if it was set
            updateScreen();
            if(progress !=null)
                progress.dismiss();
        }




        private CommResult Stage1(CommResult output) {

            try {
                URL url = hostComm.CreateURL(userData,getString(R.string.lbl_web1),getString(R.string.httpSessionToken));

                // POST seemed to work... sent back 200
                //  http:/www.carolodiorne.com/?q=services/session/token
                // currently saying 405 method not allowed - why?
                hostComm.RequestCSRFToken(url);

                output.putMessage(hostComm.getRM());
                output.putCode( hostComm.getRC());

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
                    output.putMessage(hostComm.getRM());
                    output.putCode(hostComm.getRC());

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

            String result = loadXmlFromNetwork(url);

            if (result.length() > 0) {
                output.putContent( result);
            }

            return output;
        }


        // Uploads XML, parses it, and combines it with
        // HTML markup. Returns HTML string.

        private String loadXmlFromNetwork(URL url) throws XmlPullParserException, IOException {
            // Instantiate the parser
            CarolOdiorneXmlParser carolOdiorneXmlParser = new CarolOdiorneXmlParser();


            String result = null;
            HttpURLConnection conn = null;
            try {

                conn = downloadUrl(url);
                if(hostComm.getInputStream() != null)
                    result = carolOdiorneXmlParser.parse(hostComm.getInputStream(),getResources());

            } finally {
                if (hostComm.getInputStream() != null) {
                    (hostComm.getInputStream()).close();
                    if(conn != null)
                        conn.disconnect();
                }
            }

            return result;
        }

        private HttpURLConnection downloadUrl(URL url) //throws IOException
        {

            return hostComm.getCommunication(url);

        }
    }

 }

