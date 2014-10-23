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
package org.primaresearch.web.gwt.client.ui.page.tool;

import org.primaresearch.maths.geometry.Point;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.web.gwt.client.page.PageSyncManager;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.client.ui.page.tool.controls.DraggableControl;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.dom.client.Style.Cursor;

/**
 * Tool/control for repositioning page content objects.
 * 
 * @author Christian Clausner
 *
 */
public class MoveRegionTool extends DraggableControl implements SelectionListener {

	private ContentObjectC selObj = null;
	private Polygon referencePolygon = null;
	private PageSyncManager pageSync;
	private boolean alwaysRefreshPageView;
	private boolean fillWholeRegion;
	//private int posInBoundingBoxX = 0;
	//private int posInBoundingBoxY = 0;
	
	/**
	 * Constructor
	 * 
	 * @param imageUrl Icon resource
	 * @param selectionManager Content object selection manager to add a listener
	 * @param pageSync Synchronisation manager for sending the changed outline to the server
	 * @param alwaysRefreshPageView If set to <code>false</code> the view is only refreshed when the user releases the mouse button, otherwise it is refreshed as well when moving the mouse.  
	 */
	public MoveRegionTool(String imageUrl, SelectionManager selectionManager, PageSyncManager pageSync,
							boolean alwaysRefreshPageView, boolean fillWholeRegion) {
		super(imageUrl);
		selectionManager.addListener(this);
		this.asWidget().setVisible(false);
		this.asWidget().getElement().getStyle().setCursor(Cursor.MOVE);
		this.pageSync = pageSync;
		this.alwaysRefreshPageView = alwaysRefreshPageView;
		this.fillWholeRegion = fillWholeRegion;
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		if (manager.isEmpty() || manager.getSelection().size() > 1) {
			this.asWidget().setVisible(false);
			selObj = null;
		} 
		else { //One object selected
			selObj = manager.getSelection().iterator().next();

			this.asWidget().setVisible(selObj != null && !selObj.isReadOnly());
			
			updatePosition();
		}
	}
	
	/**
	 * Updates the position of the control in relation to the currently selected page content object.
	 */
	private void updatePosition() {
		if (selObj == null)
			return;
		int x,y;
		Rect objectRect = selObj.getCoords().getBoundingBox();

		if (fillWholeRegion) {
			x = (int)(objectRect.left * getPageView().getZoomFactor());
			y = (int)(objectRect.top * getPageView().getZoomFactor());
			int w = (int)(objectRect.getWidth() * getPageView().getZoomFactor());
			int h = (int)(objectRect.getHeight() * getPageView().getZoomFactor());
			this.asWidget().setWidth(w+"px");
			this.asWidget().setHeight(h+"px");
		} else { //Centre
			int halfWidth = this.asWidget().getElement().getClientWidth() / 2;
			int halfHeight = this.asWidget().getElement().getClientHeight() / 2;
			x = (int)(((objectRect.left + objectRect.right)/2.0) * getPageView().getZoomFactor()) - halfWidth;
			y = (int)(((objectRect.top + objectRect.bottom)/2.0) * getPageView().getZoomFactor()) - halfHeight;
		}
		setPosistion(x, y);
	}

	@Override
	protected void onDrag(Point start, Point current) {
		if (referencePolygon == null) {
			//Keep the original polygon as reference 
			referencePolygon = selObj.getCoords().clone();
			//posInBoundingBoxX = start.x - referencePolygon.getBoundingBox().left;
			//posInBoundingBoxY = start.y - referencePolygon.getBoundingBox().top;
		}

		//Calculate the offset from the 'mouse down point'
		int translateX = (int)((double)(current.x - start.x) / getPageView().getZoomFactor());
		int translateY = (int)((double)(current.y - start.y) / getPageView().getZoomFactor());
		
		//Confine to document dimensions
		Rect boundingBox = referencePolygon.getBoundingBox();
		if (boundingBox.left + translateX < 0)
			translateX = -boundingBox.left;
		else if (boundingBox.right + translateX >= pageView.getPageLayout().getWidth())
			translateX = pageView.getPageLayout().getWidth() - boundingBox.right - 1;
		if (boundingBox.top + translateY < 0)
			translateY = -boundingBox.top;
		else if (boundingBox.bottom + translateY >= pageView.getPageLayout().getHeight())
			translateY = pageView.getPageLayout().getHeight() - boundingBox.bottom - 1;

		//Update the position of the polygon
		Polygon polygon = selObj.getCoords();
		for (int i=0; i<polygon.getSize(); i++) {
			polygon.getPoint(i).x = referencePolygon.getPoint(i).x + translateX;
			polygon.getPoint(i).y = referencePolygon.getPoint(i).y + translateY;
		}
		polygon.setBoundingBoxOutdated();
		
		//Update all other widgets that are on the page view
		pageView.refreshHoverWidgets();
		
		//Refresh page view
		if (alwaysRefreshPageView)
			pageView.getRenderer().refresh();
	}

	@Override
	protected void onDragEnd(Point start, Point current) {
		referencePolygon = null;
		this.getPageView().getRenderer().refresh();
		
		//Sync to server
		pageSync.syncObjectOutline(selObj);
	}

	@Override
	public void refresh() {
		updatePosition();
	}

}
