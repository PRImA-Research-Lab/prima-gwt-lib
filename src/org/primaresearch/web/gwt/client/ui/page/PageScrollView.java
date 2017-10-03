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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.primaresearch.maths.geometry.Dimension;
import org.primaresearch.maths.geometry.Point;
import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.page.PageSyncManager.PageSyncListener;
import org.primaresearch.web.gwt.client.ui.DocumentImageListener;
import org.primaresearch.web.gwt.client.ui.DocumentImageSource;
import org.primaresearch.web.gwt.client.ui.MouseScrollPanel;
import org.primaresearch.web.gwt.client.ui.MouseScrollPanel.MouseHandlerExtension;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager.SelectionListener;
import org.primaresearch.web.gwt.client.ui.page.renderer.PageRenderer;
import org.primaresearch.web.gwt.client.ui.page.tool.controls.PageViewHoverWidget;
import org.primaresearch.web.gwt.client.ui.page.tool.drawing.PageViewTool;
import org.primaresearch.web.gwt.client.ui.page.tool.drawing.PageViewToolListener;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.ContentObjectSync;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * View for displaying and interacting with a document page.
 * Uses a MouseScrollPanel as panel.
 * 
 * @author Christian Clausner
 *
 */
public class PageScrollView implements DocumentImageListener, PageSyncListener, 
										SelectionListener, MouseHandlerExtension, IsWidget {
	
	private MouseScrollPanel panel;
	
	private PageRenderer renderer;
	private PageLayoutC pageLayout;
	
	private double zoomFactor = 1.0;
	private double targetZoomFactor = 1.0;
	private double minZoom = 0.2;
	private double maxZoom = 2.0;
	private final double zoomChangeFactor = 1.333333;
	private final double zoomChangeFactorMouseWheel = 1.1666667;
	private Timer zoomingTimer = null;
	private Point zoomReferencePointInPanel = new Point();
	private Point zoomReferencePointInDocument = new Point();
	private boolean useSmoothZoomByDefault;
	
	private boolean enableMouseWheelScrolling;
		
	private DocumentImageSource imageSource;
	
	private SelectionManager selectionManager;
	private boolean multiSelectionEnabled = true;
	
	private Set<ZoomChangeListener> zoomListeners = new HashSet<ZoomChangeListener>();
	
	private AbsolutePanel viewPanel;
	
	private PageViewTool activeTool;
	
	private Set<PageViewHoverWidget> hoverWidgets = new HashSet<PageViewHoverWidget>();
	
	
	/**
	 * Constructor
	 * 
	 * @param pageLayout Page layout to display
	 * @param imageSource Document image source
	 * @param selectionManager Content object selection manager to listen to
	 * @param enableMouseWheelScrolling If set to <code>true</code> the mouse wheel is used for scrolling up and down, otherwise it is used for zooming
	 * @param useSmoothZoomByDefault If set to <code>true</code> the zoom is changed smoothly using a timer, otherwise the zoom is changed immediately.
	 */
	public PageScrollView(PageLayoutC pageLayout, DocumentImageSource imageSource, SelectionManager selectionManager,
							boolean enableMouseWheelScrolling, boolean useSmoothZoomByDefault) {
		panel = new MouseScrollPanel(true);
		this.enableMouseWheelScrolling = enableMouseWheelScrolling;
		this.pageLayout = pageLayout;
		this.imageSource = imageSource;
		this.selectionManager = selectionManager;
		this.useSmoothZoomByDefault = useSmoothZoomByDefault;
		selectionManager.addListener(this);
		
		Canvas canvas = Canvas.createIfSupported();
		renderer = new PageRenderer(canvas, pageLayout, selectionManager, imageSource);
		canvas.addStyleName("pageViewCanvas");
		//Initial refresh
		renderer.refresh(zoomFactor);
		
		viewPanel = new AbsolutePanel();
		panel.add(viewPanel);
		viewPanel.getElement().getStyle().setProperty("overflow", "visible");
		viewPanel.addStyleName("pageViewPanel");

		
		if (canvas != null) {
			viewPanel.add(canvas, 0, 0);
		}

		panel.addMouseWheelHandler(panel);
		panel.setMouseHandlerExtension(this);
	}
	
	/**
	 * Returns the page renderer.
	 */
	public PageRenderer getRenderer() {
		return renderer;
	}
	
	/**
	 * Returns the panel that contains the document canvas and possibly other controls. 
	 */
	public AbsolutePanel getViewPanel() {
		return viewPanel;
	}
	
	/**
	 * Returns the document page layout that is currently displayed.
	 */
	public PageLayoutC getPageLayout() {
		return pageLayout;
	}
	
	/**
	 * Adds a listener that will be notified if the zoom has been changed. 
	 */
	public void addZoomListener(ZoomChangeListener listener) {
		this.zoomListeners.add(listener);
	}
	
	/**
	 * Removes the given zoom listener.
	 */
	public void removeZoomListener(ZoomChangeListener listener) {
		this.zoomListeners.remove(listener);
	}

	@Override
	public void imageLoaded() {
		
		pageLayout.setWidth(imageSource.getOriginalImageWidth());
		pageLayout.setHeight(imageSource.getOriginalImageHeight());
		
		updateSize();
		//zoomToFitPage(); //Done externally now
		
		renderer.refresh(zoomFactor);
		
		//Zoom to 100%
		//targetZoomFactor = 1.0;
		//startSmoothZoomingTimer();
	}
	
	/**
	 * Updates view panel width and height (called after the after the zoom has been changed).
	 */
	private synchronized void updateSize() {
		viewPanel.setWidth((int)(pageLayout.getWidth() * zoomFactor)+"px");
		viewPanel.setHeight((int)(pageLayout.getHeight() * zoomFactor)+"px");
	}
	
	/**
	 * Calculates the zoom factor that is needed to fit the document page into the client window.
	 */
	private double getZoomFactorToFitPage() {
		int clientWidth = panel.getElement().getClientWidth() - 20; //allow for some margin
		int clientHeight = panel.getElement().getClientHeight() - 20;
	
		int pageWidth = pageLayout.getWidth();
		int pageHeight = pageLayout.getHeight();

		return Math.min((double)clientWidth/(double)pageWidth, (double)clientHeight/(double)pageHeight);
	}
	
	/**
	 * Zooms and pans to fit the document page into the client window and centre it.
	 */
	public void zoomToFitPage() {
		double oldZoom = zoomFactor;
		zoomFactor = getZoomFactorToFitPage();
		targetZoomFactor = zoomFactor;
		updateSize();
		
		renderer.refresh(zoomFactor);
		
		//Center view
		int clientWidth = panel.getElement().getClientWidth();
		int clientHeight = panel.getElement().getClientHeight();
	
		int pageWidth = (int)(pageLayout.getWidth() * zoomFactor);
		int pageHeight = (int)(pageLayout.getHeight() * zoomFactor);
		
		panel.scrollToPosition(pageWidth/2 - clientWidth/2, pageHeight/2 - clientHeight/2);
		
		//panel.scrollToCenter();
		centerZoomReferencePoints();
		notifyZoomListeners(zoomFactor, oldZoom);
	}
	
	/**
	 * Changes the zoom factor to fit an object of the given size.
	 * @param width Width of the object to fit
	 * @param height Height of the object to fit
	 * @param zoomOutOnly If set to <code>true</code> the zoom factor will not be increased, only decreased (if needed).
	 */
	public void zoomToFit(int width, int height, boolean zoomOutOnly) {
		int clientWidth = panel.getElement().getClientWidth();
		int clientHeight = panel.getElement().getClientHeight();
	
		double z = Math.min((double)clientWidth/(double)width, (double)clientHeight/(double)height);
		
		if (z < zoomFactor || !zoomOutOnly) {
			centerZoomReferencePoints();
			double oldZoom = zoomFactor;
			zoomFactor = z;
			targetZoomFactor = zoomFactor;
			updateSize();

			renderer.refresh(zoomFactor);
			alignZoomReferencePoints();
			notifyZoomListeners(z, oldZoom);
		}
	}

	@Override
	public boolean onMouseWheel(MouseWheelEvent event) {
		//Zoom
		if (event.isControlKeyDown()  
				|| (enableMouseWheelScrolling && event.isShiftKeyDown())
				|| (!enableMouseWheelScrolling && !event.isShiftKeyDown())) {
			//This does not work for Chrome
			Event e = DOM.eventGetCurrentEvent();
			e.preventDefault();
			event.stopPropagation();
			
			setZoomReferencePoints(event.getRelativeX(panel.getElement()), event.getRelativeY(panel.getElement()));

			int delta = event.getDeltaY();
			
			if (delta == 0)
				delta = workaroundEventGetMouseWheelVelocityY(event.getNativeEvent());

			if (delta < 0) {
				targetZoomFactor *= zoomChangeFactorMouseWheel;
				if (targetZoomFactor > maxZoom)
					targetZoomFactor = maxZoom;
			} else if (delta > 0) {
				targetZoomFactor /= zoomChangeFactorMouseWheel;
				if (targetZoomFactor < minZoom)
					targetZoomFactor = minZoom;
			}
			
			if (useSmoothZoomByDefault)
				startSmoothZoomingTimer();
			else 
				changeZoom(targetZoomFactor); 
			return true;
		}
		//Scroll
		else {
			return false;
		}
	}
	
	private static native int workaroundEventGetMouseWheelVelocityY(NativeEvent evt) /*-{
	  if (typeof evt.wheelDelta == "undefined") {
	    return 0;
	  }
	  return Math.round(-evt.wheelDelta / 40) || 0;
	}-*/;
	
	@Override
	public void postMouseWheel(MouseWheelEvent event) {
		if (event.isControlKeyDown() || !enableMouseWheelScrolling) {
		} else {
			centerZoomReferencePoints();
		}
	}
	
	@Override
	public void postMouseOver(MouseOverEvent event){
	}

	/**
	 * Increases the zoom factor using the default zoom behaviour (smooth or immediate).
	 */
	public void zoomIn() {
		zoomIn(useSmoothZoomByDefault);
	}
	
	/**
	 * Increases the zoom factor using custom zoom behaviour (smooth or immediate).
	 */
	public void zoomIn(boolean smooth) {
		centerZoomReferencePoints();

		targetZoomFactor *= zoomChangeFactor;
		if (targetZoomFactor > maxZoom)
			targetZoomFactor = maxZoom;
		
		if (smooth) 
			startSmoothZoomingTimer();
		else 
			changeZoom(targetZoomFactor); 
	}

	/**
	 * Decreases the zoom factor using the default zoom behaviour (smooth or immediate).
	 */
	public void zoomOut() {
		zoomOut(useSmoothZoomByDefault);
	}
	
	/**
	 * Decreases the zoom factor using custom zoom behaviour (smooth or immediate).
	 */
	public void zoomOut(boolean smooth) {
		centerZoomReferencePoints();
		targetZoomFactor /= zoomChangeFactor;
		if (targetZoomFactor < minZoom)
			targetZoomFactor = minZoom;
		
		if (smooth) 
			startSmoothZoomingTimer();
		else 
			changeZoom(targetZoomFactor); 
	}
	
	/**
	 * Changes the zoom factor immediately.
	 * @param zoom New factor
	 */
	private void changeZoom(double zoom) {
		double oldZoom = zoomFactor;
		zoomFactor = targetZoomFactor = zoom; 
		updateSize();
		renderer.refresh(zoomFactor);
		alignZoomReferencePoints();
		notifyZoomListeners(zoomFactor, oldZoom);
	}
	
	/**
	 * Sets the zoom factor to 1.0 using smooth zooming.
	 */
	public void zoomTo100Percent() {
		centerZoomReferencePoints();
		targetZoomFactor = 1.0;
		startSmoothZoomingTimer();
	}
	
	/**
	 * Starts smooth zooming.
	 */
	private void startSmoothZoomingTimer() {
		if (zoomingTimer == null) {
			zoomingTimer = new Timer() {
				@Override
				public void run() {
					double oldZoomFactor = zoomFactor;
					if (zoomFactor < targetZoomFactor-0.01)
						zoomFactor += (targetZoomFactor - zoomFactor)/3 ;
					else if (zoomFactor > targetZoomFactor+0.01)
						zoomFactor -= (zoomFactor - targetZoomFactor)/3;
					else {
						zoomFactor = targetZoomFactor;
						this.cancel();
					}
					updateSize();
					renderer.refresh(zoomFactor);
					alignZoomReferencePoints();
					if (zoomFactor != oldZoomFactor)
						notifyZoomListeners(zoomFactor, oldZoomFactor);
				}
			};
		}
		zoomingTimer.scheduleRepeating(30);
	}
	
	/**
	 * Sets the zoom reference points to the centre of the client window. 
	 */
	private void centerZoomReferencePoints() {
		zoomReferencePointInPanel = new Point(	panel.getElement().getClientWidth()/2, 
												panel.getElement().getClientHeight()/2);
		
		zoomReferencePointInDocument.x = clientToDocumentCoordsX(zoomReferencePointInPanel.x);
		zoomReferencePointInDocument.y = clientToDocumentCoordsY(zoomReferencePointInPanel.y);
	}
	
	/**
	 * Sets the zoom reference points to a specific position within the client window. 
	 */
	private void setZoomReferencePoints(int clientX, int clientY) {
		zoomReferencePointInPanel = new Point(clientX, clientY);

		zoomReferencePointInDocument.x = clientToDocumentCoordsX(clientX);
		zoomReferencePointInDocument.y = clientToDocumentCoordsY(clientY);
	}
	
	/**
	 * Converts the given screen x coordinate (relative to the panel) to a document page coordinate.
	 */
	public int clientToDocumentCoordsX(int clientX) {
		return (int)(((double)(clientX + panel.getScrollPosition().x)) / zoomFactor);
	}

	/**
	 * Converts the given screen y coordinate (relative to the panel) to a document page coordinate.
	 */
	public int clientToDocumentCoordsY(int clientY) {
		return (int)(((double)(clientY + panel.getScrollPosition().y)) / zoomFactor);
	}
	
	/**
	 * Converts the given document x coordinate to a screen coordinate (relative to the panel).
	 */
	public int documentToClientCoordsX(int docX) {
		return (int)((docX * zoomFactor)-panel.getScrollPosition().x);
	}

	/**
	 * Converts the given document y coordinate to a screen coordinate (relative to the panel).
	 */
	public int documentToClientCoordsY(int docY) {
		return (int)((docY * zoomFactor)-panel.getScrollPosition().y);
	}

	/**
	 * Scrolls the panel so that the zoom reference points in screen and document coordinates are aligned.
	 */
	private void alignZoomReferencePoints() {
		//Calculate the offset of the reference points
		int dx = (int)((clientToDocumentCoordsX(zoomReferencePointInPanel.x) - zoomReferencePointInDocument.x)*zoomFactor);
		int dy = (int)((clientToDocumentCoordsY(zoomReferencePointInPanel.y) - zoomReferencePointInDocument.y)*zoomFactor);

		panel.scroll(dx, dy);
	}

	@Override
	public void contentLoaded(String contentType) {
		renderer.setPageContentToRender(contentType);
		renderer.refresh(zoomFactor);
	}
	
	@Override
	public boolean onMouseMove(MouseMoveEvent event) {
		if (activeTool != null && activeTool.onMouseMove(event)) 
			return true;
		
		//super.onMouseMove(event);
		return false;
	}

	@Override
	public void postMouseMove(MouseMoveEvent event){
		if (activeTool != null && activeTool.onMouseMove(event)) 
			return;
		
		if (pageLayout != null && !panel.isAutoScrolling()) {
			//Handle mouse hover
			int x = clientToDocumentCoordsX(event.getRelativeX(panel.getElement()));
			int y = clientToDocumentCoordsY(event.getRelativeY(panel.getElement()));
			
			ContentObjectC obj = pageLayout.getObjectAt(x, y, renderer.getPageContentToRender());
			
			renderer.highlightContentObject(obj != null ? obj.getId() : null);
		}
	}

	@Override
	public boolean onMouseOut(MouseOutEvent event) {
		if (activeTool != null && activeTool.onMouseOut(event)) 
			return true;
		return false;
	}

	@Override
	public void postMouseOut(MouseOutEvent event) {
		if (activeTool != null && activeTool.onMouseOut(event)) 
			return;
		renderer.highlightContentObject(null);
	}

	@Override
	public boolean onMouseDown(MouseDownEvent event) {
		if (activeTool != null && activeTool.onMouseDown(event)) 
			return true;
		return false;
	}
	
	@Override
	public void postMouseDown(MouseDownEvent event){
	}

	@Override
	public boolean onMouseUp(MouseUpEvent event) {
		if (activeTool != null && activeTool.onMouseUp(event)) 
			return true;
		
		if (!panel.isDragged()) {
			//Selection handling
			int x = clientToDocumentCoordsX(event.getRelativeX(panel.getElement()));
			int y = clientToDocumentCoordsY(event.getRelativeY(panel.getElement()));
			
			ContentObjectC obj = pageLayout.getObjectAt(x, y, renderer.getPageContentToRender());
	
			if (multiSelectionEnabled && (event.isControlKeyDown() || event.isShiftKeyDown())) { //CTRL or SHIFT -> Toggle
				if (obj != null) {
					selectionManager.toggleSelection(obj);
				}
			}
			else { //No CTRL or SHIFT or no multi-selection enabled -> Single selection
				if (obj == null)
					selectionManager.clearSelection();
				else 
					selectionManager.setSelection(obj);
			}
		}		
		
		//Scrolling
		//super.onMouseUp(event);
		return false;
	}

	@Override
	public void postMouseUp(MouseUpEvent event){
		//if (activeTool != null && activeTool.onMouseUp(event, panel.isDragged())) 
		//	return;
	}

	@Override
	public void selectionChanged(SelectionManager manager) {
		renderer.refresh(zoomFactor);
	}

	/**
	 * Returns <code>true</code> if the view allows the selection of multiple content objects.
	 */
	public boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

	/**
	 * Enables or disables multiple selection.
	 * @param multiSelectionEnabled Set to <code>true</code> to allows the selection of multiple content objects.
	 */
	public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
		this.multiSelectionEnabled = multiSelectionEnabled;
	}
	
	/**
	 * Centres the specified rectangle in the panel. Use document page coordinates. 
	 */
	public void centerRectangle(int left, int top, int right, int bottom, boolean smoothScrolling) {
		int rectWidthInClient = (int)((right-left+1) * zoomFactor);
		int rectHeightInClient = (int)((bottom-top+1) * zoomFactor);
		int clientWidth = panel.getElement().getClientWidth();
		int clientHeight = panel.getElement().getClientHeight();
		
		int posXInClient = 0;
		int posYInClient = 0;
		
		//Calculate centre
		if (rectWidthInClient < clientWidth)
			posXInClient = (clientWidth-rectWidthInClient)/2;
		if (rectHeightInClient < clientHeight)
			posYInClient = (clientHeight-rectHeightInClient)/2;
		
		//Scroll
		int dx = (int)((clientToDocumentCoordsX(posXInClient) - left)*zoomFactor);
		int dy = (int)((clientToDocumentCoordsY(posYInClient) - top)*zoomFactor);
		panel.scroll(dx, dy, smoothScrolling);
		
		centerZoomReferencePoints();
	}
	
	/**
	 * Returns the given rectangle in client (panel) coordinates.
	 * @param rectInPageCoordinates The rectangle in document page coordiantes.
	 */
	public Rect translateToClientCoordinates(Rect rectInPageCoordinates) {
		return new Rect(	documentToClientCoordsX(rectInPageCoordinates.left),
							documentToClientCoordsY(rectInPageCoordinates.top),
							documentToClientCoordsX(rectInPageCoordinates.right),
							documentToClientCoordsY(rectInPageCoordinates.bottom));
	}

	/**
	 * Returns the current zoom factor.
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}
	
	/**
	 * Notifies all zoom change listeners that the zoom has changed.
	 */
	public void notifyZoomListeners(double newZoom, double oldZoom) {
		for (Iterator<ZoomChangeListener> it=zoomListeners.iterator(); it.hasNext(); ) {
			it.next().zoomChanged(newZoom, oldZoom, Math.abs(newZoom - minZoom) < 0.001, Math.abs(newZoom - maxZoom) < 0.001);
		}
		
		refreshHoverWidgets();
	}
	
	


	//@Override
	//public void metaDataLoaded() {
	//}

	@Override
	public void pageIdLoaded(String id) {
	}


	
	public void setTool(PageViewTool tool) {
		if (activeTool != null) {
			activeTool.cancel();
		}
		
		activeTool = tool;
		renderer.addPlugin(tool);
		activeTool.addListener(new PageViewToolListener() {
			@Override
			public void onToolFinished(PageViewTool tool, boolean success) {
				activeTool = null;
				renderer.removePlugin(tool);
			}
		});
	}

	@Override
	public boolean onMouseOver(MouseOverEvent event) {
		return false;
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	@Override
	public void contentObjectAdded(ContentObjectSync syncObj, ContentObjectC localObj) {
	}
	
	/**
	 * Adds a tool widget that hovers over the document page.
	 */
	public void addHoverWidget(PageViewHoverWidget icon) {
		getViewPanel().add(icon, 0, 0);
		hoverWidgets.add(icon);
		icon.setPageView(this);
	}
	
	/**
	 * Removes a tool widget that hovers over the document page.
	 */
	public void removeHoverWidget(PageViewHoverWidget icon) {
		getViewPanel().remove(icon);
		hoverWidgets.remove(icon);
	}
	
	/**
	 * Refreshes all tool widgets that hovers over the document page.
	 */
	public void refreshHoverWidgets() {
		for (Iterator<PageViewHoverWidget> it = hoverWidgets.iterator(); it.hasNext(); ) {
			it.next().refresh();
		}
	}
	
	/**
	 * Makes all tool widgets that hovers over the document page visible.
	 */
	public void showHoverWidgets() {
		for (Iterator<PageViewHoverWidget> it = hoverWidgets.iterator(); it.hasNext(); ) {
			PageViewHoverWidget w = it.next();
			w.asWidget().setVisible(true);
			w.refresh();
		}
	}
	
	/**
	 * Hides all tool widgets that hovers over the document page.
	 */
	public void hideHoverWidgets() {
		for (Iterator<PageViewHoverWidget> it = hoverWidgets.iterator(); it.hasNext(); ) {
			it.next().asWidget().setVisible(false);
		}
	}

	@Override
	public void textContentSynchronized(ContentObjectC syncObj) {
	}

	@Override
	public void objectOutlineSynchronized(ContentObjectC object) {
	}

	@Override
	public void contentObjectDeleted(ContentObjectC object) {
	}
	
	public MouseScrollPanel getScrollPanel() {
		return panel;
	}

	@Override
	public void pageFileSaved() {
	}


	/**
	 * Interface for zoom change listeners.
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static interface ZoomChangeListener {
		/** Called when the zoom of the page view has changed. */
		public void zoomChanged(double newZoomFactor, double oldZoomFactor, boolean isMinZoom, boolean isMaxZoom);
	}


	@Override
	public void regionTypeSynchronized(ContentObjectC object, ArrayList<String> toDelete) {
	}

	public double getMinZoom() {
		return minZoom;
	}

	public void setMinZoom(double minZoom) {
		this.minZoom = minZoom;
	}

	public double getMaxZoom() {
		return maxZoom;
	}

	public void setMaxZoom(double maxZoom) {
		this.maxZoom = maxZoom;
	}

	@Override
	public void changesReverted() {
	}

	@Override
	public void contentLoadingFailed(String contentType, Throwable caught) {
	}

	@Override
	public void pageIdLoadingFailed(Throwable caught) {
	}

	@Override
	public void contentObjectAddingFailed(ContentObjectC object,
			Throwable caught) {
	}

	@Override
	public void contentObjectDeletionFailed(ContentObjectC object,
			Throwable caught) {
	}

	@Override
	public void textContentSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void regionTypeSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void objectOutlineSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void pageFileSaveFailed(Throwable caught) {
	}

	@Override
	public void revertChangesFailed(Throwable caught) {
	}

	@Override
	public void attributeSynchronized(ContentObjectC object) {
	}

	@Override
	public void attributeSyncFailed(ContentObjectC object, Throwable caught) {
	}

	@Override
	public void readingOrderLoaded() {
	}

	@Override
	public void readingOrderLoadingFailed(Throwable caught) {
	}

	@Override
	public void pageSizeReceived(Dimension pageSize) {
	}

	@Override
	public void getPageSizeFailed(Throwable caught) {
	}


}
