package com.example.carolsusieo.anniebank;

import android.content.SharedPreferences;
import android.widget.TextView;

//import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by carolsusieo on 11/18/15.
 * data input, and required for a transaction
 */

class TranData //implements Serializable
 {
    private String lastamt;
    private float lastamtfloat;
    private Boolean lastAmtSet = false;
    private ArrayList<TranDataData> arrayListData;

    private void changeLastAmt(Float in) {
        if (lastAmtSet) {
            // if it wasn't set to begin with, don't do this.
            lastamtfloat -= in;
            setLastAmt(Float.toString(lastamtfloat));
        }
    }

    public String getLastAmt() { return lastamt;}
    public void setLastAmt(String in) { lastamt = in;
        if(in!= null && !in.contentEquals("unknown"))
            lastAmtSet = true;
    }
    public void setLastAmt(Float in) {
        lastamtfloat = in;
        setLastAmt(Float.toString(lastamtfloat));
    }

    //private ArrayList<TransactionData> arrayList;
    //public ArrayList<TransactionData> getTranArray(){return arrayList;}

    public ArrayList<TranDataData> getTranArrayData() { return arrayListData;}

    TranData() {
//        arrayList = new ArrayList<>();
        arrayListData = new ArrayList<>();
    }

    public int getNumStoredTrans() {return arrayListData.size();}

    public void storeTran(TransactionData transactionDataIn) {
        // it is adding a reference...   so when I add the next one, it becomes the same as the last. since they all point to the same thing
        TransactionData item = new TransactionData();
        item.putAmount(transactionDataIn.getAmount());
        item.putCurrency(transactionDataIn.getCurrency());
        item.putDesc(transactionDataIn.getDesc());
 //       arrayList.add(item);
        arrayListData.add(item.getDataStruct());
        // update the amount...
        float f = Float.valueOf(item.getAmount());
        changeLastAmt(f);
    }

    public float getValueOfStoredTransactions() {
        float amt;
        amt = 0;
        int num = arrayListData.size();
        TransactionData transactionData = new TransactionData();
        for(int i = 0;i < num;i++) {
   //        transactionData = arrayList.get(i);
            transactionData.putDataStruct(arrayListData.get(i));
            float f = Float.valueOf(transactionData.getAmount().trim());
            amt += f;
        }
        return amt;
    }

    // FIFO
    TransactionData getStoredTrans() {
        //pull transactionData
        TransactionData returnTran = null;
        if(arrayListData.size() > 0) {
            returnTran = new TransactionData();
            returnTran.putDataStruct(arrayListData.get(0));
        }

        return returnTran;
    }
    // this assumes we just sent the FIFO stored transaction
    public void decStoredTrans(){
//        arrayList.remove(0);
        arrayListData.remove(0);
    }
    public void remakeStoredTrans(ArrayList<TranDataData> arrayIn) {
        arrayListData = arrayIn;
        setLastAmt(getValueOfStoredTransactions());
    }
}

