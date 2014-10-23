/*
 * Copyright 2014 PRImA Research Lab, University of Salford, United Kingdom
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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Prototype to load multiple PAGE files that are defined in a METS file
 * 
 * @author Christian Clausner
 *
 */
public class MultiPageLoader {

	private String metsFileUrl;
	private List<PageLayoutC> pageLayoutList;
	private Set<MultiPageLoadListener> listeners = new HashSet<MultiPageLoadListener>(2);
	private DocumentPageSyncServiceAsync syncService = GWT.create(DocumentPageSyncService.class);
	private static final DateTimeFormat DATE_FORMATTER = DateTimeFormat.getFormat("yyyy-MM-dd_HH-mm-ss");

	public MultiPageLoader(String metsFileUrl, List<PageLayoutC> pageLayoutList) {
		this.metsFileUrl = metsFileUrl;
		this.pageLayoutList = pageLayoutList;
	}
	
	public void loadContentObjectsAsync(final String contentType) {
	    // Set up the callback object for loading multiple document layout XML
	    AsyncCallback<ArrayList<ArrayList<ContentObjectC>>> callback = new AsyncCallback<ArrayList<ArrayList<ContentObjectC>>>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersError(caught);
	    	}

	    	public void onSuccess(ArrayList<ArrayList<ContentObjectC>> contentObjects) {
	    		for (int i=0; i<contentObjects.size(); i++) {
	    			if (i >= pageLayoutList.size())
	    				pageLayoutList.add(new PageLayoutC());
	    			pageLayoutList.get(i).setContent(contentType, contentObjects.get(i));
	    		}
	    		
	    		notifyListeners(contentType);
	    	}
	    };
	    syncService.loadMultiPageContentObjects(metsFileUrl, contentType, callback);
	}
	
	public void loadPageIdsAsync() {
	    // Set up the callback object for loading multiple document layout XML
	    AsyncCallback<ArrayList<String>> callback = new AsyncCallback<ArrayList<String>>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersError(caught);
	    	}

	    	public void onSuccess(ArrayList<String> pageIds) {
	    		for (int i=0; i<pageIds.size(); i++) {
	    			if (i >= pageLayoutList.size())
	    				pageLayoutList.add(new PageLayoutC());
	    			pageLayoutList.get(i).setId(pageIds.get(i));
	    		}
	    		
	    		notifyListenersPageIdsLoaded();
	    	}
	    };
	    syncService.getMultiplePageIds(metsFileUrl, callback);
	}
	
	public void syncTextContent(PageLayoutC page, ContentObjectC object) {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersError(caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		// TODO
	    	}
	    };
	    String pageUrl = getPageFileUrlFromMetsFileUrl(metsFileUrl, page.getId());
	    syncService.putTextContent(pageUrl, object.getType(), object.getId(), object.getText(), callback);
	}
	
	public static String getPageFileUrlFromMetsFileUrl(String metsFileUrl, String pageId) {
		String urlBase = metsFileUrl.substring(0, metsFileUrl.lastIndexOf("/")+1);
		return urlBase + pageId + ".xml"; 
	}
	
	public void savePageXmlFiles() {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersError(caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersPageFilesSaved();
	    	}
	    };
	    
	    //Create folder name
	    String folderName = "CrowdPrototype_"+DATE_FORMATTER.format(new Date());
	    
	    //Save
	    syncService.savetMultiplePagesLocally(metsFileUrl, folderName, callback);
	}

	public void addListener(MultiPageLoadListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MultiPageLoadListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(String contentType) {
		for (Iterator<MultiPageLoadListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentLoaded(contentType);
		}
	}
	
	private void notifyListenersError(Throwable caught) {
		for (Iterator<MultiPageLoadListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().onPageSyncError(caught);
		}
	}
	
	private void notifyListenersPageIdsLoaded() {
		for (Iterator<MultiPageLoadListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageIdsLoaded();
		}
	}
	
	private void notifyListenersPageFilesSaved() {
		for (Iterator<MultiPageLoadListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageFilesSaved();
		}
	}
	
	
	public static interface MultiPageLoadListener {
		public void contentLoaded(String contentType);
		
		public void pageIdsLoaded();
		
		public void pageFilesSaved();

		public void onPageSyncError(Throwable caught);
	}

}
