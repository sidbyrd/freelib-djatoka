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

import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

/**
 * ContextObject represents a single task to be performed.
 * <p />
 * Note that Transports have the option, if necessary, to 
 * create a sequence of ContextObjects needed to fulfill a
 * single HTTP request from the client.
 * 
 * @author Jeffrey A. Young
 */
public interface ContextObject {
	/**
	 * Get the Referent. Note that every ContextObject
	 * MUST contain one and only one Referent indicating
	 * the "what" portion of a ContextObject task.
	 * 
	 * @return the referent for this ContextObject
	 */
	public Referent getReferent();
	
	/**
	 * Get a sequence of ReferringEntities. There might be several,
	 * with each one indicating a different "where".
	 * 
	 * @return the ReferringEntities contained in this ContextObject
	 */
	public ReferringEntity[] getReferringEntities();

	/**
	 * Get a sequence of Requesters. There might be several,
	 * with each one indicating a different "who".
	 * 
	 * @return the Requesters contained in this ContextObject
	 */
	public Requester[] getRequesters();
	
	/**
	 * Get a sequence of ServiceTypes. There might be several,
	 * with each one indicating a different "why".
	 * 
	 * @return the ServiceTypes contained in this ContextObject
	 */
	public ServiceType[] getServiceTypes();
	
	/**
	 * Get a sequence of Resolvers. There might be several,
	 * with each one indicating a different server capable
	 * of processing the ContextObject.
	 * 
	 * @return the Resolvers contained in this ContextObject
	 */
	public Resolver[] getResolvers();
	
	/**
	 * Get a sequence of Referrers. There might be several,
	 * with each one indicating the entity responsible
	 * for formulating the ContextObject.
	 * 
	 * @return the Referrers contained in this ContextObject
	 */
	public Referrer[] getReferrers();
}
