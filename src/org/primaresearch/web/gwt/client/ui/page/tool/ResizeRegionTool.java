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
 * Page view tool/control for resizing a selected page content object.
 * 
 * @author Christian Clausner
 *
 */
public class ResizeRegionTool extends DraggableControl implements SelectionListener {
	
	//Resize types
	public static final int TYPE_LEFT 	= 1;
	public static final int TYPE_RIGHT 	= 2;
	public static final int TYPE_TOP 	= 3;
	public static final int TYPE_BOTTOM	= 4;
	public static final int TYPE_TOP_LEFT 		= 5;
	public static final int TYPE_TOP_RIGHT 		= 6;
	public static final int TYPE_BOTTOM_LEFT 	= 7;
	public static final int TYPE_BOTTOM_RIGHT 	= 8;

	private int resizeType;
	private ContentObjectC selObj = null;
	private Polygon referencePolygon = null;
	private PageSyncManager pageSync;
	private boolean alwaysRefreshPageView;

	/**
	 * Constructor
	 * 
	 * @param resizeType See TYPE_... constants
	 * @param imageUrl Icon resource
	 * @param selectionManager Content object selection manager for adding a listener
	 * @param pageSync Page content synchronisation manager for sending changed object outlines to the server
	 * @param alwaysRefreshPageView  If set to <code>false</code> the view is only refreshed when the user releases the mouse button, otherwise it is refreshed as well when moving the mouse.
	 */
	public ResizeRegionTool(int resizeType, String imageUrl, SelectionManager selectionManager, PageSyncManager pageSync,
							boolean alwaysRefreshPageView) {
		super(imageUrl);
		this.resizeType = resizeType;
		selectionManager.addListener(this);
		this.asWidget().setVisible(false);
		setCursor();
		this.pageSync = pageSync;
		this.alwaysRefreshPageView = alwaysRefreshPageView;
	}
	
	/**
	 * Selects the mouse cursor according to the resize type.
	 */
	private void setCursor() {
		if (resizeType == TYPE_LEFT)
			this.asWidget().getElement().getStyle().setCursor(Cursor.W_RESIZE);
		else if (resizeType == TYPE_RIGHT)
			this.asWidget().getElement().getStyle().setCursor(Cursor.E_RESIZE);
		else if (resizeType == TYPE_TOP)
			this.asWidget().getElement().getStyle().setCursor(Cursor.N_RESIZE);
		else if (resizeType == TYPE_BOTTOM)
			this.asWidget().getElement().getStyle().setCursor(Cursor.S_RESIZE);
		else if (resizeType == TYPE_TOP_LEFT)
			this.asWidget().getElement().getStyle().setCursor(Cursor.NW_RESIZE);
		else if (resizeType == TYPE_TOP_RIGHT)
			this.asWidget().getElement().getStyle().setCursor(Cursor.NE_RESIZE);
		else if (resizeType == TYPE_BOTTOM_LEFT)
			this.asWidget().getElement().getStyle().setCursor(Cursor.SW_RESIZE);
		else if (resizeType == TYPE_BOTTOM_RIGHT)
			this.asWidget().getElement().getStyle().setCursor(Cursor.SE_RESIZE);
	}
	
