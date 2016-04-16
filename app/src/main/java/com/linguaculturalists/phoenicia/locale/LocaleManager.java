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
import java.io.IOException;
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
    public Map<String, String> scan(String locales_directory) throws IOException {
        final Map<String, String> locale_map = new HashMap<String, String>();

        String[] files = PhoeniciaContext.assetManager.list(locales_directory);
        for (String locale_dir: files) {
            if (locale_dir.equals("common")) continue;
            String locale_path = locales_directory+"/"+locale_dir+"/manifest.xml";
            LocaleHeaderScanner scanner = new LocaleHeaderScanner(locale_path, locale_map);
            try {
                InputStream locale_manifest_in = PhoeniciaContext.assetManager.open(locale_path);
                final SAXParserFactory spf = SAXParserFactory.newInstance();
                final SAXParser sp = spf.newSAXParser();
                final XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(scanner);
                xr.parse(new InputSource(new BufferedInputStream(locale_manifest_in)));
            } catch (Exception e) {
                Debug.e(e);
            }
        }
        return locale_map;
    }

    public class LocaleHeaderScanner extends DefaultHandler {

        private Map<String, String> locale_map;
        private String locale_path;
        public LocaleHeaderScanner(String locale_path, Map<String, String> map) {
            this.locale_map = map;
            this.locale_path = locale_path;
        }
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equals("locale")) {
                locale_map.put(locale_path, attributes.getValue("display_name"));
            }
        }
    };
}
