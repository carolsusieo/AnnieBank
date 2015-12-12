package com.example.carolsusieo.anniebank;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.TextView;

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
 */
public class HostComm {

    private String RM;
    private int RC;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] response;

    public void putRC(int in) {RC = in;}
    public void putRM(String in) {RM = in;}
    public String getRM() {return RM;}
    public int getRC() {return RC;}
    public byte[] getResponse() {return response;}

    public void putInputStream(InputStream in) { inputStream = in; }
    public InputStream getInputStream() {return inputStream;}
    public void putOutputStream(OutputStream in) { outputStream = in;}
    public OutputStream getOutputStream() { return outputStream; }
    private CookieManager myCookies = null;// = new CookieManager();

    Context context;
    Resources resources;
    public HostComm(Context contextIn)
    {
          if(myCookies == null)
              myCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
          CookieHandler.setDefault(myCookies);
        context = contextIn;
        resources = contextIn.getResources();

    }

    public URL CreateURL(UserData userData, String label, String label2) {
        String webString;
        String website;
        if(userData.useWeb1())
            website = userData.getWebsite1();
        else
            website = userData.getWebsite2();
        webString = new StringBuilder(label).append(website).append(label2).toString();

        URL url = null;
        try {
            url = new URL(webString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public int RequestCSRFToken(URL url)
    {
        try {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(false); // this is a get
            conn.setDoInput(true);
            putRM(conn.getResponseMessage());
            putRC(conn.getResponseCode());
            conn.disconnect();
            return getRC();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;

    }
    String createLoginData(UserData userData) {
        String logString;
        if(userData != null)
            logString = new StringBuilder(resources.getString(R.string.xmlUsername))
                    .append(userData.getUsername())
                    .append("&").append(resources.getString(R.string.xmlPassword))
                            .append(userData.getPassword())
                    .append("&").append(resources.getString(R.string.xmlLoginFrm)).toString();

        else
            logString = new StringBuilder(resources.getString(R.string.xmlUsername)).append("&").append(resources.getString(R.string.xmlPassword)).toString();
        return logString;
    }


    public int Login(URL url, UserData userData)
    {
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true); // this is a put
            conn.setDoInput(true);
            conn.setRequestProperty(resources.getString(R.string.htmlContent), resources.getString(R.string.htmlApp));


            conn.setRequestMethod(resources.getString(R.string.htmlPost));
            putOutputStream(conn.getOutputStream());

            // if loginOnly is set, we need to get this information for the userData array
            String logString = createLoginData(userData);

            outputStream.write(logString.getBytes());

            putRM(conn.getResponseMessage());
            putRC(conn.getResponseCode());
/*
            inputStream = conn.getInputStream();
            response = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(response, 0, response.length)) != -1) ;
            inputStream.close();
*/
            conn.disconnect();
            return getRC();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

    public HttpURLConnection getCommunication(URL url) {
        try{

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod(resources.getString(R.string.htmlGet));
            conn.setDoInput(true);
            conn.setDoOutput(false); // this is a get
            conn.setRequestProperty(resources.getString(R.string.htmlAccept), resources.getString(R.string.htmlXml));

                    // Starts the query
                    conn.connect();

            putRM(conn.getResponseMessage());
            putRC(conn.getResponseCode());
            putInputStream(conn.getInputStream());
            return conn;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
    public HttpURLConnection putCommunication(URL url,String value) {
        try{

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setDoOutput(true); // this is a put
            conn.setDoInput(true);
            conn.setRequestProperty(resources.getString(R.string.htmlContent), resources.getString(R.string.htmlApp));
            conn.setRequestMethod(resources.getString(R.string.htmlPost));

            putOutputStream(conn.getOutputStream());

            // this should be the login string... username and password
            outputStream.write(value.getBytes());


            // Starts the query
            conn.connect();

            putRM(conn.getResponseMessage());
            putRC(conn.getResponseCode());
            putInputStream(conn.getInputStream());
            return conn;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }


    static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    Log.i("Class", info[i].getState().toString());
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
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
        boolean isConnectionExist = isNetworkAvailable(context);

        // check for Internet status
        if (isConnectionExist) {
            // Internet Connection exists
            return true;
        } else {
            return false;
        }

    }


}
