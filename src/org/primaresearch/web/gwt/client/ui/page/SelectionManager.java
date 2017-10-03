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
package org.primaresearch.web.gwt.client.ui.page;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.primaresearch.web.gwt.shared.page.ContentObjectC;


/**
 * Manages page content object selection (selected objects, selection listeners).
 * 
 * @author Christian Clausner
 *
 */
public class SelectionManager {

	private HashSet<ContentObjectC> selection = new HashSet<ContentObjectC>();
	private HashSet<ContentObjectC> previousSelection = new HashSet<ContentObjectC>();
	
	private Set<SelectionListener> listeners = new HashSet<SelectionListener>();
	
	/**
	 * Selects the given page content object and deselects all other objects
	 */
	@SuppressWarnings("unchecked")
	public void setSelection(ContentObjectC obj) {
		boolean notify = selection.size() != 1 || !selection.contains(obj);
		
		previousSelection = (HashSet<ContentObjectC>)selection.clone();
		
		selection.clear();
		selection.add(obj);
		if (notify)
			notifyListeners();
	}
	
	/**
	 * Selects the given page content object and keeps the previously selected objects selected.
	 */
	@SuppressWarnings("unchecked")
	public void addSelection(ContentObjectC obj) {
		boolean notify = !selection.contains(obj);
		previousSelection = (HashSet<ContentObjectC>)selection.clone();
		selection.add(obj);
		if (notify)
			notifyListeners();
	}

	/**
	 * Returns a set of all currently selected page content objects
	 */
	public Set<ContentObjectC> getSelection() {
		return selection;
	}
	
	/**
	 * Returns a set of all previously selected page content objects
	 */
	public HashSet<ContentObjectC> getPreviousSelection() {
		return previousSelection;
	}

	/**
	 * Checks if the given page content object is currently selected
	 */
	public boolean isSelected(ContentObjectC obj) {
		return selection.contains(obj);
	}

	/**
	 * Deselects all objects
	 */
	@SuppressWarnings("unchecked")
	public void clearSelection() {
		boolean notify = !selection.isEmpty();
		previousSelection = (HashSet<ContentObjectC>)selection.clone();
		selection.clear();
		if (notify)
			notifyListeners();
	}
	
	/**
	 * Deselects the given page content object
	 */
	@SuppressWarnings("unchecked")
	public void removeSelection(ContentObjectC obj) {
		boolean notify = selection.contains(obj);
		previousSelection = (HashSet<ContentObjectC>)selection.clone();
		selection.remove(obj);
		if (notify)
			notifyListeners();
	}
	
	/**
	 * Toggles the selection state of the given page content object (selected->deselected or deslected->selected).
	 */
	@SuppressWarnings("unchecked")
	public void toggleSelection(ContentObjectC obj) {
		previousSelection = (HashSet<ContentObjectC>)selection.clone();
		if (isSelected(obj)) 
			removeSelection(obj);
		else
			addSelection(obj);
		notifyListeners();
	}

	/**
	 * Returns true if no object is selected; false otherwise
	 */
	public boolean isEmpty() {
		return selection.isEmpty();
	}
	
	/**
	 * Adds a selection change listener
	 */
	public void addListener(SelectionListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes a selection change listener
	 */
	public void removeListener(SelectionListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notifies all listeners of a selection change event
	 */
	private void notifyListeners() {
		for (Iterator<SelectionListener> it = listeners.iterator(); it.hasNext(); ) {
			it.next().selectionChanged(this);
		}
	}
	

	/**
	 * Listener interface for selection change events
	 *
	 */
	public static interface SelectionListener {
		/**
		 * Called when the page content object selection has changed.
		 */
		public void selectionChanged(SelectionManager manager);
	}
}
