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
package info.openurl.oom.config;

import java.util.Map;

import javax.xml.transform.TransformerException;

/**
 * Configuration information for OOM Classes.
 * 
 * @author Jeffrey A. Young
 */
public interface ClassConfig {

    /**
     * The Java classname that instantiates the implied OOM Class
     * 
     * @return the fully-qualified Java class name
     * @throws TransformerException
     */
    public String getClassName() throws TransformerException;

    /**
     * Get a class configuration property from the OOM configuration file
     * for the specified key.
     * Only use this method if you know there is only one value for the
     * key.
     * 
     * @param key
     * @return the value for a key in the OOM configuration file
     * @throws TransformerException
     */
    public String getArg(String key) throws TransformerException;

    /**
     * Get an array of class configuration properties from the OOM configuration
     * file for the specified key.
     * @param key
     * @return an array of values assigned to this key in the OOM
     * configuration file.
     * @throws TransformerException
     */
    public String[] getArgs(String key) throws TransformerException;
    
    /**
     * Get a Map of the args in the OOM configuration file for this class
     * @return a Map of args
     * @throws TransformerException
     */
    public Map getArgs() throws TransformerException;
}
