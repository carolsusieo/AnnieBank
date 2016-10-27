package com.example.carolsusieo.anniebank;


import java.util.ArrayList;

/**
 * Created by carolsusieo on 11/18/15.
 * data input, and required for a transaction
 */

class TranData
 {
    private String lastamt;
    private float lastamtfloat;
    private Boolean lastAmtSet = false;
     private float lastHostAmt;
    private ArrayList<TranDataData> arrayListData;

     TranData() {
         arrayListData = new ArrayList<>();

     }

     private void changeLastAmt(Float in) {
        if (lastAmtSet) {
            // if it wasn't set to begin with, don't do this.
            lastamtfloat -= in;
            setLastAmt(Float.toString(lastamtfloat));
        }
    }

    public String getLastAmt() { return lastamt;}
     public Float getLastAmtFloat() { return lastamtfloat;}


    public void setLastAmt(String in) { lastamt = in;
        if(in!= null && !in.contentEquals("unknown"))
            lastAmtSet = true;
    }
     public void setLastAmt(Float in) {
         lastamtfloat = in;
         setLastAmt(Float.toString(lastamtfloat));
     }

    public ArrayList<TranDataData> getTranArrayData() { return arrayListData;}


    public int getNumStoredTrans() {return arrayListData.size();}

    public void storeTran(TransactionData transactionDataIn) {
        // it is adding a reference...   so when I add the next one, it becomes the same as the last. since they all point to the same thing
        TransactionData item = new TransactionData();
        item.putAmount(transactionDataIn.getAmount());
        item.putCurrency(transactionDataIn.getCurrency());
        item.putDesc(transactionDataIn.getDesc());
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
        arrayListData.remove(0);
    }

    // a remake of stored trans after we have deleted some is going to change the
     // amount
     public void holdLastAmt() {
         // this is the amount without the storedTrans;
         lastHostAmt = lastamtfloat + getValueOfStoredTransactions();
     }

    public void remakeStoredTrans(ArrayList<TranDataData> arrayIn) {
        arrayListData = arrayIn;
        setLastAmt(lastHostAmt - getValueOfStoredTransactions());
      }
}

