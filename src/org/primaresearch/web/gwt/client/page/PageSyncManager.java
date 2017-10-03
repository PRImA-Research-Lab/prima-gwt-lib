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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.primaresearch.dla.page.layout.physical.shared.LowLevelTextType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.maths.geometry.Dimension;
import org.primaresearch.shared.Pair;
import org.primaresearch.shared.variable.Variable;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.ContentObjectSync;
import org.primaresearch.web.gwt.shared.page.GroupC;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Manager for synchronisation of page content between server and client.
 * 
 * @author Christian Clausner
 *
 */
public class PageSyncManager {
	
	private String url;
	private DocumentPageSyncServiceAsync syncService = GWT.create(DocumentPageSyncService.class);
	private Set<PageSyncListener> listeners = new HashSet<PageSyncListener>(2);
	private PageLayoutC pageLayout;

	/**
	 * Constructor
	 * 
	 * @param url URL of page file to sync with
	 * @param pageLayout Client side page layout object
	 */
	public PageSyncManager(String url, PageLayoutC pageLayout) {
		this.url = url;
		this.pageLayout = pageLayout;
	}
	
	/**
	 * Loads all page content objects of the specified type.  
	 * @param contentType Content type (Region, TextLine, ...)
	 */
	public void loadContentObjectsAsync(final String contentType) {
	    AsyncCallback<ArrayList<ContentObjectC>> callback = new AsyncCallback<ArrayList<ContentObjectC>>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersContentLoadingFailed(contentType, caught);
	    	}

