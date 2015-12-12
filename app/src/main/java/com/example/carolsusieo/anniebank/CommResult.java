package com.example.carolsusieo.anniebank;

/**
 * Created by carolsusieo on 11/6/15.
 */
public class CommResult {
//    private String token; // x-csrf-toxen
    private String id; //
    private String content; // value from sheetnode
    private String message;
    private String code_string;
    private int code; // http response code
    private int stage; // initial, login, data

    public int getStage() { return this.stage;}
    public int getCode() { return this.code;}
    public String getCodeString() { return this.code_string;}

//    public String getToken() {
//        return this.token;
//    }
//    public void putToken(String in) { token = in;}

    public String getId() {return this.id;}

    public String getContent() {
        return this.content;
    }

    public void put(String where, String what) {
        if(where.contains("Output"))
            content = what;
        else if(where.contains("Message"))
            message = what;
        else
            code_string = what;

    }

    public void put(String where, int what) {
        if (where.contains("Code"))
            code = what;
        else
            stage = what;
    }
}
