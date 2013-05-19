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
package org.oclc.oomRef.transports;

import info.openurl.oom.ContextObject;
import info.openurl.oom.OpenURLException;
import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.Service;
import info.openurl.oom.Transport;
import info.openurl.oom.config.ClassConfig;
import info.openurl.oom.config.OpenURLConfig;
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.oclc.oomRef.descriptors.ByReferenceMetadataImpl;
import org.oclc.oomRef.descriptors.ByValueMetadataImpl;

/**
 * @author Jeffrey A. Young
 *
 * This class transforms HTTP requests into OpenURL
 * ContextObjects. Override this class to change the request
 * pattern. Configure the Servlet to use your new class by
 * adding/changing the following property to the Servlet.props
 * file:
 * 
 * Servlet.transportClassname={packageName.className}
 */

public class HttpOpenURLInlineTransport implements Transport {
    private OpenURLConfig openURLConfig;
//    private ClassConfig classConfig;
    
	/**
	 * Construct an HTTP OpenURL Inline Transport object
	 * 
	 * @param openURLConfig
	 * @param classConfig
	 */
	public HttpOpenURLInlineTransport(OpenURLConfig openURLConfig,
            ClassConfig classConfig) {
        this.openURLConfig = openURLConfig;
//        this.classConfig = classConfig;
	}

