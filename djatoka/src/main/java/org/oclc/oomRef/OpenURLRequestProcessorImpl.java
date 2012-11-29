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
package org.oclc.oomRef;

import info.openurl.oom.ContextObject;
import info.openurl.oom.OpenURLException;
import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.OpenURLResponse;
import info.openurl.oom.Service;
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

import org.oclc.oomRef.descriptors.ByReferenceMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataKevImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataXmlImpl;
import org.oclc.oomRef.entities.ReferentImpl;
import org.oclc.oomRef.entities.ReferrerImpl;
import org.oclc.oomRef.entities.ReferringEntityImpl;
import org.oclc.oomRef.entities.RequesterImpl;
import org.oclc.oomRef.entities.ResolverImpl;
import org.oclc.oomRef.entities.ServiceTypeImpl;
import org.w3c.dom.Document;

/**
 * For the sake of simplicity, this resolver assumes that multiple
 * ServiceType Identifiers should be processed in sequence until one of
 * them returns a Response instead of null. For example, the ContextObject
 * could specify two services: 1) The desired service 2) A failover service

 * @author Jeffrey A. Young
 */
public class OpenURLRequestProcessorImpl implements OpenURLRequestProcessor {

    public URI getProcessorID() throws URISyntaxException {
        return new URI("info:oom/oomProcessors/OOMRef-J");
    }
    
	public OpenURLResponse resolve(OpenURLRequest openURLRequest)
	throws OpenURLException {
        OpenURLResponse response = null;
        
        // Try each ContentObject until someone responds
        ContextObject[] contextObjects = openURLRequest.getContextObjects();
        for (int i=0; response == null && i < contextObjects.length; ++i) {
            ContextObject contextObject = contextObjects[i];
            ServiceType[] serviceTypes = contextObject.getServiceTypes();

            // Try each ServiceType/Service until someone responds
            for (int j=0; response == null && j<serviceTypes.length; ++j) {
            	ServiceType serviceType = serviceTypes[j];
                Object[] descriptors = serviceType.getDescriptors();
                
                // Try each Service until someone responds
                for (int k=0; response == null && k < descriptors.length; ++k) {
                	Object descriptor = descriptors[k];
                    if (descriptor instanceof Service) {
                    	Service service = (Service) descriptor;
                    	try {
                            response = service.resolve(
//                            		this,
                            		serviceType,
                            		contextObject,
                                    openURLRequest,
                                    this);
                    	} catch (Exception e) {
                    		throw new OpenURLException(e.getMessage(), e);
                    	}
                    }
                }
            }
        }
        return response;
	}

	public ContextObject contextObjectFactory(Referent referent,
			ReferringEntity referringEntity, Requester requester,
			ServiceType serviceType, Resolver resolver, Referrer referrer) {
		ReferringEntity[] referringEntities = null;
		Requester[] requesters = null;
		ServiceType[] serviceTypes = null;
		Resolver[] resolvers = null;
		Referrer[] referrers = null;

		if (referringEntity != null)
			referringEntities = new ReferringEntity[] { referringEntity };
		if (requester != null)
			requesters = new Requester[] { requester };
		if (serviceType != null)
			serviceTypes = new ServiceType[] { serviceType };
		if (resolver != null)
			resolvers = new Resolver[] { resolver };
		if (referrer != null)
			referrers = new Referrer[] { referrer };
			
		
		return contextObjectFactory(referent,
				referringEntities,
				requesters,
				serviceTypes,
				resolvers,
				referrers);
	}
	
	public ContextObject contextObjectFactory(Referent referent,
			ReferringEntity[] referringEntities,
			Requester[] requesters,
			ServiceType[] serviceTypes,
			Resolver[] resolvers,
			Referrer[] referrers) {
		return new ContextObjectImpl(
				referent,
				referringEntities,
				requesters,
				serviceTypes,
				resolvers,
				referrers);
	}
    
	public Referent referentFactory(Object object) {
		return new ReferentImpl(object);
	}

	public ServiceType serviceTypeFactory(Object object) {
		return new ServiceTypeImpl(object);
	}
	
	public Requester requesterFactory(Object object) {
		return new RequesterImpl(object);
	}
	
	public ReferringEntity referringEntityFactory(Object object) {
		return new ReferringEntityImpl(object);
	}
	
	public Referrer referrerFactory(Object object) {
		return new ReferrerImpl(object);
	}
	
	public Resolver resolverFactory(Object object) {
		return new ResolverImpl(object);
	}

	public ByReferenceMetadata byReferenceMetadataFactory(URI ref_fmt, URL ref) {
		return new ByReferenceMetadataImpl(ref_fmt, ref);
	}

	public ByValueMetadata byValueMetadataFactory(URI val_fmt, String prefix, Set entrySet) {
		return new ByValueMetadataImpl(val_fmt, prefix, entrySet);
	}

    public ByValueMetadataKev byValueMetadataKevFactory(URI val_fmt, String prefix, Set entrySet) {
        return new ByValueMetadataKevImpl(val_fmt, prefix, entrySet);
    }

    public ByValueMetadataXml byValueMetadataXmlFactory(URI val_fmt, Document xmlDoc) {
        return new ByValueMetadataXmlImpl(val_fmt, xmlDoc);
    }

	public OpenURLRequest openURLRequestFactory(ContextObject contextObject) {
		return new OpenURLRequestImpl(contextObject);
	}

	public OpenURLResponse openURLResponseFactory(int status,
			String redirectURL,
			String contentType,
			byte[] bytes) {
		return new OpenURLResponse(status, redirectURL, contentType, bytes);
	}
}
