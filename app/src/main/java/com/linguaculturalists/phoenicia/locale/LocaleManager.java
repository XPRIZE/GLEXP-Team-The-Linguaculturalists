package com.linguaculturalists.phoenicia.locale;

import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.util.debug.Debug;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Loads the Locale instance for a GameSession.
 */
public class LocaleManager {

    public LocaleManager() {
        super();
    }

    /**
     * Identify available locales
     * @param locales_directory
     * @return
     */
    public Map<String, String> scan(File locales_directory) {
        final Map<String, String> locale_map = new HashMap<String, String>();
        DefaultHandler localeScanner = new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                super.startElement(uri, localName, qName, attributes);
                if (localName.equals("locale")) {
                    locale_map.put(uri, attributes.getValue("display_name"));
                }
            }
        };

        FilenameFilter manifestDirectoryFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File testDir = new File(dir, filename);
                File manifestFile = new File(testDir, "manifest.xml");
                return manifestFile.exists();
            }
        };

        for (File locale_dir: locales_directory.listFiles(manifestDirectoryFilter)) {
            File locale_file = new File(locale_dir, "manifest.xml");
            try {
                InputStream locale_manifest_in = PhoeniciaContext.assetManager.open(locale_file.getAbsolutePath());
                final SAXParserFactory spf = SAXParserFactory.newInstance();
                final SAXParser sp = spf.newSAXParser();
                final XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(localeScanner);
                xr.parse(new InputSource(new BufferedInputStream(locale_manifest_in)));
            } catch (Exception e) {
                Debug.e(e);
            }
        }
        return locale_map;
    }

}
