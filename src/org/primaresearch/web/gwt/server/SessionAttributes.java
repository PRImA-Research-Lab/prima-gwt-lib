/*
 * Copyright 2015 PRImA Research Lab, University of Salford, United Kingdom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primaresearch.web.gwt.server;

/**
 * Collection of constants for session attribute names.
 * @author Christian Clausner
 *
 */
public interface SessionAttributes {

	public static final String USER_AUTH 				= "USER_AUTHENTICATED";
	public static final String USER_ID 					= "USER_ID";
	public static final String ATTACHMENT_ID 			= "ATTACHMENT_ID";
	public static final String SOAP_SERVICE 			= "SOAP_SERVICE";
	public static final String PERMISSIONS 				= "USER_PERMISSIONS";
	public static final String PAGE_CONTENT_WEB_SERVICE = "PAGE_CONTENT_WEB_SERVICE";

}
