package com.example.carolsusieo.anniebank;

//import java.io.Serializable;

/**
 * Created by carolsusieo on 11/23/15.
 * specific data wrapper for transaction data
 */
class TranDataData {
    protected String amount;
    protected String desc;
    protected String which;
}
public class TransactionData// implements Serializable {
{

    protected TranDataData data = new TranDataData();
    TranDataData getDataStruct () { return data;}
    void putDataStruct(TranDataData in) { data = in;}
    void putAmount(String inamount) {data.amount = inamount; }
    String getAmount() {return data.amount;}
    void putDesc(String indesc) {data.desc = indesc;}
    String getDesc() {return data.desc;}
    void putCurrency(String incurrency) { data.which = incurrency;}
    String getCurrency() { return data.which;}

}
