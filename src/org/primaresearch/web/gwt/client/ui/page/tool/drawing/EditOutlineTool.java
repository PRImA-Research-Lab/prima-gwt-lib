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
package org.primaresearch.web.gwt.client.ui.page.tool.drawing;

import org.primaresearch.maths.geometry.Point;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.page.PageSyncManager;
import org.primaresearch.web.gwt.client.ui.RenderStyles.RenderStyle;
import org.primaresearch.web.gwt.client.ui.page.PageScrollView;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.client.ui.page.renderer.PageRenderer;
import org.primaresearch.web.gwt.client.ui.page.renderer.RendererHelper;
import org.primaresearch.web.gwt.client.ui.page.tool.controls.ContentObjectToolbarButton;
import org.primaresearch.web.gwt.client.ui.page.tool.controls.ContentObjectToolbar;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.Image;

/**
 * Tool for adding, moving and deleting polygon points for a page content object.
 * 
 * @author Christian Clausner
 *
 */
public class EditOutlineTool extends BasePageViewTool implements SelectionListener {
	
	/** Defines how close the mouse cursor needs to be to 'catch' a polygon point. */
	private static final int POINT_NEIGHBOURHOOD = 10;
	
	//Colour for add sign and move sign 
	private static final CssColor POINT_HIGHLIGHT_LINE_COLOR = CssColor.make(0,82,117);
	private static final CssColor POINT_HIGHLIGHT_FILL_COLOR = CssColor.make(0,162,232);
	
	//Colour for delete sign
	private static final CssColor DELETE_POINT_LINE_COLOR = CssColor.make(128,0,0);
	private static final CssColor DELETE_POINT_FILL_COLOR = CssColor.make(255,0,0);
	
	private ContentObjectC contentObject;
	private Polygon polygon;
	private PageScrollView view;
	private Point currentPolygonPoint = null;
	private Point newPolygonPointCandidate = null;
	private int indexOfPointBeforeNewPolygonPoint;
	private Point referencePoint = null;
	private ContentObjectToolbarButton applyWidget;
	private ContentObjectToolbarButton cancelWidget;
	private ContentObjectToolbar toolbar;
	private Polygon originalPolygon;
	private Point mouseDownPoint = null;
	private SelectionManager selectionManager;
	private boolean deletePointsMode = false;

