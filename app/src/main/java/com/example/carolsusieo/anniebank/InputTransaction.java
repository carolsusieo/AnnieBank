package com.example.carolsusieo.anniebank;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
    private Intent intentIn;
    private UserData userData;
    private TranData tranData;
    private CommResult commResult;
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

        intentIn = getIntent();
        Bundle bundle = intentIn.getExtras();
        userData = (UserData) bundle.getSerializable(getString(R.string.userFile));
       // tranData = (TranData) bundle.getSerializable(getString(R.string.tranFile));
        // read trandata from the TinyDB
        //tranData = (TranData) bundle.getSerializable(getString(R.string.tranFile));
        TinyDB tinydb = new TinyDB(getApplicationContext());
        ArrayList<Object> objArray = tinydb.getListObject("TranDataData", TranDataData.class);
        ArrayList<TranDataData> tranArray = new ArrayList<>();
        for(Object obj: objArray) {
            TranDataData in = (TranDataData) obj;
            tranArray.add(in);
        }
        tranData = new TranData();
        tranData.remakeStoredTrans(tranArray);

        transactionData = new TransactionData();

        setSendBtnText();

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        _sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the information and store it.  Start the communication
                if (tranData.getNumStoredTrans() > 0) {
                    String fld_amt = getItem(R.id.fld_amt);
                    if (fld_amt.equals(getString(R.string.empty)) || fld_amt.contentEquals(getString(R.string.empty))) {
                        showAlertDialog(getApplicationContext(), getString(R.string.clearTran),
                                getString(R.string.clearMsg), false);
                        clearTransaction();
                    }
                    commResult = new CommResult();
                    // verify that we are already logged in
                    if (tranData.getNumStoredTrans() > 0) {
                        new TranHttpRequest(userData, commResult, v.getContext()).execute();
                        setSendBtnText();
                    }
                }

            }
        });
        // look at stored transactions
        _viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the information and store it.  Start the communication
                if (tranData.getNumStoredTrans() > 0) {
                    // if we call save_transactions appropriately, we shouldn't need to update here
                    saveTransactions();
                    viewTransactions();
                    // tinyDB should of been updated in the view if transctions were deleted.
                  }
                else {
                    // there are not stored transations to display
                }
            }
        });

        _clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(getApplicationContext(), getString(R.string.clearTran),
                        getString(R.string.clearMsg), false);
                clearTransaction();
            }
        });

        _storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((getData(transactionData)) != null) {
                    tranData.storeTran(transactionData);
                    saveTransactions();
                }
                setSendBtnText();
            }
        });
    }
    private void clearItem(int in)
    {
        EditText item = (EditText) findViewById(in);
        item.setText(getString(R.string.empty));
    }
    private void clearTransaction() {
        clearItem(R.id.fld_amt);
        clearItem(R.id.fld_tran_desc);
        setSendBtnText();
    }
    private void setSendBtnText() {
        if (tranData.getNumStoredTrans() > 0) {
            _sendBtn.setText(getString(R.string.send_transactions));
        } else {
            _sendBtn.setText(getString(R.string.send_transaction));
        }
        TextView storeText = (TextView) findViewById(R.id.storedTransNum);
        storeText.setText(Integer.toString(tranData.getNumStoredTrans()));
    }
    // whenever the transaction stored information changes, this needs to be called.
    private void saveTransactions()
    {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        ArrayList<TranDataData> arrayListData =  tranData.getTranArrayData();
        ArrayList<Object> array = new ArrayList<Object>();
        for(TranDataData item: arrayListData)
            array.add(item);
        tinydb.putListObject("TranDataData", array);
    }
    private ArrayList<TranDataData> readTransactions()
    {
        TinyDB tinydb = new TinyDB(getApplicationContext());
        ArrayList<TranDataData> arrayListData =  tranData.getTranArrayData();
        ArrayList<Object> array = new ArrayList<Object>();
        tinydb.getListObject("TranDataData", TranDataData.class);
        for(Object item: array) {
            arrayListData.add((TranDataData) item);
            // need to update all the various amounts and the like based on possible changes.

        }
        return arrayListData;
    }

    // is this handling all types of exits?
    public void onBackPressed() {
        if (intentIn != null) {

            if (userData != null)
                intentIn.putExtra(getString(R.string.userFile), userData);
//            if (tranData != null) {
//                intentIn.putExtra(getString(R.string.tranFile), tranData);
//            }

            saveTransactions();
            setResult(RESULT_OK, intentIn);
        }
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
        // Showing Alert Message
        // it blows here.
        alertDialog.show();
    }
    private String getItem(int in) {
        EditText item = (EditText) findViewById(in);
        return  item.getText().toString();
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
    private void updateScreen() {
        if (commResult != null) {
            if (commResult.getStage() >= 3) {
                setItem(R.id.commResult,commResult.getRespMessage());
                _sendBtn.setText(getString(R.string.tran_sent));
            } else {
                setItem(R.id.commResult,getString(R.string.error));
                _sendBtn.setText(getString(R.string.tran_failed_retry));
            }
        } else {
            setItem(R.id.commResult, getString(R.string.error));
            _sendBtn.setText(R.string.tran_failed);
        }
        setItem(R.id.storedTransNum,Integer.toString(tranData.getNumStoredTrans()));
        setItem(R.id.currentBalance, tranData.getLastAmt());
    }

    private static final int VIEW_ACTIVITY = 3;
    private void viewTransactions(/*View view*/) {
        // start activity that allows user to input user name, and password (twice with verification)
        if(userData == null) {
            userData = new UserData();
        }
        Intent intent = new Intent(this, ViewStoredTransactions.class);
        startActivityForResult(intent,VIEW_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // different data is shuffled back and forth depending on activity
        if (data != null) {
            switch (requestCode) {
                case VIEW_ACTIVITY:
                    // restore the transactions, in case some were deleted
                    ArrayList<TranDataData> array = readTransactions();
                    tranData.remakeStoredTrans(array);
                    updateScreen();
                    break;
            }

        }
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
                        if(retval != null)
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
            if(value == 1) {
                max = progressMark + 2;
            }
            else
            {
                //todo determine why this doesn't seem to be working
                percentage = (100/max) * (max - progressMark);
                progressMark--;
            }
            progress.incrementProgressBy(percentage);
        }
        @Override
        protected void onPreExecute(){
            initiateProgress();
        }
        @Override
        protected void onPostExecute(CommResult commResult) {
            updateScreen();
            progress.dismiss();
            // need to update the data that's serialized?
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
                return(updateOutput(output));
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

        private CommResult SendTran(CommResult output)  {
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