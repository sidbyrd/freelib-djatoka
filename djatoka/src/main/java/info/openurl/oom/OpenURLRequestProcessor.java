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
package info.openurl.oom;

import info.openurl.oom.descriptors.ByReferenceMetadata;
import info.openurl.oom.descriptors.ByValueMetadata;
import info.openurl.oom.descriptors.ByValueMetadataKev;
import info.openurl.oom.descriptors.ByValueMetadataXml;
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.w3c.dom.Document;

/**
 * This abstraction processes web service request(s) represented
 * by the OpenURL model. The OpenURL 1.0 specification doesn't
 * dictate the mode of operation at this level, so implementations
 * should make instantiations of this interface a configuration
 * parameter.
 * 
 * @author Jeffrey A. Young
 */
public interface OpenURLRequestProcessor {
    
	/**
	 * Constructs a ContextObject container for the provided
	 * Entities.
	 * 
	 * @param referent descriptions of what the request refers to
	 * @param referringEntity descriptions of where the request
	 * was invoked
	 * @param requester descriptions of who invoked the request
	 * @param serviceType descriptions for why the requesters
	 * invoked the request
	 * @param resolver description of viable service providers
	 * @param referrer description of the entities that formulated
	 * the request
	 * @return a ContextObject containing the specified Entities
	 */
	public ContextObject contextObjectFactory(Referent referent,
			ReferringEntity referringEntity,
			Requester requester,
			ServiceType serviceType,
			Resolver resolver,
			Referrer referrer);
	
	/**
	 * Constructs a ContextObject container for the provided
	 * Entities.
	 * 
	 * @param referent descriptions of what the request refers to
	 * @param referringEntities descriptions of where the request
	 * was invoked
	 * @param requesters descriptions of who invoked the request
	 * @param serviceTypes descriptions for why the requesters
	 * invoked the request
	 * @param resolvers description of viable service providers
	 * @param referrers description of the entities that formulated
	 * the request
	 * @return a ContextObject containing the specified Entities
	 */
	public ContextObject contextObjectFactory(Referent referent,
			ReferringEntity[] referringEntities,
			Requester[] requesters,
			ServiceType[] serviceTypes,
			Resolver[] resolvers,
			Referrer[] referrers);

	/**
	 * This method takes a web service request that has been
	 * transformed into the OpenURL model, locates, and invokes
	 * the corresponding Service classes to produce a result.
	 *  
	 * @param openURLRequest the entire web service request
	 * represented in the OpenURL model.
	 * @return a single result that the Servlet can easily
	 * return to the client.
	 * @throws OpenURLException
	 */
	public OpenURLResponse resolve(OpenURLRequest openURLRequest)
	throws OpenURLException;

	/**
	 * This factory creates a Referent. Note that the descriptor
	 * parameter can be anything, including an array of objects
	 * 
	 * @param descriptors Object(s) describing the Referent
	 * @return a Referent containing the specified descriptor(s)
	 */
	public Referent referentFactory(Object descriptors);
	
	/**
	 * This factory creates a ServiceType. Note that the descriptor
	 * parameter can be anything, including an array of objects
	 * 
	 * @param descriptors Object(s) describing the ServiceType
	 * @return a ServiceType containing the specified descriptor(s)
	 */
	public ServiceType serviceTypeFactory(Object descriptors);

	/**
	 * This factory creates a Requester. Note that the descriptor
	 * parameter can be anything, including an array of objects
	 * 
	 * @param descriptors Object(s) describing the Requester
	 * @return a Requester containing the specified descriptor(s)
	 */
	public Requester requesterFactory(Object descriptors);
	
	/**
	 * This factory creates a ReferringEntity. Note that the descriptor
	 * parameter can be anything, including an array of objects
	 * 
	 * @param descriptors Object(s) describing the ReferringEntity
	 * @return a ReferringEntity containing the specified descriptor(s)
	 */
	public ReferringEntity referringEntityFactory(Object descriptors);
	
	/**
	 * This factory creates a Referrer. Note that the descriptor
	 * parameter can be anything, including an array of objects
	 * 
	 * @param descriptors Object(s) describing the Referrer
	 * @return a Referrer containing the specified descriptor(s)
	 */
	public Referrer referrerFactory(Object descriptors);
	