	public OpenURLRequest toOpenURLRequest(
			OpenURLRequestProcessor processor,
			HttpServletRequest req) 
	throws OpenURLException {
		try {
	        String url_ver = null;
	        
	        // url_ver=Z39.88-2004 is the only acceptable value
	        String[] url_vers = req.getParameterValues("url_ver");
	        
	        if (url_vers != null) {
	            for (int i=0; url_ver == null && i<url_vers.length; ++i) {
	            	if ("Z39.88-2004".equals(url_vers[i])) {
	            		url_ver = url_vers[i];
	            	}
	            }
	        }
	        
	        /* url_ctx_fmt=null or info:ofi/fmt:kev:mtx:ctx are
	         * the only acceptable values
	         */
	        
	        String url_ctx_fmt = "info:ofi/fmt:kev:mtx:ctx";
	        String[] url_ctx_fmts = req.getParameterValues("url_ctx_fmt");
	        if (url_ctx_fmts != null) {
	            for (int i=0; "info:ofi/fmt:kev:mtx:ctx".equals(url_ctx_fmt); ++i) {
	            	if (url_ctx_fmts[i].length() > 0
	            			&& !url_ctx_fmts[i].equals(url_ctx_fmt)) {
	            		url_ctx_fmt = url_ctx_fmts[i];
	            	}
	            }
	        }
	        
	        if (!("Z39.88-2004".equals(url_ver)
	        		&& url_ctx_fmt.equals("info:ofi/fmt:kev:mtx:ctx"))) {
	        	// sorry, this isn't our type of request
	        	return null;
	        }

	        Set entrySet = req.getParameterMap().entrySet();
	        Iterator iter = entrySet.iterator();
	        
	    	ArrayList referentDescriptors = new ArrayList();
	    	ArrayList requesterDescriptors = new ArrayList();
	    	ArrayList referringEntityDescriptors = new ArrayList();
	    	ArrayList referrerDescriptors = new ArrayList();
	    	ArrayList resolverDescriptors = new ArrayList();
	    	ArrayList serviceTypeDescriptors = new ArrayList();
	    	HashMap openURLKeys = new HashMap();
	    	HashMap adminKeys = new HashMap();
	    	HashMap foreignKeys = new HashMap();
	    	
	        while (iter.hasNext()) {
	        	Map.Entry entry = (Entry) iter.next();
	        	String key = (String) entry.getKey();
	        	String[] values = (String[]) entry.getValue();
	        	
	        	if (key.startsWith("url_")) {
	        		openURLKeys.put(key, values);
	        	} else if (key.startsWith("ctx_")) {
	        		adminKeys.put(key, values);
	        	} else if ("rft_id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
	            	    values[i] = URLEncoder.encode(values[i], "UTF-8");
		    	    referentDescriptors.add(new URI(values[i]));
	            	}
	    		} else if ("rft_dat".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referentDescriptors.add(values[i]);
	            	}
	    		} else if ("rft_val_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    			ByValueMetadataImpl bvm =
		    				new ByValueMetadataImpl(new URI(values[i]),
		    						"rft.", entrySet);
		    			referentDescriptors.add(bvm);
	            	}
	    		} else if ("rft_ref_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        String[] rft_refs = req.getParameterValues("rft_ref");
		    	        for (int j=0; j<rft_refs.length; ++j) {
		        			ByReferenceMetadataImpl brm =
		        				new ByReferenceMetadataImpl(
		        						new URI(values[i]), new URL(rft_refs[j]));
		        			referentDescriptors.add(brm);
		    	        }
	            	}
	    		} else if ("req_id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        requesterDescriptors.add(new URI(values[i]));
	            	}
	    		} else if ("req_dat".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        requesterDescriptors.add(values[i]);
	            	}
	    		} else if ("req_val_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    			ByValueMetadataImpl bvm =
		    				new ByValueMetadataImpl(new URI(values[i]),
		    						"req.", entrySet);
		    			requesterDescriptors.add(bvm);
	            	}
	    		} else if ("req_ref_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        String[] req_refs = req.getParameterValues("req_ref");
		    	        for (int j=0; j<req_refs.length; ++j) {
		        			ByReferenceMetadataImpl brm =
		        				new ByReferenceMetadataImpl(
		        						new URI(values[i]), new URL(req_refs[j]));
		        			requesterDescriptors.add(brm);
		    	        }
	            	}
	    		} else if ("rfe_id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referringEntityDescriptors.add(new URI(values[i]));
	            	}
	    		} else if ("rfe_dat".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referringEntityDescriptors.add(values[i]);
	            	}
	    		} else if ("rfe_val_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    			ByValueMetadataImpl bvm =
		    				new ByValueMetadataImpl(new URI(values[i]),
		    						"rfe.", entrySet);
		    			referringEntityDescriptors.add(bvm);
	            	}
	    		} else if ("rfe_ref_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        String[] rfe_refs = req.getParameterValues("rfe_ref");
		    	        for (int j=0; j<rfe_refs.length; ++j) {
		        			ByReferenceMetadataImpl brm =
		        				new ByReferenceMetadataImpl(
		        						new URI(values[i]), new URL(rfe_refs[j]));
		        			referringEntityDescriptors.add(brm);
		    	        }
	            	}
	    		} else if ("rfr_id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referrerDescriptors.add(new URI(values[i]));
	            	}
	    		} else if ("rfr_dat".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        referrerDescriptors.add(values[i]);
	            	}
	    		} else if ("rfr_val_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    			ByValueMetadataImpl bvm =
		    				new ByValueMetadataImpl(new URI(values[i]),
		    						"rfr.", entrySet);
		    			referrerDescriptors.add(bvm);
	            	}
	    		} else if ("rfr_ref_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        String[] rfr_refs = req.getParameterValues("rfr_ref");
		    	        for (int j=0; j<rfr_refs.length; ++j) {
		        			ByReferenceMetadataImpl brm =
		        				new ByReferenceMetadataImpl(
		        						new URI(values[i]), new URL(rfr_refs[j]));
		        			referrerDescriptors.add(brm);
		    	        }
	            	}
	    		} else if ("res_id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        resolverDescriptors.add(new URI(values[i]));
	            	}
	    		} else if ("res_dat".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        resolverDescriptors.add(values[i]);
	            	}
	    		} else if ("res_val_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    			ByValueMetadataImpl bvm =
		    				new ByValueMetadataImpl(new URI(values[i]),
		    						"res.", entrySet);
		    			resolverDescriptors.add(bvm);
	            	}
	    		} else if ("res_ref_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        String[] res_refs = req.getParameterValues("res_ref");
		    	        for (int j=0; j<res_refs.length; ++j) {
		        			ByReferenceMetadataImpl brm =
		        				new ByReferenceMetadataImpl(
		        						new URI(values[i]), new URL(res_refs[j]));
		        			resolverDescriptors.add(brm);
		    	        }
	            	}
	    		} else if ("svc_id".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
	            		URI uri = new URI(values[i]);
	                    
		    	        serviceTypeDescriptors.add(uri);
	                    
	                    // Throw in the corresponding Java class while we're here
		    	        Service service = (Service) openURLConfig.getService(uri);
		    	        serviceTypeDescriptors.add(service);
	            	}
	    		} else if ("svc_dat".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        serviceTypeDescriptors.add(values[i]);
	            	}
	    		} else if ("svc_val_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    			ByValueMetadataImpl bvm =
		    				new ByValueMetadataImpl(new URI(values[i]),
		    						"svc.", entrySet);
		    			serviceTypeDescriptors.add(bvm);
	            	}
	    		} else if ("svc_ref_fmt".equals(key)) {
	            	for (int i=0; i<values.length; ++i) {
		    	        String[] svc_refs = req.getParameterValues("svc_ref");
		    	        for (int j=0; j<svc_refs.length; ++j) {
		        			ByReferenceMetadataImpl brm =
		        				new ByReferenceMetadataImpl(
		        						new URI(values[i]), new URL(svc_refs[j]));
		        			serviceTypeDescriptors.add(brm);
		    	        }
	            	}
	    		} else if (key.startsWith("rft.")
	    				|| key.startsWith("rfe.")
	    				|| key.startsWith("req.")
	    				|| key.startsWith("rfr.")
	    				|| key.startsWith("res.")
	    				|| key.startsWith("svc.")
	    				) {
	    			// do nothing
	        	} else {
	        		foreignKeys.put(key, values);
	        	}
	        }
	        
	    	Referent referent =
	    		processor.referentFactory(referentDescriptors.toArray());
	    	Requester requester =
	    		processor.requesterFactory(requesterDescriptors.toArray());
	    	ReferringEntity referringEntity =
	    		processor.referringEntityFactory(referringEntityDescriptors.toArray());
	    	Referrer referrer =
	    		processor.referrerFactory(referrerDescriptors.toArray());
	    	Resolver resolver =
	    		processor.resolverFactory(resolverDescriptors.toArray());
	    	ServiceType serviceType =
	    		processor.serviceTypeFactory(serviceTypeDescriptors.toArray());
	    	
	        // Construct the ContextObject
	        ContextObject contextObject = processor.contextObjectFactory(
                    referent,
	                new ReferringEntity[] { referringEntity },
	                new Requester[] { requester },
	                new ServiceType[] { serviceType },
	                new Resolver[] { resolver },
	                new Referrer[] { referrer });
	        return processor.openURLRequestFactory(contextObject);
		} catch (Exception e) {
			throw new OpenURLException(e.getMessage(), e);
		}
	}

    public URI getTransportID() throws URISyntaxException {
        return new URI("info:ofi/tsp:http:openurl-inline");
    }
}
