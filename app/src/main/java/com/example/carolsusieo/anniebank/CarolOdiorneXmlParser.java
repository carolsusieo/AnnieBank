package com.example.carolsusieo.anniebank;

import android.content.res.Resources;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by carolsusieo on 11/19/15.
 * communications with the online application... specific to that communication
 *
 */
class CarolOdiorneXmlParser {
     //    public List parse(InputStream in) throws XmlPullParserException, IOException {
    public String parse(InputStream in, Resources resources) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            int eventType = parser.getEventType();
            if(eventType != XmlPullParser.END_DOCUMENT) {
                 return readResult(parser,resources);
            }
            else
                return resources.getString(R.string.ok);
        } finally {
            in.close();
        }
    }
    // asummes there is only one value. and that is what will be returned, when it is found.
    private String readResult(XmlPullParser myParser,Resources resources) //throws IOException, XmlPullParserException {
    {

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
                        if(text != null) {
                            if (name.equals(resources.getString(R.string.xmlResult))) {
                                result = text;
                            } else if (name.equals(resources.getString(R.string.xmlData))) {
                                result = text;
                            }
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
}
