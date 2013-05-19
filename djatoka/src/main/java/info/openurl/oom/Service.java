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

import info.openurl.oom.entities.ServiceType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * If you think of a service in terms of pure business logic,
 * this class provides a simple bridge providing access to it
 * from the web. To use another analogy, if the business logic
 * is a coat, and OpenURL is a coatrack, this class represents
 * a loop of cloth sewn into the collar of the coat. 
 * <p />
 * Installing new Services can be as easy as dropping them on
 * the classpath, if your Transport is smart enough to deduce
 * the package and classname from information in the 
 * HttpServletRequest. If the Transport can't deduce everything
 * it needs from the HTTP request, it could use a config
 * file to locate and configure Service classes instead.
 * 
 * @author Jeffrey A. Young
 */
public interface Service {
    /**
     * This is a unique identifier assigned to a service.
     * It is easy to imagine a Service being used in
     * multiple CommunityProfiles, so it might be nice to
     * keep track of them in a registry someday. These
     * IDs could be used to represent the service in a
     * language independent way.
     * 
     * @return the Service identifier
     * @throws URISyntaxException
     */
    public URI getServiceID() throws URISyntaxException;

    /**
     * This method is responsible for pulling what (Referent),
     * why (ServiceType), who (Requester), etc. information
     * out of the ContextObject and using it to call any Java
     * classes and methods needed to produce a result. Having
     * obtained a result of some sort from the business logic,
     * this method is then responsible for transforming it
     * into an OpenURLResponse that acts as a proxy for
     * HttpServletResponse.
     * 
     * @param serviceType the current ServiceType in sequence as
     * enumerated in the ContextObject
     * @param contextObject the current ContextObject in sequence
     * as enumerated in the OpenURLRequest
     * @param openURLRequest the entire request from the client,
     * represented according to the OpenURL Object Model
     * @return null to have the ServiceType processing loop move
     * on to the next ServiceType or non-null to abort the
     * ServiceType processing loop and return a result.
     * @throws UnsupportedEncodingException
     * @throws OpenURLException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public OpenURLResponse resolve(
//            OpenURLRequestProcessor processor,
    		ServiceType serviceType,
    		ContextObject contextObject,
    		OpenURLRequest openURLRequest,
            OpenURLRequestProcessor openURLProcessor)
    throws UnsupportedEncodingException, OpenURLException, SAXException,
    IOException, ParserConfigurationException, TransformerException,
    SecurityException, IllegalArgumentException, ClassNotFoundException,
    NoSuchMethodException, InstantiationException, IllegalAccessException,
    InvocationTargetException;
}
