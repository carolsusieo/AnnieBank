package com.example.carolsusieo.anniebank;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by carolsusieo on 11/20/15.
 * the host communication process.
 */
class HostComm {

    private String RM;
    private int RC;
    private InputStream inputStream;
    private OutputStream outputStream;

    private void putRC(int in) {RC = in;}
    private void putRM(String in) {RM = in;}
    public String getRM() {return RM;}
    public int getRC() {return RC;}

    private void putInputStream(InputStream in) { inputStream = in; }
    public InputStream getInputStream() {return inputStream;}
    private void putOutputStream(OutputStream in) { outputStream = in;}

    //Context context;
    private final Resources resources;
    public HostComm(Context contextIn)
    {
        CookieManager myCookies;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            myCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(myCookies);
        }
        // todo cookies with older versions.
        //context = contextIn;
        resources = contextIn.getResources();
    }
    public URL CreateURL(UserData userData, String label, String label2) {
        String webString;
        String website;
        if(userData.useWeb1())
            website = userData.getWebsite1();
        else
            website = userData.getWebsite2();
        webString = label + website + label2;

        URL url = null;
        try {
            url = new URL(webString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public void RequestCSRFToken(URL url)
    {
        try {

            HttpURLConnection conn = startCommunication(url);
            if(conn !=null) {
                conn.setDoOutput(false); // this is a get
                //?  used to not do the conn.connect() in endComm
                //endCommunication(conn);
                putRM(conn.getResponseMessage());
                putRC(conn.getResponseCode());
                // todo why am I doing this, and then not doing this in other places?
                conn.disconnect();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    private String createLoginData(UserData userData) {
        String logString = null;
        if(userData != null)
            logString = resources.getString(R.string.xmlUsername) + userData.getUsername() + resources.getString(R.string.xmlSeparator) + resources.getString(R.string.xmlPassword) + userData.getPassword() + resources.getString(R.string.xmlSeparator) + resources.getString(R.string.xmlLoginFrm);

        return logString;
    }

    public int Login(URL url, UserData userData) {
        String logString = createLoginData(userData);
        if (logString != null) {
            try {
                HttpURLConnection conn = startCommunication(url);
                if(conn != null) {
                    conn.setDoOutput(true); // this is a put
                    conn.setRequestProperty(resources.getString(R.string.htmlContent), resources.getString(R.string.htmlApp));
                    conn.setRequestMethod(resources.getString(R.string.htmlPost));
                    putOutputStream(conn.getOutputStream());
                    outputStream.write(logString.getBytes());
                    // todo why not using endcommunication -- not using conn.connect
                    putRM(conn.getResponseMessage());
                    putRC(conn.getResponseCode());
                    // todo why disconnect here but not other places?
                    conn.disconnect();
                }
             } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else {
            putRC(0);
        }
        return getRC();
    }

    private HttpURLConnection startCommunication(URL url)
    {
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setDoInput(true);
            return conn;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
    private void endCommunication(HttpURLConnection conn)
    {
        try {
            conn.connect();
            putRM(conn.getResponseMessage());
            putRC(conn.getResponseCode());
            putInputStream(conn.getInputStream());
        }
        catch(Throwable t){
            t.printStackTrace();
        }
    }
    // get used for balance inquiry
    public HttpURLConnection getCommunication(URL url) {
        try {
            HttpURLConnection conn = startCommunication(url);
            if(conn != null) {
                conn.setRequestMethod(resources.getString(R.string.htmlGet));
                conn.setDoOutput(false); // this is a get
                conn.setRequestProperty(resources.getString(R.string.htmlAccept), resources.getString(R.string.htmlXml));
                endCommunication(conn);
                return conn;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
    // put used for transaction update
    public HttpURLConnection putCommunication(URL url,String value) {
        try{
            HttpURLConnection conn = startCommunication(url);
            if(conn != null) {
                conn.setRequestMethod(resources.getString(R.string.htmlPut));
                conn.setDoOutput(true); // this is a put
                conn.setRequestProperty(resources.getString(R.string.htmlContent), resources.getString(R.string.htmlApp));
                putOutputStream(conn.getOutputStream());
                outputStream.write(value.getBytes());
                endCommunication(conn);
                return conn;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = connectivity.getAllNetworks();
                NetworkInfo networkInfo;
                for (Network mNetwork : networks) {
                    networkInfo = connectivity.getNetworkInfo(mNetwork);
                    if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                        return true;
                    }
                }
            }else {
                    //noinspection deprecation
                    NetworkInfo[] info = connectivity.getAllNetworkInfo();
                    if (info != null)
                    {
                        for (NetworkInfo anInfo : info)
                            if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                                return true;
                            }
                    }


            }

         }
        return false;
    }
    // check internet connection
    public boolean checkConnection(Context context)
    {
        return isNetworkAvailable(context);
    }
}
