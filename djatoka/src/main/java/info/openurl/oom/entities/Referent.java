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
package info.openurl.oom.entities;

/**
 * Referent is a fancy word meaning "what". In other words,
 * <em>what</em> resource does the client want?
 * <p />
 * IMPORTANT! Note that every ContextObject MUST contain one
 * and only one Referent. This should be your starting point when
 * thinking about how the Transport should map HTTP requests
 * into ContextObjects. The clearer you can articulate the
 * "what" entity, the easier it will be for programmers to
 * code the service, and clients to understand its purpose.
 * <p />
 * Consider, however, the fact that the descriptors in a
 * ServiceType in one request could appear within a Referent
 * in another. For example, I could pass in a Service
 * identifier as a Referent or as a ServiceType, depending
 * on whether I want information about the service, or to
 * invoke the service respectively.
 * 
 * @author Jeffrey A. Young
 */
public interface Referent extends Entity {
}