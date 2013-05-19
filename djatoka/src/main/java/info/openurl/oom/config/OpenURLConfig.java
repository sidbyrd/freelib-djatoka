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

import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.Service;
import info.openurl.oom.Transport;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.xml.transform.TransformerException;

/**
 * General configuration information for OOM.
 * 
 * @author Jeffrey A. Young
 */
public interface OpenURLConfig {

    /**
     * Get the ServletConfig (if available)
     * @return the ServletConfig
     */
    public ServletConfig getServletConfig();
    
    /**
     * Get a list of Transport classes defined in this CommunityProfile.
     * 
     * @return a list of Transports supported by the CommunityProfile
     * @throws TransformerException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Transport[] getTransports()
    throws TransformerException, ClassNotFoundException, SecurityException,
    NoSuchMethodException, IllegalArgumentException, InstantiationException,
    IllegalAccessException, InvocationTargetException;

    /**
     * Get an instance of an OpenURLRequestProcessor. Different processors may
     * interpret OpenURLRequests differently, so OOM uses the configuration
     * file to identify the one to be used by this application.
     * @return the configured OpenURLRequestProcessor
     * @throws TransformerException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public OpenURLRequestProcessor getProcessor()
    throws TransformerException, ClassNotFoundException, InstantiationException,
    IllegalAccessException;

    /**
     * Get an instance of a URI-identified service from the configuration file.
     *  
     * @param uri an identifier for a configured service
     * @return a Service.
     * @throws TransformerException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Service getService(URI uri)
    throws TransformerException, ClassNotFoundException, SecurityException,
    NoSuchMethodException, IllegalArgumentException, InstantiationException,
    IllegalAccessException, InvocationTargetException;

    /**
     * Get an instance of a Service class identified by a class name from the
     * configuration file. Use this method to construct Service classes
     * because it will automatically include any class-specific information
     * it finds in the configuration file.
     * 
     * @param className the name of a Service class
     * @return a Service.
     * @throws TransformerException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Service getService(String className)
    throws TransformerException, ClassNotFoundException, SecurityException,
    NoSuchMethodException, IllegalArgumentException, InstantiationException,
    IllegalAccessException, InvocationTargetException;

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
