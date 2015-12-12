package com.example.carolsusieo.anniebank;

/**
 * Created by carolsusieo on 11/23/15.
 */
public class TransactionData {
    String amount;
    String desc;
    void putAmount(String inamount) {amount = inamount; }
    String getAmount() {return amount;}
    void putDesc(String indesc) {desc = indesc;}
    String getDesc() {return desc;}
}
