//========================================================================
// Original Authors : Van den Broeke Iris, Deville Daniel, Dubois Roger, Greg Wilkins
// Revision Author : Ryan Chute
// Copyright (c) 2001 Deville Daniel. All rights reserved.
// Permission to use, copy, modify and distribute this software
// for non-commercial or commercial purposes and without fee is
// hereby granted provided that this copyright notice appears in
// all copies.
//========================================================================

package gov.lanl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to filter remote content by Apache .htaccess format
 * 
 * @author Ryan Chute Parts of code from HTAccessHandler in Jetty 6:
 *         http://www.mortbay.org/jetty/jetty-6/xref/org/mortbay/jetty/security/ HTAccessHandler.html
 */
public class AccessManager {

    private static Logger LOGGER = LoggerFactory.getLogger(AccessManager.class);

    private ArrayList<String> _allowList = new ArrayList<String>();

    private ArrayList<String> _denyList = new ArrayList<String>();

    int _order;

    /**
     * Creates an access manager from the supplied resource.
     * 
     * @param resource A supplied resource
     */
    public AccessManager(String resource) {
        this(new File(resource));
    }

    /**
     * Creates an access manager from the supplied resource file.
     * 
     * @param resource A supplied resource file
     */
    public AccessManager(File resource) {
        BufferedReader htin = null;

        try {
            htin = new BufferedReader(new InputStreamReader(new FileInputStream(resource)));
            parse(htin);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * Check access for the supplied host.
     * 
     * @param host The host
     * @return True if access is allowed; else, false
     */
    public boolean checkAccess(String host) {
        // Figure out if it's a host or ip
        boolean isIP = false;
        char a = host.charAt(0);
        if (a >= '0' && a <= '9') {
            isIP = true;
        }

        String elm;
        boolean alp = false;
        boolean dep = false;

        // if no allows and no deny defined, then return true
        if (_allowList.size() == 0 && _denyList.size() == 0) {
            return (true);
        }

        // looping for allows
        for (int i = 0; i < _allowList.size(); i++) {
            elm = _allowList.get(i);
            if (elm.equals("all")) {
                alp = true;
                break;
            } else {
                char c = elm.charAt(0);
                if (c >= '0' && c <= '9') {
                    // ip
                    if (isIP && host.startsWith(elm)) {
                        alp = true;
                        break;
                    }
                } else {
                    // hostname
                    if (!isIP && host.endsWith(elm)) {
                        alp = true;
                        break;
                    }
                }
            }
        }

        // looping for denies
        for (int i = 0; i < _denyList.size(); i++) {
            elm = _denyList.get(i);
            if (elm.equals("all")) {
                dep = true;
                break;
            } else {
                char c = elm.charAt(0);
                if (c >= '0' && c <= '9') { // ip
                    if (isIP && host.startsWith(elm)) {
                        dep = true;
                        break;
                    }
                } else { // hostname
                    if (!isIP && host.endsWith(elm)) {
                        dep = true;
                        break;
                    }
                }
            }
        }

        if (_order < 0) {
            return !dep || alp;
        }

        return alp && !dep;
    }

    /**
     * Return true if access is limited; else, false.
     * 
     * @return True if the access is limited; else, false
     */
    public boolean isAccessLimited() {
        if (_allowList.size() > 0 || _denyList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void parse(BufferedReader htin) throws IOException {
        String line;
        int limit = 0;
        while ((line = htin.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("order")) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("orderline=" + line + "order=" + _order);
                }
                if (line.indexOf("allow,deny") > 0) {
                    LOGGER.debug("==>allow+deny");
                    _order = 1;
                } else if (line.indexOf("deny,allow") > 0) {
                    LOGGER.debug("==>deny,allow");
                    _order = -1;
                }
            } else if (line.startsWith("allow from")) {
                int pos1 = 10;
                limit = line.length();
                while ((pos1 < limit) && (line.charAt(pos1) <= ' ')) {
                    pos1++;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("allow from:" + line.substring(pos1));
                }
                StringTokenizer tkns = new StringTokenizer(line.substring(pos1));
                while (tkns.hasMoreTokens()) {
                    _allowList.add(tkns.nextToken());
                }
            } else if (line.startsWith("deny from")) {
                int pos1 = 9;
                limit = line.length();
                while ((pos1 < limit) && (line.charAt(pos1) <= ' ')) {
                    pos1++;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("deny from:" + line.substring(pos1));
                }

                StringTokenizer tkns = new StringTokenizer(line.substring(pos1));
                while (tkns.hasMoreTokens()) {
                    _denyList.add(tkns.nextToken());
                }
            }
        }
    }

}