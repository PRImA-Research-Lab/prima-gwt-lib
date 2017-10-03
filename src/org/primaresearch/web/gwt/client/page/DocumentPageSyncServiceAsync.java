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
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.ContentObjectSync;
import org.primaresearch.web.gwt.shared.page.GroupC;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Synchronisation of page content between client and server.
 *
 * @author Christian Clausner
 *
 */
public interface DocumentPageSyncServiceAsync {

	void loadContentObjects(String url, String contentType, AsyncCallback<ArrayList<ContentObjectC>> callback);
	
	void loadReadingOrder(String url, AsyncCallback<GroupC> callback);
	
	void putTextContent(String url, ContentType type, String contentObjectId, String text, AsyncCallback<Boolean> callback);
	
	void setAttributeValue(String url, ContentType type, String contentObjectId, Variable attr, AsyncCallback<Boolean> callback);

	void setRegionType(String url, RegionType oldType, RegionType newType, String newSubType, String contentObjectId, AsyncCallback<Pair<ContentObjectC,ArrayList<String>>> callback);

	//void loadMetaData(String url, AsyncCallback<MetaData> metaData);
	
	void getPageId(String url, AsyncCallback<String> pcgtsId);
	
	void loadMultiPageContentObjects(String url, String contentType, AsyncCallback<ArrayList<ArrayList<ContentObjectC>>> callback);

	void getMultiplePageIds(String url, AsyncCallback<ArrayList<String>> pcgtsIds);

	void addContentObject(String url, ContentObjectC object, AsyncCallback<ContentObjectSync> callback);
	
	void updateOutline(String url, ContentType type, String contentObjectId, Polygon outline, AsyncCallback<Boolean> callback);
	
	void deleteContentObject(String url, ContentType type, String contentObjectId, AsyncCallback<Boolean> callback);

	void save(String url, AsyncCallback<Boolean> callback);
	
	void savetMultiplePagesLocally(String metsFileUrl, String folderName, AsyncCallback<Boolean> callback);
	
	void revertChanges(String url, AsyncCallback<Boolean> callback);

	void getPageSize(String url, AsyncCallback<Dimension> callback);
}
