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
 * Resolver doesn't have a convenient synonym like most of
 * the others. Its purpose is to decouple the content of
 * a request from any specific Resolver. This is a great
 * benefit in relation to OpenURL's roots in citation linking,
 * but it is the source of considerable confusion for everyone
 * else. Generally speaking, I would expect this information to
 * be part of the server's configuration rather than have it
 * passed in from the client. If you disagree, you will probably
 * want to use one of the unfriendly Transports
 * described in the OpenURL spec that are ready to accommodate it:
 * openurl-by-ref, openurl-by-val, or openurl-inline.
 * This information would be difficult to represent in Transports
 * that can be understood by mere mortals. Normal Transports
 * will probably want to assign a value of null whenever
 * they encounter a Resolver variable.
 * <p />
 * If you <em>are</em> tempted to use this Entity, consider including
 * another ServiceType in the ContextObject instead. This
 * ServiceType should contain an info.openurl.oom.Service
 * instantiation to deal with the situation along with URL
 * descriptors indicating alternate resolvers.
 * 
 * @author Jeffrey A. Young
 */
public interface Resolver extends Entity {
}
