/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.openurl.oom.util;

import info.openurl.oom.entities.Entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * General-purpose static utility methods.
 * 
 * @author Jeffrey A. Young
 */
public class OOMUtil {

    /**
     * @param queryString
     * @return a SortedMap of the parameters
     * @throws UnsupportedEncodingException
     */
    public static SortedMap getParameterMap(String queryString)
    throws UnsupportedEncodingException {
        String[] queryStrings;
        if (queryString == null) {
            queryStrings = new String[0];
        } else {
            queryStrings = new String[] { queryString };
        }
        return getParameterMap(queryStrings);
    }
    
    /**
     * @param parameterMap
     * @param key
     * @return a parameter value
     */
    public static Object getSingleParameterValue(Map parameterMap, Object key) {
        Object parameterValue = null;
        
        Object[] values = (Object[]) parameterMap.get(key);
        if (values != null && values.length > 0) {
            parameterValue = values[0];
        }
        
        return parameterValue;
    }
	/**
	 * Transforms a queryString into a Map of String/Object[]
	 * 
	 * @param queryStrings
	 * @return a Map of key/values from the queryString.
	 * @throws UnsupportedEncodingException 
	 */
	public static SortedMap getParameterMap(String[] queryStrings)
    throws UnsupportedEncodingException {
		HashMap tempMap = new HashMap();
		
        if (queryStrings != null) {
            for (int i=0; i<queryStrings.length; ++i) {
                String[] parameters = queryStrings[i].split("&");
                for (int j=0; j<parameters.length; ++j) {
                    String[] parameter = parameters[j].split("=", 2);
                    String key = URLDecoder.decode(parameter[0], "UTF-8");
                    String value = URLDecoder.decode(parameter[1], "UTF-8");
                    ArrayList list = (ArrayList) tempMap.get(key);
                    if (list == null) {
                        list = new ArrayList();
                        tempMap.put(key, list);
                    }
                    if (parameter.length == 2) {
                        list.add(value);
                    } else {
                        list.add("");
                    }
                }
            }
        }
		
		// Switch the values from ArrayList to String[]
		SortedMap parameterMap = new TreeMap();
		Iterator iter = tempMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String key = (String) entry.getKey();
			ArrayList value = (ArrayList) entry.getValue();
			parameterMap.put(key, value.toArray(new String[value.size()]));
		}
		return parameterMap;
	}

    /**
     * Get the bytes out of an InputStream.
     * @param is the stream to be read.
     * @return the bytes found in the stream.
     * @throws IOException
     */
    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) != -1) {
            baos.write(bytes, 0, len);
        }
        return baos.toByteArray();
    }
    
    /**
     * Get a single Descriptor of the specified time from the Entity.
     * @param entity
     * @param c
     * @return the first specified Descriptor
     */
    public static Object getSingleDescriptor(Entity entity, Class c) {
        if (entity != null) {
            Object[] objects = entity.getDescriptors(c);
            if (objects != null && objects.length > 0) {
                return objects[0];
            }
        }
        return null;
    }
}
