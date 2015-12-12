package com.example.carolsusieo.anniebank;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by carolsusieo on 11/19/15.
 */
public class CarolOdiorneXmlParser {
    // We don't use namespaces
    private final String ns = null;

    //    public List parse(InputStream in) throws XmlPullParserException, IOException {
    public String parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            // some issue here....
            parser.setInput(in, null);
            parser.nextTag();
            // this is an assumption that the only thing returned from the host will be result
            return readResult(parser);
        } finally {
            in.close();
        }
    }
/*
   private String readResult(XmlPullParser parser) throws IOException, XmlPullParserException {

       // it's possible that there might be tag inside of result ... data tag...  or something like that.
       String result = null;
       parser.require(XmlPullParser.START_TAG, ns, "result");
       // parse.next CONSUMES the data off the string...
       if(parser.next() != XmlPullParser.TEXT) {
           //if(parser.next() == XmlPullParser.START_TAG);
                // skip past it

           while (parser.next() != XmlPullParser.END_TAG) {

               if (parser.getEventType() != XmlPullParser.START_TAG ) {
                   // if any start tags in the result, just skip through them (such as data)
                   continue;
               }
               if ( parser.getEventType() == XmlPullParser.TEXT ) {
                   String name = parser.getName();
                   // Starts by looking for the entry tag
                   if (name.equals("result")) {
                       readText(parser);
                   } else if (name.equals("data")) {
                       continue;
                   } else {
                       skip(parser);
                   }
               }
           }
       }
       else
            result = readText(parser);
        return result;
    }

*/
    private String readResult(XmlPullParser myParser) throws IOException, XmlPullParserException {

        int event;
        String text = null;
        String result = null;

        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("result")) {
                            result = new String(text);
                         } else if (name.equals("data")) {
                            result = new String(text);
                        } else {
                        }
                        break;
                }
                event = myParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
   /*
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.getEventType() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        else
        {
            // maybe it's the data tag BEFORE the actual data - within the result?

        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
    */
}
