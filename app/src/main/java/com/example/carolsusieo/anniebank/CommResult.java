package com.example.carolsusieo.anniebank;

/**
 * Created by carolsusieo on 11/6/15.
 * Deal with the communication process and what to display during and after a communication attempt
 */
// todo could this be a class in the HostComm class...?  so we don't just shuffle around data?
public class CommResult {
    //    private String token; // x-csrf-toxen
    private String content; // value from sheetnode
    private String message;
    private int code; // http response code
    private int stage; // initial, login, data

    public int getStage() {
        return stage;
    }
    public int getCode() {return code;}

    public String getRespMessage() {return message;}
    public String getContent() {
        return this.content;
    }

    public void putStage(int what) {
        stage = what;
    }
    public void putCode(int what) {code = what;}

    // response message. such as OK, or the like
    public void putMessage(String what)
    {
        message = what;
    }
    // content from host xml
    public void putContent(String what) {
        content = what;
    }
}