	/**
	 * Constructor
	 * 
	 * @param contentObject The page content object with the outline to edit
	 * @param pageView The document page view 
	 * @param selectionManager Selection manager for adding a listener
	 * @param syncManager Synchronisation manager for sending the changed outline to the server
	 */
	public EditOutlineTool(final ContentObjectC contentObject, final PageScrollView pageView, SelectionManager selectionManager,
							final PageSyncManager syncManager) {
		super();
		polygon = contentObject.getCoords();
		originalPolygon = polygon.clone();
		this.view = pageView;
		this.contentObject = contentObject;
		this.selectionManager = selectionManager;

		//Hide currently active widgets on the page view
		pageView.hideHoverWidgets();
		
		//Create a toolbar
		toolbar = new ContentObjectToolbar(selectionManager, -5);
		pageView.addHoverWidget(toolbar);

		//OK button
		applyWidget = new ContentObjectToolbarButton("img/tick.png", "Done");
		toolbar.add(applyWidget);
		applyWidget.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pageView.removeHoverWidget(toolbar);
				notifyListenersToolFinished(true);
				syncManager.syncObjectOutline(contentObject);
				pageView.showHoverWidgets();
				stopListeningForSelectionChanges();
				toolbar.dispose();
				view.getViewPanel().getElement().getStyle().setCursor(Cursor.AUTO);
			}
		});
		
		//Cancel button
		cancelWidget = new ContentObjectToolbarButton("img/cross.png", "Cancel");
		toolbar.add(cancelWidget);
		cancelWidget.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onCancel(true);
			}
		});
		
		//Separator
		Image sep = new Image("img/separator.png");
		toolbar.add(sep);
		
		//Edit modes (add/move and erase)
		final ContentObjectToolbarButton addAndMoveMode = new ContentObjectToolbarButton("img/pen.png", "Move and add points", true);
		toolbar.add(addAndMoveMode);
		addAndMoveMode.setDown(true);

		final ContentObjectToolbarButton deleteMode = new ContentObjectToolbarButton("img/eraser.png", "Delete points", true);
		toolbar.add(deleteMode);
		
		addAndMoveMode.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deletePointsMode = !deletePointsMode;
				deleteMode.setDown(deletePointsMode);
				chooseCursor();
			}
		});

		deleteMode.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deletePointsMode = !deletePointsMode;
				addAndMoveMode.setDown(!deletePointsMode);
				chooseCursor();
			}
		});
		
		toolbar.refresh(); //Update toolbar position

		view.getRenderer().refresh();
		selectionManager.addListener(this);
	}
	
	/**
	 * Sets the cursor for the page view according to the current mode.
	 */
	private void chooseCursor() {
		if (deletePointsMode)
			view.getViewPanel().getElement().getStyle().setCursor(Cursor.POINTER);
		else
			view.getViewPanel().getElement().getStyle().setCursor(Cursor.AUTO);
	}
	
	private void stopListeningForSelectionChanges() {
		selectionManager.removeListener(this);
	}
	
	private void onCancel(boolean unhideOtherToolWidgets) {
		contentObject.setCoords(originalPolygon);
		view.removeHoverWidget(toolbar);
		notifyListenersToolFinished(false);
		if (unhideOtherToolWidgets)
			view.showHoverWidgets();
		stopListeningForSelectionChanges();
		toolbar.dispose();
		view.getViewPanel().getElement().getStyle().setCursor(Cursor.AUTO);
	}
	
	@Override
	public boolean onMouseMove(MouseMoveEvent event) {

		//Mouse down?
		if (mouseDownPoint != null) {
			
			//Delete mode -> 'Hoover up' the polygon points
			if (deletePointsMode) {
				int x = view.clientToDocumentCoordsX(event.getRelativeX(view.asWidget().getElement()));
				int y = view.clientToDocumentCoordsY(event.getRelativeY(view.asWidget().getElement()));
				
				//Look for polygon point that is close by
				currentPolygonPoint = findNearestPolygonPointCloseby(x, y);
				
				if (currentPolygonPoint != null) {
					deleteCurrentPolygonPoint();
					view.getRenderer().refresh();
					refreshToolbar();
				}
			}
			//Add and move mode -> Move polygon point
			else {
				if (currentPolygonPoint != null) {
					int diffX = (int)((double)(event.getX() - mouseDownPoint.x) / view.getZoomFactor());
					int diffY = (int)((double)(event.getY() - mouseDownPoint.y) / view.getZoomFactor());
					
					currentPolygonPoint.x = referencePoint.x + diffX;
					currentPolygonPoint.y = referencePoint.y + diffY;
					
					confinePointToDocument(currentPolygonPoint);
					
					polygon.setBoundingBoxOutdated();
					refreshToolbar(); //Update icon position
				}
			}
			
			view.getRenderer().refresh();
			
			return true;  //Forbid scrolling
		} 
		//Mouse hover -> Find nearby point
		else {
			int x = view.clientToDocumentCoordsX(event.getRelativeX(view.asWidget().getElement()));
			int y = view.clientToDocumentCoordsY(event.getRelativeY(view.asWidget().getElement()));
			
			//First look for polygon point that is close by
			Point oldPoint = currentPolygonPoint;
			currentPolygonPoint = findNearestPolygonPointCloseby(x, y);

			if (oldPoint != currentPolygonPoint)
				view.getRenderer().refresh();

			//Add and move mode
			if (!deletePointsMode) {
				//Now look if we are close to a polygon line (to add a new point)
				if (currentPolygonPoint == null) {
					newPolygonPointCandidate = findNearestPointOnLines(x,y);
					
					view.getRenderer().refresh();
				}
				else if (newPolygonPointCandidate != null) {
					newPolygonPointCandidate = null;
					view.getRenderer().refresh();
				}
			}
		}		
		
		return false; //Allow scrolling
	}

	@Override
	public boolean onMouseOut(MouseOutEvent event) {
		return false;
	}

	@Override
	public boolean onMouseUp(MouseUpEvent event) {
		if (currentPolygonPoint != null) {
			referencePoint = null;
		} 
		mouseDownPoint = null;
		return true;
	}

	@Override
	public boolean onMouseDown(MouseDownEvent event) {
		//Delete points mode
		if (deletePointsMode) {
			if (currentPolygonPoint != null) {
				mouseDownPoint = new Point(event.getX(), event.getY());
				deleteCurrentPolygonPoint();
				return true; //Forbid scrolling
			}
		}
		//Add or move points mode
		else {
			//Nearby existing point?
			if (currentPolygonPoint != null) {
				mouseDownPoint = new Point(event.getX(), event.getY());
				referencePoint = new Point(currentPolygonPoint.x, currentPolygonPoint.y);
				return true; //Forbid scrolling
			}
			//have a candidate for new point?
			else if (newPolygonPointCandidate != null) {
				mouseDownPoint = new Point(event.getX(), event.getY());
				polygon.insertPoint(indexOfPointBeforeNewPolygonPoint, newPolygonPointCandidate);
				currentPolygonPoint = newPolygonPointCandidate;
				referencePoint = new Point(currentPolygonPoint.x, currentPolygonPoint.y);
				newPolygonPointCandidate = null;
				view.getRenderer().refresh();
				return true; //Forbid scrolling
			}
		}
		return false; //Allow scrolling
	}
	
	/**
	 * Deletes the currently highlighted point from the outline (if enough points left).
	 */
	private void deleteCurrentPolygonPoint() {
		if (polygon.getSize() > 3) {
			polygon.removePoint(currentPolygonPoint);
			currentPolygonPoint = null;
			view.getRenderer().refresh();
		}
	}
	
	/**
	 * Makes sure the given point is within the limits of the document page.
	 */
	private void confinePointToDocument(Point p) {
		PageLayoutC layout = view.getPageLayout();
		if (p.x < 0)
			p.x = 0;
		else if (p.x >= layout.getWidth())
			p.x = layout.getWidth()-1;
		if (p.y < 0)
			p.y = 0;
		else if (p.y >= layout.getHeight())
			p.y = layout.getHeight()-1;
	}

	@Override
	public void render(PageRenderer renderer) {
		if (!isEnabled())
			return;

		Context2d context = renderer.getContext();
		
		//Draw the selected outline in red
		RenderStyle style = new RenderStyle("rgb(255,0,0)", "transparent", 1.0);
		RendererHelper.drawPolygon(context, polygon, style, renderer.getZoomFactor(), true, false);

		//Delete mode
		if (deletePointsMode) {
			if (currentPolygonPoint != null) {
				context.setFillStyle(DELETE_POINT_FILL_COLOR);
				context.setStrokeStyle(DELETE_POINT_LINE_COLOR);
				context.setLineWidth(1.0/renderer.getZoomFactor());
				
				//Cross
				context.beginPath();
				int size = (int)(3.0 / view.getZoomFactor());
				context.moveTo(currentPolygonPoint.x-2*size, currentPolygonPoint.y-3*size);
				context.lineTo(currentPolygonPoint.x, currentPolygonPoint.y-size);
				context.lineTo(currentPolygonPoint.x+2*size, currentPolygonPoint.y-3*size);
				context.lineTo(currentPolygonPoint.x+3*size, currentPolygonPoint.y-2*size);
				context.lineTo(currentPolygonPoint.x+size, currentPolygonPoint.y);
				context.lineTo(currentPolygonPoint.x+3*size, currentPolygonPoint.y+2*size);
				context.lineTo(currentPolygonPoint.x+2*size, currentPolygonPoint.y+3*size);
				context.lineTo(currentPolygonPoint.x, currentPolygonPoint.y+size);
				context.lineTo(currentPolygonPoint.x-2*size, currentPolygonPoint.y+3*size);
				context.lineTo(currentPolygonPoint.x-3*size, currentPolygonPoint.y+2*size);
				context.lineTo(currentPolygonPoint.x-size, currentPolygonPoint.y);
				context.lineTo(currentPolygonPoint.x-3*size, currentPolygonPoint.y-2*size);
				context.lineTo(currentPolygonPoint.x-2*size, currentPolygonPoint.y-3*size);
				
				context.fill();
				context.stroke();
			}			
		}
		//Add or move mode
		else {
			//Move point
			if (currentPolygonPoint != null) {
				context.setFillStyle(POINT_HIGHLIGHT_FILL_COLOR);
				context.setStrokeStyle(POINT_HIGHLIGHT_LINE_COLOR);
				context.setLineWidth(1.0/renderer.getZoomFactor());
				
				int size = (int)(3.0 / view.getZoomFactor());
				
				//Rect in centre
				context.beginPath();
				context.rect(currentPolygonPoint.x-size, currentPolygonPoint.y-size, 2*size+1, 2*size+1);
				context.fill();
				context.stroke();
				
				//Arrows
				// Left
				context.beginPath();
				context.moveTo(currentPolygonPoint.x-2*size, currentPolygonPoint.y+size);
				context.lineTo(currentPolygonPoint.x-2*size, currentPolygonPoint.y-size);
				context.lineTo(currentPolygonPoint.x-3*size, currentPolygonPoint.y);
				context.lineTo(currentPolygonPoint.x-2*size, currentPolygonPoint.y+size);
				context.fill();
				context.stroke();
				// Right
				context.beginPath();
				context.moveTo(currentPolygonPoint.x+2*size, currentPolygonPoint.y+size);
				context.lineTo(currentPolygonPoint.x+2*size, currentPolygonPoint.y-size);
				context.lineTo(currentPolygonPoint.x+3*size, currentPolygonPoint.y);
				context.lineTo(currentPolygonPoint.x+2*size, currentPolygonPoint.y+size);
				context.fill();
				context.stroke();
				// Top
				context.beginPath();
				context.moveTo(currentPolygonPoint.x+size, currentPolygonPoint.y-2*size);
				context.lineTo(currentPolygonPoint.x-size, currentPolygonPoint.y-2*size);
				context.lineTo(currentPolygonPoint.x, currentPolygonPoint.y-3*size);
				context.lineTo(currentPolygonPoint.x+size, currentPolygonPoint.y-2*size);
				context.fill();
				context.stroke();
				// Bottom
				context.beginPath();
				context.moveTo(currentPolygonPoint.x+size, currentPolygonPoint.y+2*size);
				context.lineTo(currentPolygonPoint.x-size, currentPolygonPoint.y+2*size);
				context.lineTo(currentPolygonPoint.x, currentPolygonPoint.y+3*size);
				context.lineTo(currentPolygonPoint.x+size, currentPolygonPoint.y+2*size);
				context.fill();
				context.stroke();
			}
			
			//Add point
			if (newPolygonPointCandidate != null) {
				context.setFillStyle(POINT_HIGHLIGHT_FILL_COLOR);
				context.setStrokeStyle(POINT_HIGHLIGHT_LINE_COLOR);
				context.setLineWidth(1.0/renderer.getZoomFactor());
				
				//Plus sign
				context.beginPath();
				int size = (int)(3.0 / view.getZoomFactor());
				context.moveTo(newPolygonPointCandidate.x-size, newPolygonPointCandidate.y-3*size);
				context.lineTo(newPolygonPointCandidate.x+size, newPolygonPointCandidate.y-3*size);
				context.lineTo(newPolygonPointCandidate.x+size, newPolygonPointCandidate.y-size);
				context.lineTo(newPolygonPointCandidate.x+3*size, newPolygonPointCandidate.y-size);
				context.lineTo(newPolygonPointCandidate.x+3*size, newPolygonPointCandidate.y+size);
				context.lineTo(newPolygonPointCandidate.x+size, newPolygonPointCandidate.y+size);
				context.lineTo(newPolygonPointCandidate.x+size, newPolygonPointCandidate.y+3*size);
				context.lineTo(newPolygonPointCandidate.x-size, newPolygonPointCandidate.y+3*size);
				context.lineTo(newPolygonPointCandidate.x-size, newPolygonPointCandidate.y+size);
				context.lineTo(newPolygonPointCandidate.x-3*size, newPolygonPointCandidate.y+size);
				context.lineTo(newPolygonPointCandidate.x-3*size, newPolygonPointCandidate.y-size);
				context.lineTo(newPolygonPointCandidate.x-size, newPolygonPointCandidate.y-size);
				context.lineTo(newPolygonPointCandidate.x-size, newPolygonPointCandidate.y-3*size);
				
				context.fill();
				context.stroke();
			}
		}
	}
	
	/**
	 * Returns the closest existing polygon point that is in the neighbourhood.
	 * @param x Centre of neighbourhood
	 * @param y Centre of neighbourhood
	 * @return A point or <code>null</code>
	 */
	private Point findNearestPolygonPointCloseby(int x, int y) {
		double maxDist = POINT_NEIGHBOURHOOD/view.getZoomFactor();
		double minDist=100000.0, dist;
		int nearestPointIndex = -1;
		for (int i=0; i<polygon.getSize(); i++) {
			dist = polygon.getPoint(i).calculateDistance(x, y);
			if (dist <= minDist && dist < maxDist) {
				minDist = dist;
				nearestPointIndex = i;
			}
		}
		if (nearestPointIndex >= 0) {
			return polygon.getPoint(nearestPointIndex);
		}
		return null;
	}
	
	/**
	 * Returns the closest point on a polygon line that is in the neighbourhood.
	 * @param x Centre of neighbourhood
	 * @param y Centre of neighbourhood
	 * @return A point or <code>null</code>
	 */
	private Point findNearestPointOnLines(int x, int y) {
		double maxDist = POINT_NEIGHBOURHOOD/view.getZoomFactor();
		double minDist=100000.0, dist;
		Point p;
		Point nearest = null;
		Point l1, l2;
		for (int i=0; i<polygon.getSize(); i++) {
			l1 = polygon.getPoint(i);
			l2 = i<polygon.getSize()-1 ? polygon.getPoint(i+1) : polygon.getPoint(0);
			
			p = new Point();
			dist = new Point(x,y).calculateDistance(l1.x, l1.y, l2.x, l2.y, p);
			if (dist <= minDist && dist < maxDist) {
				minDist = dist;
				nearest = p;
				indexOfPointBeforeNewPolygonPoint = i;
			}
		}
		return nearest;
	}

	/** Updates the toolbar position */
	private void refreshToolbar() {
		toolbar.refresh();
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		onCancel(false);
	}
	
	@Override
	public void cancel() {
		onCancel(false);
	}

}
