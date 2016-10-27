package com.example.carolsusieo.anniebank;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class InputTransaction extends AppCompatActivity {

    private Button _sendBtn;
    private Button _viewBtn;
    private Button _clearBtn;
    private Button _storeBtn;

    private TransactionData transactionData;
    private UserData userData;
    private TranData tranData;
    private CommResult commResult;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _sendBtn = (Button) findViewById(R.id.btn_send);
        _clearBtn = (Button) findViewById(R.id.btn_clear);
        _storeBtn = (Button) findViewById(R.id.btn_store);
        _viewBtn = (Button) findViewById(R.id.btn_display_stored_trans);


        TinyDB tinydb = new TinyDB(getApplicationContext());
        userData = (UserData) tinydb.getObject("UserData", UserData.class);


        ArrayList<TranDataData> tranArray = setupTranData(readDatabase());
        // there is more to tranData than just the array of stored transactions..
        // there is also values sent from the host already.
        if(tranData == null) {
            tranData = new TranData();
        }

        tranData.remakeStoredTrans(tranArray);
        setLastAmt(tranData);
        transactionData = new TransactionData();
        updateScreenText(tranData);



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        _sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tranData.getNumStoredTrans() > 0) {
                    String fld_amt = getItem(R.id.fld_amt);
                    if (fld_amt.equals(getString(R.string.empty)) || fld_amt.contentEquals(getString(R.string.empty))) {
                        showAlertDialog(getApplicationContext(), getString(R.string.clearTran),
                                getString(R.string.clearMsg), false);
                        clearTransaction(tranData);
                    }
                    commResult = new CommResult();
                    // verify that we are already logged in
                    if (tranData.getNumStoredTrans() > 0) {
                        new TranHttpRequest(userData, commResult, v.getContext()).execute();
                        setSendBtnText(tranData);
                        setNumberStoredTransText(tranData);
                    }
                }

            }
        });
        // look at stored transactions
        _viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tranData.getNumStoredTrans() > 0) {
                    saveTransactions(tranData);
                    viewTransactions();
                }
            }
        });

        _clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(getApplicationContext(), getString(R.string.clearTran),
                        getString(R.string.clearMsg), false);
                clearTransaction(tranData);
            }
        });

        _storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((getData(transactionData)) != null) {
                    tranData.storeTran(transactionData);
                    saveTransactions(tranData);
                }
                updateScreenText(tranData);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setLastAmt(TranData trandata) {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        float amt = tinydb.getFloat("TranDataDataAmt");
        if (amt > 0.0)
            trandata.setLastAmt(amt + trandata.getLastAmtFloat());
    }

    private ArrayList<Object> readDatabase() {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        /* no workee
        TranData hold = (TranData)tinydb.getObject("TranData",TranData.class);
        if(hold != null)
            tranData = hold;
        */
        return tinydb.getListObject("TranDataData", TranDataData.class);
    }

    private void writeDatabase(ArrayList<Object> array) {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        tinydb.putFloat("TranDataDataAmt", tranData.getLastAmtFloat());
        tinydb.putListObject("TranDataData", array);
        /*
        tinydb.putObject("TranData",tranData);
        */
    }

    private ArrayList<TranDataData> setupTranData(ArrayList<Object> objArray) {
        ArrayList<TranDataData> tranArray = new ArrayList<>();
        for (Object obj : objArray) {
            TranDataData in = (TranDataData) obj;
            tranArray.add(in);
        }
        return tranArray;
    }


    private ArrayList<TranDataData> readTransactions(TranData tranDataIn) {
        ArrayList<TranDataData> tranArray = setupTranData(readDatabase());
        tranDataIn.remakeStoredTrans(tranArray);
        updateScreenText(tranDataIn);
        /*
        TinyDB tinydb = new TinyDB(getApplicationContext());
        TranData hld = (TranData) tinydb.getObject("TranData",TranData.class);
        if(hld != null)
            tranData = hld;
        */
        return tranArray;
    }

    private void saveTransactions(TranData tranDataIn) {
        //ArrayList<TranDataData> tranArray =  ;

        ArrayList<Object> array = new ArrayList<Object>();
        for (TranDataData item : tranDataIn.getTranArrayData())
            array.add(item);
        writeDatabase(array);
    }

    private void clearItem(int in) {
        EditText item = (EditText) findViewById(in);
        item.setText(getString(R.string.empty));
    }

    private void clearTransaction(TranData tranDataIn) {
        clearItem(R.id.fld_amt);
        clearItem(R.id.fld_tran_desc);
        updateScreenText(tranDataIn);
    }

    private void updateScreenText(TranData tranDataIn) {
        setSendBtnText(tranDataIn);
        setNumberStoredTransText(tranDataIn);
        setItem(R.id.currentBalance, tranDataIn.getLastAmt());
    }

    private void setNumberStoredTransText(TranData tranDataIn) {
        TextView storeText = (TextView) findViewById(R.id.storedTransNum);
        storeText.setText(Integer.toString(tranDataIn.getNumStoredTrans()));
    }

    private void setSendBtnText(TranData tranDataIn) {
        if (tranDataIn.getNumStoredTrans() > 0) {
            _sendBtn.setText(getString(R.string.send_transactions));
        } else {
            _sendBtn.setText(getString(R.string.send_transaction));
        }
    }

    public void onBackPressed() {
        if (userData != null) {
            TinyDB tinydb = new TinyDB(getApplicationContext());
            tinydb.putObject("UserData", userData);

        }
        saveTransactions(tranData);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    //testing dialog below main...
    private void showAlertDialog(Context context, String title, String message, Boolean status) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        // Setting Dialog Title
        alertDialog.setTitle(title);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.agt_action_success : R.drawable.agt_action_fail);
        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    private String getItem(int in) {
        EditText item = (EditText) findViewById(in);
        return item.getText().toString();
    }

    private TransactionData getData(TransactionData transactionData) {
        String fld_desc = getItem(R.id.fld_tran_desc);
        String fld_amt = getItem(R.id.fld_amt);
        if (fld_amt.trim().length() == 0 || fld_amt.contentEquals(getString(R.string.empty)))
            return null;
        else {
            RadioGroup currencyBlock = (RadioGroup) findViewById(R.id.currency_radio);
            int selectedId = currencyBlock.getCheckedRadioButtonId();
            RadioButton currencyButton = (RadioButton) findViewById(selectedId);
            transactionData.putCurrency(currencyButton.getText().toString());
            transactionData.putAmount(fld_amt);
            transactionData.putDesc(fld_desc);
        }
        return transactionData;
    }

    private void setItem(int in, String inStr) {
        TextView item = (TextView) findViewById(in);
        item.setText(inStr);
    }

    private void updateScreenAfterComm() {
        if (commResult != null) {
            if (commResult.getStage() >= 3) {
                setItem(R.id.commResult, commResult.getRespMessage());
                _sendBtn.setText(getString(R.string.tran_sent));
            } else {
                setItem(R.id.commResult, getString(R.string.error));
                _sendBtn.setText(getString(R.string.tran_failed_retry));
            }
        } else {
            setItem(R.id.commResult, getString(R.string.error));
            _sendBtn.setText(R.string.tran_failed);
        }
        setItem(R.id.storedTransNum, Integer.toString(tranData.getNumStoredTrans()));
        setItem(R.id.currentBalance, tranData.getLastAmt());
    }

    private static final int VIEW_ACTIVITY = 3;

    private void viewTransactions(/*View view*/) {
        Intent intent = new Intent(this, ViewStoredTransactions.class);
        tranData.holdLastAmt();
        startActivityForResult(intent, VIEW_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         switch (requestCode) {
            case VIEW_ACTIVITY:
                readTransactions(tranData);
                updateScreenText(tranData);
                break;
        }
   }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "InputTransaction Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.carolsusieo.anniebank/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "InputTransaction Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.carolsusieo.anniebank/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class TranHttpRequest extends AsyncTask<Void, Void, CommResult> {

        final CommResult postTran;
        final HostComm hostComm;
        UserData userData;
        final ProgressDialog progress;
        int progressMark = 0;

        public TranHttpRequest(UserData userDatain, CommResult inget, Context contextIn) {
            // we already have access to transaction data, but it won't hurt....
            postTran = inget;
            userData = userDatain;
            hostComm = new HostComm(contextIn);
            progress = new ProgressDialog(contextIn);
        }

        @Override
        protected CommResult doInBackground(Void... params) {
            CommResult retval = Stage1(postTran);
            doProgress(1);
            if (retval != null) {
                retval.putStage(1);
            }
            if (!isCancelled() && retval != null && retval.getCode() == HttpURLConnection.HTTP_OK) {
                // progress is going to depend on number of stored transactions....
                doProgress(2);
                retval.putStage(2);
                if (userData == null || !userData.getLoggedIn())
                    retval = Login(retval);
                if (!isCancelled() && retval != null && retval.getCode() == HttpURLConnection.HTTP_OK) {
                    if (userData == null) {
                        userData = new UserData();
                    }
                    userData.setLoggedIn(true);
                    doProgress(3);
                    retval.putStage(3);
                    try {
                        // if this is the first time...  and I haven't logged in somewhere else...  this SendTran fails...
                        while ((transactionData = tranData.getStoredTrans()) != null) {
                            retval = SendTran(retval);
                            if (retval != null) {
                                tranData.decStoredTrans();
                                doProgress(3);
                            }
                        }
                        //publishProgress(100);
                    } catch (Exception e) {
                        // we need to indicate that everything didn't go perfectly
                        if (retval != null)
                            retval.putStage(2);
                    }
                }
            }
            return retval;
        }

        public void initiateProgress() {
            progress.setMessage(getString(R.string.SendTransactions));
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setIndeterminate(true);
            progressMark = tranData.getNumStoredTrans();
            progress.show();
            progress.setProgress(0);
        }

        // currently not progessing
        int max;

        public void doProgress(int value) {
            int percentage = 0;
            if (value == 1) {
                max = progressMark + 2;
            } else {
                //todo determine why this doesn't seem to be working
                percentage = (100 / max) * (max - progressMark);
                progressMark--;
            }
            progress.incrementProgressBy(percentage);
        }

        @Override
        protected void onPreExecute() {
            initiateProgress();
        }

        @Override
        protected void onPostExecute(CommResult commResult) {
            updateScreenAfterComm();
            progress.dismiss();
        }

        public CommResult updateOutput(CommResult output) {
            output.putMessage(hostComm.getRM());
            output.putCode(hostComm.getRC());
            return output;
        }

        private CommResult Stage1(CommResult output) {
            try {
                URL url = hostComm.CreateURL(userData, getString(R.string.lbl_web1), getString(R.string.httpSessionToken));
                hostComm.RequestCSRFToken(url);
                return (updateOutput(output));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        private CommResult Login(CommResult output) {
            try {
                URL url = hostComm.CreateURL(userData, getString(R.string.lbl_web1), getString(R.string.httpLogin));
                int ret = hostComm.Login(url, userData);

                if (ret == HttpURLConnection.HTTP_OK) {
                    return updateOutput(output);
                } else {
                    // why not update output even on comm failure?  We could display the error
                    return null;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        private CommResult SendTran(CommResult output) {
            URL url = hostComm.CreateURL(userData, getString(R.string.lbl_web1), getString(R.string.httpPut));
            String result = loadXmlFromNetwork(url);
            if (result.length() > 0) {
                // initial transactions might go, while subsequent ones do not...
                output.putContent(result);
                output.putStage(4);
            }
            return output;
        }

        private String loadXmlFromNetwork(URL url) //throws XmlPullParserException, IOException
        {
            String result = null;
            HttpURLConnection conn = null;
            try {
                conn = downloadUrl(url);
                try {
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        result = getString(R.string.ok);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                if (hostComm.getInputStream() != null) {
                    try {
                        (hostComm.getInputStream()).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (conn != null)
                        conn.disconnect();
                }
            }
            return result;
        }

        private HttpURLConnection downloadUrl(URL url) //throws IOException {
        {

            String a = String.valueOf(new StringBuilder(getString(R.string.xmlAmt)).append(transactionData.getAmount())
                    .append(getString(R.string.xmlSeparator)).append(getString(R.string.xmlLabel)).append(transactionData.getDesc())
                    .append(getString(R.string.xmlSeparator)).append(getString(R.string.xmlCurrency)).append(transactionData.getCurrency()));
            return hostComm.putCommunication(url, a);
        }
    }
}