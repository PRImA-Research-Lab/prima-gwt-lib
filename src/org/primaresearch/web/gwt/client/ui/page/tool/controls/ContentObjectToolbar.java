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
package org.primaresearch.web.gwt.client.ui.page.tool.controls;

import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.web.gwt.client.ui.page.PageScrollView;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Toolbar panel for selected page content objects (hovering next to the object).<br>
 * <br>
 * CSS style name: pageViewHoverToolbar
 * 
 * @author Christian Clausner
 *
 */
public class ContentObjectToolbar implements PageViewHoverWidget, SelectionListener {

	private HorizontalPanel panel;
	private PageScrollView view;
	private ContentObjectC selObj;
	private SelectionManager selectionManager;
	private int offsetX;
	private int offsetY;

	/**
	 * Constructor (toolbar at top left corder of selected object, default offset)
	 * @param selectionManager Content object selection manager to add a listener.
	 */
	public ContentObjectToolbar(SelectionManager selectionManager) {
		this(selectionManager, 0, 8);
	}

	/**
	 * Constructor (toolbar at top left corner of selected object, custom horizontal offset)
	 * @param selectionManager Content object selection manager to add a listener.
	 * @param offsetX Horizontal offset for the toolbar
	 */
	public ContentObjectToolbar(SelectionManager selectionManager, int offsetX) {
		this(selectionManager, offsetX, 8);
	}

	/**
	 * Constructor (toolbar at top left corner of selected object, custom offset)
	 * @param selectionManager Content object selection manager to add a listener.
	 * @param offsetX Horizontal offset for the toolbar
	 * @param offsetY Vertical offset for the toolbar
	 */
	public ContentObjectToolbar(SelectionManager selectionManager, int offsetX, int offsetY) {
		this.selectionManager = selectionManager;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		panel = new HorizontalPanel();
		panel.addStyleName("pageViewHoverToolbar");
		if (selectionManager.getSelection().size() == 1)
			selObj = selectionManager.getSelection().iterator().next();
		else
			selObj = null;
		selectionManager.addListener(this);
	}
	
	/**
	 * Cleans up when the toolbar isn't needed any longer.
	 */
	public void dispose() {
		selectionManager.removeListener(this);
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	@Override
	public void setPageView(PageScrollView view) {
		this.view = view;
	}

	@Override
	public PageScrollView getPageView() {
		return view;
	}

	@Override
	public void refresh() {
		
		this.asWidget().setVisible(selObj != null);

		if (selObj != null)
			updatePosition();
	}
	
	/**
	 * Positions the toolbar at the top left corner of the selected content object.
	 * Applies an offset, if defined. 
	 */
	private void updatePosition() {
		int height = this.asWidget().getElement().getClientHeight();
		Rect objectRect = selObj.getCoords().getBoundingBox();
		int x = (int)(((objectRect.left) * getPageView().getZoomFactor()) + offsetX);
		int y = (int)(((objectRect.top) * getPageView().getZoomFactor()) - height - offsetY);
		setPosistion(x, y);
	}

	@Override
	public void setPosistion(int x, int y) {
		view.getViewPanel().setWidgetPosition(asWidget(), x, y);
	}
	
	/**
	 * Adds a control to the toolbar (e.g. button or separator)
	 */
	public void add(Widget widget) {
		panel.add(widget);
	}
	
	/**
	 * Adds a control to the toolbar (e.g. button or separator)
	 */
	public void add(IsWidget widget) {
		panel.add(widget);
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		if (manager.getSelection().size() == 1)
			selObj = manager.getSelection().iterator().next();
		else
			selObj = null;
		
		refresh();
	}

}