	@Override
	public void selectionChanged(SelectionManager manager) {
		if (manager.isEmpty() || manager.getSelection().size() > 1) {
			//Multiple or no objects selected
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
	 * Positions the control according to the resize type (at a corner of the bounding box or in the middle of one side).
	 */
	private void updatePosition() {
		if (selObj == null)
			return;
		int halfWidth = this.asWidget().getElement().getClientWidth() / 2;
		int halfHeight = this.asWidget().getElement().getClientHeight() / 2;
		Rect objectRect = selObj.getCoords().getBoundingBox();
		
		Point targetPos = getTargetPositionOfWidget(objectRect);
		
		int x = (int)(targetPos.x * getPageView().getZoomFactor()) - halfWidth;
		int y = (int)(targetPos.y * getPageView().getZoomFactor()) - halfHeight;
		setPosistion(x, y);
	}
	
	Point getTargetPositionOfWidget(Rect rect) {
		if (resizeType == TYPE_LEFT)
			return new Point(rect.left, (rect.top + rect.bottom) / 2);
		else if (resizeType == TYPE_RIGHT)
			return new Point(rect.right, (rect.top + rect.bottom) / 2);
		else if (resizeType == TYPE_TOP)
			return new Point((rect.left + rect.right) / 2, rect.top);
		else if (resizeType == TYPE_BOTTOM)
			return new Point((rect.left + rect.right) / 2, rect.bottom);
		else if (resizeType == TYPE_TOP_LEFT)
			return new Point(rect.left, rect.top);
		else if (resizeType == TYPE_TOP_RIGHT)
			return new Point(rect.right, rect.top);
		else if (resizeType == TYPE_BOTTOM_LEFT)
			return new Point(rect.left, rect.bottom);
		else //if (resizeType == TYPE_BOTTOM_RIGHT)
			return new Point(rect.right, rect.bottom);
	}

	@Override
	public void refresh() {
		updatePosition();
	}

	@Override
	protected void onDrag(Point start, Point current) {
		if (referencePolygon == null) {
			referencePolygon = selObj.getCoords().clone();
		}

		int diffX = (int)((double)(current.x - start.x) / getPageView().getZoomFactor());
		int diffY = (int)((double)(current.y - start.y) / getPageView().getZoomFactor());

		final double minScalingFactor = 0.1; //Minimum of 10% 
		
		double scaleX = Math.max(getScalingFactorX(diffX), minScalingFactor);
		double scaleY = Math.max(getScalingFactorY(diffY), minScalingFactor);

		int offsetX = getOffsetX(diffX);
		int offsetY = getOffsetY(diffY);

		if (scaleX > minScalingFactor || scaleY > minScalingFactor) {
			Polygon polygon = selObj.getCoords();
			int xBoundingBox = referencePolygon.getBoundingBox().left;
			int yBoundingBox = referencePolygon.getBoundingBox().top;
			for (int i=0; i<polygon.getSize(); i++) {
				int xOld = referencePolygon.getPoint(i).x;
				int xRel = xOld - xBoundingBox;
				int yOld = referencePolygon.getPoint(i).y;
				int yRel = yOld - yBoundingBox;
				
				if (scaleX > minScalingFactor)
					polygon.getPoint(i).x = xBoundingBox + (int)((double)xRel * scaleX) + offsetX;
				if (scaleY > minScalingFactor)
					polygon.getPoint(i).y = yBoundingBox + (int)((double)yRel * scaleY) + offsetY;
			}
			polygon.setBoundingBoxOutdated();
		}
		
		pageView.refreshHoverWidgets(); //Refresh position of all widgets
		if (alwaysRefreshPageView)
			pageView.getRenderer().refresh();
	}
	
	/**
	 * Calculates the horizontal scaling factor for resizing the current page content object (in relation to the mouse movement).
	 */
	private double getScalingFactorX(int diffX) {
		if (resizeType == TYPE_TOP || resizeType == TYPE_BOTTOM)
			return 1.0;
		if (resizeType == TYPE_LEFT || resizeType == TYPE_TOP_LEFT || resizeType == TYPE_BOTTOM_LEFT)
			return (double)(referencePolygon.getBoundingBox().getWidth()-diffX) / (double)referencePolygon.getBoundingBox().getWidth();
		return (double)(referencePolygon.getBoundingBox().getWidth() + diffX) / (double)referencePolygon.getBoundingBox().getWidth();
	}

	/**
	 * Calculates the vertical scaling factor for resizing the current page content object (in relation to the mouse movement).
	 */
	private double getScalingFactorY(int diffY) {
		if (resizeType == TYPE_LEFT || resizeType == TYPE_RIGHT)
			return 1.0;
		if (resizeType == TYPE_TOP || resizeType == TYPE_TOP_LEFT || resizeType == TYPE_TOP_RIGHT)
			return (double)(referencePolygon.getBoundingBox().getHeight()-diffY) / (double)referencePolygon.getBoundingBox().getHeight();
		return (double)(referencePolygon.getBoundingBox().getHeight() + diffY) / (double)referencePolygon.getBoundingBox().getHeight();
	}
	
	/**
	 * Calculates the horizontal offset for resizing the current page content object (in relation to the mouse movement).
	 */
	private int getOffsetX(int diffX) {
		if (resizeType == TYPE_LEFT || resizeType == TYPE_TOP_LEFT || resizeType == TYPE_BOTTOM_LEFT)
			return diffX;
		return 0;
	}

	/**
	 * Calculates the vertical offset for resizing the current page content object (in relation to the mouse movement).
	 */
	private int getOffsetY(int diffY) {
		if (resizeType == TYPE_TOP || resizeType == TYPE_TOP_LEFT || resizeType == TYPE_TOP_RIGHT)
			return diffY;
		return 0;
	}

	@Override
	protected void onDragEnd(Point start, Point current) {
		referencePolygon = null;
		this.getPageView().getRenderer().refresh();
		
		//Sync to server
		pageSync.syncObjectOutline(selObj);
	}



}
