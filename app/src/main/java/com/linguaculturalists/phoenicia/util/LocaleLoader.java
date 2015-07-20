package com.linguaculturalists.phoenicia.util;

import com.linguaculturalists.phoenicia.Locale;
import com.linguaculturalists.phoenicia.util.LocaleParser;

import org.andengine.util.debug.Debug;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by mhall on 7/17/15.
 */
public class LocaleLoader {

    public LocaleLoader() {
        super();
    }

    public Locale load(InputStream locale_manifest_in) {
        LocaleParser localeParser = new LocaleParser();
        try {
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            final SAXParser sp = spf.newSAXParser();
            final XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(localeParser);
            xr.parse(new InputSource(new BufferedInputStream(locale_manifest_in)));
        } catch (Exception e) {
            Debug.e(e);
        }
        return localeParser.getLocale();
    }

}
