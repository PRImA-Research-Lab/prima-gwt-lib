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

import java.util.ArrayList;
import java.util.Iterator;

import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.maths.geometry.Dimension;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.page.PageSyncManager.PageSyncListener;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.ContentObjectSync;
import org.primaresearch.web.gwt.shared.page.GroupC;
import org.primaresearch.web.gwt.shared.page.GroupMemberC;
import org.primaresearch.web.gwt.shared.page.RegionRefC;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class ReadingOrderTreeView implements PageSyncListener, SelectionListener, SelectionHandler<TreeItem> {
	
	private Tree tree = null;
	private PageLayoutC pageLayout;
	private SelectionManager selectionManager;

	public ReadingOrderTreeView(PageLayoutC pageLayout, SelectionManager selectionManager) {
		this.pageLayout = pageLayout;
		this.selectionManager = selectionManager;
		selectionManager.addListener(this);
		refresh();
	}
	
	public Widget getWidget() {
		return tree;
	}
	
	public void refresh() {
		if (tree == null) {
			tree = new Tree();
			tree.addSelectionHandler(this);
		} else
			tree.clear();
		
		if (pageLayout != null && pageLayout.getReadingOrder() != null) {
			addGroup(pageLayout.getReadingOrder(), null);
		}
		
	}
	
	private void addGroup(GroupC group, TreeItem parent) {
		if (group != null) {
			//Group tree item
			String caption = "Group '";
			if (group.caption != null && !group.caption.isEmpty())
				caption += group.caption;
			else if (parent == null)
				caption += "Root";
			else
				caption += group.id;
			caption += group.ordered ? "' (ordered)" : "' (unordered)";
			TreeItem groupItem = new TreeItem(new Label(caption));
			if (parent == null)
				tree.addItem(groupItem);
			else
				parent.addItem(groupItem);
			
			//Members
			if (group.members != null) {
				for (Iterator<GroupMemberC> it = group.members.iterator(); it.hasNext(); ) {
					GroupMemberC member = it.next();
					if (member instanceof GroupC)
						addGroup((GroupC)member, groupItem);
					else { //Region ref
						TreeItem regionRefItem = new TreeItem(new Label("Region '"+((RegionRefC)member).regionId+"'"));
						regionRefItem.setUserObject(((RegionRefC)member).regionId);
						groupItem.addItem(regionRefItem);
					}
				}
			}

			groupItem.setState(true);
		}
	}

	@Override
	public void contentLoaded(String contentType) {
	}

	@Override
	public void contentLoadingFailed(String contentType, Throwable caught) {
	}

	@Override
	public void pageIdLoaded(String id) {
	}

	@Override
	public void pageIdLoadingFailed(Throwable caught) {
	}

	@Override
	public void readingOrderLoaded() {
		refresh();
	}

	@Override
	public void readingOrderLoadingFailed(Throwable caught) {
	}

	@Override
	public void contentObjectAdded(ContentObjectSync syncObj,
			ContentObjectC localObj) {
	}

	@Override
	public void contentObjectAddingFailed(ContentObjectC object,
			Throwable caught) {
	}

	@Override
	public void contentObjectDeleted(ContentObjectC object) {
		if (object != null && object.getType() != null && object.getType() instanceof RegionType)
			refresh();
	}

	@Override
	public void contentObjectDeletionFailed(ContentObjectC object,
			Throwable caught) {
	}

	@Override
	public void textContentSynchronized(ContentObjectC object) {
	}

	@Override
	public void textContentSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void attributeSynchronized(ContentObjectC object) {
	}

	@Override
	public void attributeSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void regionTypeSynchronized(ContentObjectC object,
			ArrayList<String> childObjectsToDelete) {
	}

	@Override
	public void regionTypeSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void objectOutlineSynchronized(ContentObjectC object) {
	}

	@Override
	public void objectOutlineSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void pageFileSaved() {
	}

	@Override
	public void pageFileSaveFailed(Throwable caught) {
	}

	@Override
	public void changesReverted() {
	}

	@Override
	public void revertChangesFailed(Throwable caught) {
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		try {
			if (manager.getSelection() != null && manager.getSelection().size() == 1) {
				ContentObjectC selObj = manager.getSelection().iterator().next();
				if (selObj.getType() instanceof RegionType) {
					selectRegionRefTreeItem(selObj);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private void selectRegionRefTreeItem(ContentObjectC region) {
		if (tree.getItemCount() > 0) {
			TreeItem item = tree.getItem(0);
			selectRegionRefTreeItem(region, item);
		}
	}
	
	private void selectRegionRefTreeItem(ContentObjectC region, TreeItem item) {
		if (item.getUserObject() != null && region != null && region.getId().equals(item.getUserObject())) {
			item.setSelected(true);
		} else {
			item.setSelected(false);
			for (int i=0; i<item.getChildCount(); i++) {
				selectRegionRefTreeItem(region, item.getChild(i));
			}
		}
	}

	@Override
	public void onSelection(SelectionEvent<TreeItem> event) {
		TreeItem item = event.getSelectedItem();
		if (item != null && item.getUserObject() != null) {
			ContentObjectC region = pageLayout.findContentObject((String)item.getUserObject()); 
			selectionManager.setSelection(region);
			//selectRegionRefTreeItem(region);
		}
		/*if (item != null) {
			//Group?
			if (item.getUserObject() == null)
				item.setSelected(false);
			else { //Region ref
			}
		}*/
		
	}

	@Override
	public void pageSizeReceived(Dimension pageSize) {
	}

	@Override
	public void getPageSizeFailed(Throwable caught) {
	}
}