	    	public void onSuccess(ArrayList<ContentObjectC> contentObjects) {
	    		pageLayout.setContent(contentType, contentObjects);
	    		notifyListenersContentLoaded(contentType);
	    	}
	    };
	    syncService.loadContentObjects(url, contentType, callback);
	}
	
	/**
	 * Triggers loading the reading order from the server.
	 */
	public void loadReadingOrderAsync() {
	    AsyncCallback<GroupC> callback = new AsyncCallback<GroupC>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersReadingOrderLoadingFailed(caught);
	    	}

	    	public void onSuccess(GroupC readingOrderRoot) {
	    		pageLayout.setReadingOrder(readingOrderRoot);
	    		notifyListenersReadingOrderLoaded();
	    	}
	    };
	    syncService.loadReadingOrder(url, callback);
	}
	
	/**
	 * Triggers loading the page size from the server.
	 */
	public void getPageSizeAsync() {
	    AsyncCallback<Dimension> callback = new AsyncCallback<Dimension>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersGetPageSizeFailed(caught);
	    	}

	    	public void onSuccess(Dimension size) {
	    		notifyListenersPageSizeReceived(size);
	    	}
	    };
	    syncService.getPageSize(url, callback);
	}
	
	/**
	 * Sends the text content of the given object to the server.
	 * @param object Text container content object
	 */
	public void syncTextContent(final ContentObjectC object) {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersTextContentSyncFailed(object, caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersTextContentSynced(object);
	    	}
	    };
	    syncService.putTextContent(url, object.getType(), object.getId(), object.getText(), callback);
	}
	
	/**
	 * Sends the text content of the given object to the server.
	 * @param object Text container content object
	 */
	public void syncAttribute(final ContentObjectC object, final Variable attr) {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersAttributeSyncFailed(object, caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersAttributeSynced(object);
	    	}
	    };
	    syncService.setAttributeValue(url, object.getType(), object.getId(), attr, callback);
	}

	/**
	 * Sends the text content of the given object to the server.
	 * @param object Text container content object
	 */
	public void syncRegionType(final ContentObjectC region, final RegionType newType, final String newSubType) {
		if (!(region.getType() instanceof RegionType))
				return;
	    AsyncCallback<Pair<ContentObjectC,ArrayList<String>>> callback = new AsyncCallback<Pair<ContentObjectC,ArrayList<String>>>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersRegionTypeSyncFailed(region, caught);
	    	}

	    	public void onSuccess(Pair<ContentObjectC,ArrayList<String>> resultData) {
	    		notifyListenersRegionTypeSynced(resultData);
	    	}
	    };
	    syncService.setRegionType(url, (RegionType)region.getType(), newType, newSubType, region.getId(), callback);
	}

	/*public void loadMetaData() {
	    AsyncCallback<MetaData> callback = new AsyncCallback<MetaData>() {
	    	public void onFailure(Throwable caught) {
	    	}

	    	public void onSuccess(MetaData metaData) {
	    		pageLayout.setMetaData(metaData);
	    		notifyListenersMetaDataLoaded();
	    	}
	    };
	    syncService.loadMetaData(url, callback);
	}*/
	
	/**
	 * Requests a PAGE Ground Truth and Storage ID (GtsID) from the server.
	 */
	public void loadPageId() {
	    AsyncCallback<String> callback = new AsyncCallback<String>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersPageIdLoadingFailed(caught);
	    	}

	    	public void onSuccess(String id) {
	    		pageLayout.setId(id);
	    		notifyListenersPageIdLoaded(id);
	    	}
	    };
	    syncService.getPageId(url, callback);
	}
	
	public void addListener(PageSyncListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PageSyncListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListenersContentLoaded(String contentType) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentLoaded(contentType);
		}
	}

	private void notifyListenersReadingOrderLoaded() {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().readingOrderLoaded();
		}
	}

	//private void notifyListenersMetaDataLoaded() {
	//	for (Iterator<PageLoadListener> it = listeners.iterator(); it.hasNext(); ) {
	//		it.next().metaDataLoaded();
	//	}
	//}
	
	private void notifyListenersPageIdLoaded(String id) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageIdLoaded(id);
		}
	}

	private void notifyListenersContentLoadingFailed(String contentType, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentLoadingFailed(contentType, caught);
		}
	}

	private void notifyListenersReadingOrderLoadingFailed(Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().readingOrderLoadingFailed(caught);
		}
	}

	private void notifyListenersPageIdLoadingFailed(Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageIdLoadingFailed(caught);
		}
	}

	private void notifyListenersContentObjectAddingFailed(ContentObjectC object, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentObjectAddingFailed(object, caught);
		}
	}
	
	private void notifyListenersContentObjectDeletionFailed(ContentObjectC object, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentObjectDeletionFailed(object, caught);
		}
	}

	private void notifyListenersTextContentSyncFailed(ContentObjectC object, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().textContentSyncFailed(object, caught);
		}
	}

	private void notifyListenersAttributeSyncFailed(ContentObjectC object, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().attributeSyncFailed(object, caught);
		}
	}

	private void notifyListenersRegionTypeSyncFailed(ContentObjectC object, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().regionTypeSyncFailed(object, caught);
		}
	}

	private void notifyListenersObjectOutlineSyncFailed(ContentObjectC object, Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().objectOutlineSyncFailed(object, caught);
		}
	}

	private void notifyListenersPageFileSaveFailed(Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageFileSaveFailed(caught);
		}
	}

	private void notifyListenersRevertChangesFailed(Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().revertChangesFailed(caught);
		}
	}

	private void notifyListenersContentObjectAdded(ContentObjectSync syncObj, ContentObjectC localObj) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentObjectAdded(syncObj, localObj);
		}
	}

	private void notifyListenersContentObjectDeleted(ContentObjectC obj) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().contentObjectDeleted(obj);
		}
	}

	private void notifyListenersTextContentSynced(ContentObjectC obj) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().textContentSynchronized(obj);
		}
	}

	private void notifyListenersAttributeSynced(ContentObjectC obj) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().attributeSynchronized(obj);
		}
	}

	private void notifyListenersRegionTypeSynced(Pair<ContentObjectC,ArrayList<String>> resultData) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			ContentObjectC obj = resultData != null ? resultData.left : null;
			ArrayList<String> toDelete = resultData != null ? resultData.right : null;
			it.next().regionTypeSynchronized(obj, toDelete);
		}
	}

	private void notifyListenersObjectOutlineSynced(ContentObjectC obj) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().objectOutlineSynchronized(obj);
		}
	}
	
	private void notifyListenersPageFileSaved() {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageFileSaved();
		}
	}
	
	private void notifyListenersChangesReverted() {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().changesReverted();
		}
	}

	private void notifyListenersPageSizeReceived(Dimension pageSize) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().pageSizeReceived(pageSize);
		}
	}
	
	private void notifyListenersGetPageSizeFailed(Throwable caught) {
		for (Iterator<PageSyncListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().getPageSizeFailed(caught);
		}
	}

	/**
	 * Resets this manager.
	 */
	public void clear() {
		url = null;
		pageLayout.clear();
	}
	
	/**
	 * Sets the current PAGE XML source.
	 * @param url URL of PAGE file.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	/**
	 * Adds the given content object (that has been created on client side) 
	 * to the page layout on the server. Returns the same object enriched with
	 * new ID and attributes.<br>
	 * If the server returns <code>null</code> as object, the object could not
	 * be added to the page layout on server side and will be removed from the client page layout.
	 */
	public void addContentObject(final ContentObjectC object) {
	    AsyncCallback<ContentObjectSync> callback = new AsyncCallback<ContentObjectSync>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersContentObjectAddingFailed(object, caught);
	    	}

	    	public void onSuccess(ContentObjectSync res) {
	    		if (res != null) {
		    		ContentObjectC obj = pageLayout.findContentObject(res.id);
		    		if (obj != null) {
		    			if (res.object != null) {
		    				//Copy content
		    				obj.setId(res.object.getId());
		    				obj.setAttributes(res.object.getAttributes());
		    				notifyListenersContentObjectAdded(res, obj);
		    			} 
		    			else { //No object returned from server -> delete object on client
		    				pageLayout.remove(obj);
		    				String text = null;
		    				if (LowLevelTextType.TextLine.equals(obj.getType()))
		    					text = "The text line has not been created because there is no text region at this position.";
		    				else if (LowLevelTextType.Word.equals(obj.getType()))
		    					text = "The word has not been created because there is no text line at this position.";
		    				else if (LowLevelTextType.Glyph.equals(obj.getType()))
		    					text = "The glyph has not been created because there is no word at this position.";
		    				if (text != null)
		    					Window.alert(text);
		    			}
		    		}
	    		}
	    	}
	    };
	    syncService.addContentObject(url, object, callback);
	}

	/**
	 * Sends an updated object polygon to the server.
	 * @param object Page object with polygon.
	 */
	public void syncObjectOutline(final ContentObjectC object) {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersObjectOutlineSyncFailed(object, caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersObjectOutlineSynced(object);
	    	}
	    };
	    syncService.updateOutline(url, object.getType(), object.getId(), object.getCoords(), callback);
	}

	/**
	 * Sends a request to delete a page object on the server.
	 * @param object
	 */
	public void deleteContentObject(final ContentObjectC object) {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersContentObjectDeletionFailed(object, caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersContentObjectDeleted(object);
	    	}
	    };
	    syncService.deleteContentObject(url, object.getType(), object.getId(), callback);
	}
	
	/**
	 * Sends a request to save the current PAGE file permanently.
	 */
	public void save() {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersPageFileSaveFailed(caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersPageFileSaved();
	    	}
	    };
	    syncService.save(url, callback);
	}
	
	/**
	 * Sends a request to discard all changes since the initial load or the last save.
	 */
	public void revertChanges() {
	    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
	    	public void onFailure(Throwable caught) {
	    		notifyListenersRevertChangesFailed(caught);
	    	}

	    	public void onSuccess(Boolean success) {
	    		notifyListenersChangesReverted();
	    	}
	    };
	    syncService.revertChanges(url, callback);
	}
	

	/**
	 * Listener interface for page content synchronisation between client and server.
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static interface PageSyncListener {
		
		/** Called when the document page content (e.g. regions) has been loaded successfully */
		public void contentLoaded(String contentType);
		/** Called when loading the document page content (e.g. regions) has failed */
		public void contentLoadingFailed(String contentType, Throwable caught);
		
		//public void metaDataLoaded();
		
		/** Called when the document page ID (ground truth and storage ID) has been loaded successfully */
		public void pageIdLoaded(String id);
		/** Called when loading the document page ID (ground truth and storage ID) has failed */
		public void pageIdLoadingFailed(Throwable caught);
		
		/** Called when the page reading order has been loaded successfully */
		public void readingOrderLoaded();
		/** Called when loading the page reading order has failed */
		public void readingOrderLoadingFailed(Throwable caught);
		
		/** Called when the page content object (e.g. a region) has been added successfully on server side */
		public void contentObjectAdded(ContentObjectSync syncObj, ContentObjectC localObj);
		/** Called when the page content object (e.g. a region) could not be added on server side */
		public void contentObjectAddingFailed(ContentObjectC object, Throwable caught);

		/** Called when the page content object (e.g. a region) has been deleted successfully on server side */
		public void contentObjectDeleted(ContentObjectC object);
		/** Called when the page content object (e.g. a region) could not be deleted on server side */
		public void contentObjectDeletionFailed(ContentObjectC object, Throwable caught);
		
		/** Called when the text content has been sent successfully to the server */
		public void textContentSynchronized(ContentObjectC object);
		/** Called when the text content could not be synchronised to the server */
		public void textContentSyncFailed(ContentObjectC object, Throwable caught);

		/** Called when an attribute has been sent successfully to the server */
		public void attributeSynchronized(ContentObjectC object);
		/** Called when the attribute could not be synchronised to the server */
		public void attributeSyncFailed(ContentObjectC object, Throwable caught);

		/** Called when the region type change has been applied successfully on server side */
		public void regionTypeSynchronized(ContentObjectC object, ArrayList<String> childObjectsToDelete);
		/** Called when the region type change could not be applied on server side */
		public void regionTypeSyncFailed(ContentObjectC object, Throwable caught);

		/** Called when the object outline change has been applied successfully on server side */
		public void objectOutlineSynchronized(ContentObjectC object);
		/** Called when the object outline change could not be applied on server side */
		public void objectOutlineSyncFailed(ContentObjectC object, Throwable caught);
		
		/** Called when the page content has been saved successfully on server side */
		public void pageFileSaved();
		/** Called when the page content could not be saved on server side */
		public void pageFileSaveFailed(Throwable caught);

		/** Called when the revert has been executed successfully on server side */
		public void changesReverted();
		/** Called when the revert could not executed on server side */
		public void revertChangesFailed(Throwable caught);

		/** Called when the page size has been received from on server side */
		public void pageSizeReceived(Dimension pageSize);
		/** Called when the page size could not be loaded from the server */
		public void getPageSizeFailed(Throwable caught);
	}
}