	/**
	 * This factory creates a Resolver. Note that the descriptor
	 * parameter can be anything, including an array of objects
	 * 
	 * @param descriptors Object(s) describing the Resolver
	 * @return a Resolver containing the specified descriptor(s)
	 */
	public Resolver resolverFactory(Object descriptors);
	
	/**
	 * Obtain a metadata descriptor. Generally speaking, any
	 * Java Object can be a descriptor, but metadata is
	 * generally useful and doesn't have a native Java
	 * representation. OTOH, we don't want people who need metadata
	 * descriptors to be bound to specific OOM implementations, so
	 * this factory allows Transports to get an implementation-
	 * independent instance.
	 * 
	 * @param ref_fmt
	 * @param ref
	 * @return a reference to a metadata description
	 */
	public ByReferenceMetadata byReferenceMetadataFactory(URI ref_fmt,
			URL ref);

	/**
	 * Obtain a metadata descriptor. Generally speaking, any
	 * Java Object can be a descriptor, but metadata is
	 * generally useful and doesn't have a native Java
	 * representation. OTOH, we don't want people who need metadata
	 * descriptors to be bound to specific OOM implementations, so
	 * this factory allows Transports to get an implementation-
	 * independent instance.
	 * 
	 * @param val_fmt
	 * @param prefix
	 * @param entrySet
	 * @return a metadata description
	 */
	public ByValueMetadataKev byValueMetadataKevFactory(URI val_fmt,
			String prefix, Set entrySet);

    /**
     * Obtain a metadata descriptor. Generally speaking, any
     * Java Object can be a descriptor, but metadata is
     * generally useful and doesn't have a native Java
     * representation. OTOH, we don't want people who need metadata
     * descriptors to be bound to specific OOM implementations, so
     * this factory allows Transports to get an implementation-
     * independent instance.
     * 
     * @param val_fmt
     * @param prefix
     * @param entrySet
     * @return a metadata description
     * @deprecated Use byValueMetadataKevFactory instead.
     */
    public ByValueMetadata byValueMetadataFactory(URI val_fmt,
            String prefix, Set entrySet);

    /**
     * Obtain a metadata descriptor. Generally speaking, any
     * Java Object can be a descriptor, but metadata is
     * generally useful and doesn't have a native Java
     * representation. OTOH, we don't want people who need metadata
     * descriptors to be bound to specific OOM implementations, so
     * this factory allows Transports to get an implementation-
     * independent instance.
     * 
     * @param val_fmt
     * @param xmlDoc The XML document
     * @return a metadata description
     */
    public ByValueMetadataXml byValueMetadataXmlFactory(URI val_fmt,
            Document xmlDoc);

	/**
	 * The OpenURL specification doesn't provide any clues
	 * for how to interpret an OpenURLRequest. For example,
	 * what should happen if there are multiple ServiceTypes?
	 * It is easy to imagine different implementations
	 * of this interface being created to suit different
	 * interpretations. To manage these variations, each
	 * interpretation should be represented by a unique
	 * identifier and returned through this method. Perhaps 
	 * we could add this category to the OpenURL registry someday.
	 * 
	 * @return the processor identifier
	 * @throws URISyntaxException 
	 */
	public URI getProcessorID() throws URISyntaxException;

	/**
	 * Get an OpenURLRequest object containing essential
	 * ingredients from the HTTP request represented in
	 * terms of the OpenURL Object Model (OOM).
	 * 
	 * @param contextObject
	 * @return an OpenURLRequest container for the
	 * specified request components.
	 */
	public OpenURLRequest openURLRequestFactory(ContextObject contextObject);

	/**
	 * Get an OpenURLResponse object containing proxy
	 * information destined for the HttpServletResponse.
	 * 
	 * @param status
	 * @param redirectURL
	 * @param contentType
	 * @param bytes
	 * @return an OpenURLResponse object
     * @deprecated Create a new info.openurl.oom.OpenURLResponse object directly instead.
	 */
	public OpenURLResponse openURLResponseFactory(int status,
			String redirectURL,
			String contentType,
			byte[] bytes);
}