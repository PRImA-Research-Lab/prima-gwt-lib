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
package org.primaresearch.web.gwt.client.page;

import java.util.ArrayList;

import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.maths.geometry.Dimension;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.shared.Pair;
import org.primaresearch.shared.variable.Variable;
import org.primaresearch.web.gwt.shared.RemoteException;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.ContentObjectSync;
import org.primaresearch.web.gwt.shared.page.GroupC;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Synchronisation of page content between client and server.
 *
 * @author Christian Clausner
 *
 */
@RemoteServiceRelativePath("documentPageSync")
public interface DocumentPageSyncService extends RemoteService {

	/**
	 * Requests page content objects a specified type (e.g. text lines) from the server.
	 * @param url Source of PAGE XML file (optional)
	 * @param contentType Type of requested page content objects  (supported: 'Region', 'TextLine', 'Word', 'Glyph')
	 * @return List of content objects
	 * @throws RemoteException
	 */
	public ArrayList<ContentObjectC> loadContentObjects(String url, String contentType) throws RemoteException;
	
	/**
	 * Requests the page reading order from the server.
	 * @param url Source of PAGE XML file (optional)
	 * @return Root group of reading order.
	 * @throws RemoteException
	 */
	public GroupC loadReadingOrder(String url) throws RemoteException;

	/**
	 * Sends updated text content for a page object to the server.
	 * @param url Source of PAGE XML file (optional)
	 * @param type Type of page object  (supported: 'Region', 'TextLine', 'Word', 'Glyph')
	 * @param contentObjectId ID of page object
	 * @param text Text content
	 * @return True if successful
	 * @throws RemoteException
	 */
	public Boolean putTextContent(String url, ContentType type, String contentObjectId, String text) throws RemoteException;
	
	/**
	 * Sends a new attribute value for a page content object to the server.
	 * @param url Source of PAGE XML file (optional)
	 * @param type Type of page object  (supported: 'Region', 'TextLine', 'Word', 'Glyph')
	 * @param contentObjectId ID of page object
	 * @param attr Attribute with new value
	 * @return True if successful
	 * @throws RemoteException
	 */
	public Boolean setAttributeValue(String url, ContentType type, String contentObjectId, Variable attr) throws RemoteException;
	
	/**
	 * Sends updated region type and sub-type for one region to the server.
	 * @param url
	 * @param oldType
	 * @param newType
	 * @param newSubType
	 * @param contentObjectId
	 * @return The changed object together with IDs of child objects that are to delete on client side or null
	 * @throws RemoteException
	 */
	public Pair<ContentObjectC,ArrayList<String>> setRegionType(String url, RegionType oldType, RegionType newType, String newSubType, String contentObjectId) throws RemoteException;
	
	//public MetaData loadMetaData(String url);
	
	/**
	 * Requests the Ground Truth and Storage ID (GtsID) of the current PAGE file from the server.
	 * @param url Source of PAGE XML file (optional)
	 * @return The ID
	 * @throws RemoteException
	 */
	public String getPageId(String url) throws RemoteException;
	
	/**
	 * Requests to revert all changes made to a document page. 
	 * @param url Source of PAGE XML file (optional)
	 * @return True if successful
	 * @throws RemoteException
	 */
	public Boolean revertChanges(String url) throws RemoteException;
	
	/**
	 * Prototype: Loads page content objects from multiple PAGE XML files. 
	 * @param metsFileUrl Source of METS file that contains links to the PAGE files.
	 * @param contentType Type of requested page content objects  (supported: 'Region', 'TextLine', 'Word', 'Glyph')
	 * @return List of lists with page objects.
	 * @throws RemoteException
	 */
	public ArrayList<ArrayList<ContentObjectC>> loadMultiPageContentObjects(String metsFileUrl, String contentType) throws RemoteException;

	/**
	 * Prototype: Requests Ground Truth and Storage IDs (GtsIDs) for multiple PAGE XML files. 
	 * @param metsFileUrl Source of METS file that contains links to the PAGE files.
	 * @return List of IDs
	 * @throws RemoteException
	 */
	public ArrayList<String> getMultiplePageIds(String metsFileUrl) throws RemoteException;

	/**
	 * Adds a new content object to the PAGE file on the server.
	 * @param url Source of PAGE XML file (optional)
	 * @param object Page content object (e.g. a text line)
	 * @return Updated content object (new ID) 
	 * @throws RemoteException
	 */
	public ContentObjectSync addContentObject(String url, ContentObjectC object) throws RemoteException;
	
	/**
	 * Sends an updated page object polygon to the server.
	 * @param url Source of PAGE XML file (optional)
	 * @param type Type of page object  (supported: 'Region', 'TextLine', 'Word', 'Glyph')
	 * @param contentObjectId ID of page object
	 * @param outline Polygon
	 * @return True if successful
	 * @throws RemoteException
	 */
	public Boolean updateOutline(String url, ContentType type, String contentObjectId, Polygon outline) throws RemoteException;
	
	/**
	 * Deletes a page object from the PAGE file on the server.
	 * @param url Source of PAGE XML file (optional)
	 * @param type Type of page object  (supported: 'Region', 'TextLine', 'Word', 'Glyph')
	 * @param contentObjectId ID of page object
	 * @return True if successful
	 * @throws RemoteException
	 */
	public Boolean deleteContentObject(String url, ContentType type, String contentObjectId) throws RemoteException;
	
	/**
	 * Saves a PAGE file permanently.
	 * @param url Source of PAGE XML file (optional)
	 * @return True if successful
	 * @throws RemoteException
	 */
	public Boolean save(String url) throws RemoteException;
	
	/**
	 * Saves all PAGE XML files specified in the METS file in the local temp folder
	 * using a sub-folder of the given name.
	 */
	public Boolean savetMultiplePagesLocally(String metsFileUrl, String folderName) throws RemoteException;
	
	/**
	 * Gets the document page size (width and height) from the server.
	 * @param url Source of PAGE XML file (optional)
	 * @return Width and height as specified in PAGE file
	 * @throws RemoteException
	 */
	public Dimension getPageSize(String url) throws RemoteException;

}
