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
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.Referrer;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.Resolver;
import info.openurl.oom.entities.ServiceType;

class ContextObjectImpl implements ContextObject {
	private Referent referent;
	private ReferringEntity[] referringEntities;
	private Requester[] requesters;
	private ServiceType[] serviceTypes;
	private Resolver[] resolvers;
	private Referrer[] referrers;

	ContextObjectImpl(Referent referent,
			ReferringEntity[] referringEntities,
			Requester[] requesters,
			ServiceType[] serviceTypes,
			Resolver[] resolvers,
			Referrer[] referrers) {
		this.referent = referent;
		this.referringEntities = referringEntities;
		this.requesters = requesters;
		this.serviceTypes = serviceTypes;
		this.resolvers = resolvers;
		this.referrers = referrers;
	}
	
	public Referent getReferent() {
		return referent;
	}

	public ServiceType[] getServiceTypes() {
		return serviceTypes;
	}

	public ReferringEntity[] getReferringEntities() {
		return referringEntities;
	}

	public Requester[] getRequesters() {
		return requesters;
	}

	public Resolver[] getResolvers() {
		return resolvers;
	}

	public Referrer[] getReferrers() {
		return referrers;
	}
	
	public String toString() {
		return new StringBuffer(referent.toString()).append("\n")
		.append(referringEntities).append("\n")
		.append(requesters).append("\n")
		.append(serviceTypes).append("\n")
		.append(resolvers).append("\n")
		.append(referrers).append("\n")
		.toString();
	}
}
