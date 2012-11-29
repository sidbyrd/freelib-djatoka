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
 * Referrer doesn't have a convenient synonym like most of
 * the others. Its purpose is related to the decoupling
 * of the content of a request from any particular Resolver.
 * Specifically, Referrer identifies some vague entity that
 * generated a free-standing ContextObject and unleashed it
 * on the world. One apparent benefit is that it might provide
 * some clues on any idiosyncracies found in the ContextObject.
 * It might also be useful for logging purposes. To take advantage
 * of this capability, you will probably want to choose one of
 * the unfriendly Transports described in the OpenURL 
 * spec: openurl-by-ref, openurl-by-val, or openurl-inline.
 * This information would be difficult to represent in Transports
 * that can be understood by mere mortals. Normal Transports
 * will probably want to assign a value of null whenever
 * they encounter a Referrer variable.
 
 * @author Jeffrey A. Young
 */
public interface Referrer extends Entity {
}
