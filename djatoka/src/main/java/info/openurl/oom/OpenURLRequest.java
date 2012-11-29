/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package info.openurl.oom;

import java.util.Map;

/**
 * The class contains information from that HTTP request
 * that has been transformed into the OpenURL Object Model.
 * 
 * @author Jeffrey A. Young
 */
public interface OpenURLRequest {
    /**
     * This Map contains key/value pairs defined by the OpenURL
     * spec as "OpenURL keys". OpenURL keys don't deserve the
     * special attention given them in the spec. They are artifacts
     * of the peculiar and unfriendly transports used in the spec
     * as examples:
     * openurl-by-ref, openurl-by-val, and openurl-inline.
     * 
     * @return a Map containing String key and String[] values
     * for OpenURL administrative keys.
     * @deprecated Proper Transports should consume these in
     * the process of generating ContextObjects and not defer
     * them to the Service classes.
     */
    public Map getOpenURLKeys();

    /**
     * This Map contains key/value pairs defined by the OpenURL
     * spec as "foreign keys". Foreign keys don't deserve the
     * special attention given them in the spec. They are artifacts
     * of the peculiar and unfriendly transports used in the spec
     * as examples:
     * openurl-by-ref, openurl-by-val, and openurl-inline.
     * 
     * @return a Map containing String key and String[] values
     * for OpenURL foreign keys.
     * @deprecated Proper Transports should consume these in
     * the process of generating ContextObjects and not defer
     * them to the Service classes.
     */
    public Map getForeignKeys();
    
    /**
     * ContextObjects represent, in the form of OpenURL Object Model
     * classes, the sequence of Services implied in a web service
     * request. For example, the Transport might generate a login
     * ContextObject followed by an edit ContextObject, if it sees
     * that the Requester hasn't logged in yet.
     * 
     * @return an array of ContextObjects
     */
    public ContextObject[] getContextObjects();
}
