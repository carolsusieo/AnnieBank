package com.example.carolsusieo.anniebank;

import java.io.Serializable;

/**
 * Created by carolsusieo on 11/18/15.
 */
public class UserData implements Serializable {
    private String username;
    private String password;
    private boolean loggedIn;
    private String website1;
    private String website2;
    private boolean useWeb1;

    public String getWebsite1() { return website1;}
    public String getWebsite2() { return website2;}
    public void setWebsite1(String in) { website1 = in;}
    public void setWebsite2(String in) {website2 = in; }
    public void setUseWeb1(boolean in) {useWeb1 = in; }
    public boolean useWeb1() { return useWeb1;}
    public String getUsername() { return this.username;}
    public String getPassword() { return this.password;}
    public boolean getLoggedIn() { return this.loggedIn;}
    public boolean setUsername(String in) {
        if (in.length() > 0) {
            username = in;
            return true;
        } else {
            return false;
        }
    }
    public boolean setPassword(String in, String other)
    {
        if(in.contentEquals(other)){
            password = in;
            return true;
        }
        else
            return false;
    }
    public void setLoggedIn(boolean in) {
        loggedIn = in;
    }
}
