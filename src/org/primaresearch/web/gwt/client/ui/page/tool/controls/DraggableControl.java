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

import org.primaresearch.maths.geometry.Point;
import org.primaresearch.web.gwt.client.ui.page.PageScrollView;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for draggable page view controls (e.g. for resize and move).
 * @author Christian Clausner
 *
 */
public abstract class DraggableControl implements PageViewHoverWidget, MouseDownHandler, MouseUpHandler, MouseMoveHandler {

	protected Image iconWidget;
	protected PageScrollView pageView;
	private Point mouseDownPoint = null;
	
	public DraggableControl(String imageUrl) {
		iconWidget = new Image(imageUrl);
		iconWidget.addMouseDownHandler(this);
		iconWidget.addMouseUpHandler(this);
		iconWidget.addMouseMoveHandler(this);
	}
	
	@Override
	public Widget asWidget() {
		return iconWidget;
	}

	@Override
	public void setPageView(PageScrollView view) {
		this.pageView = view;
	}

	@Override
	public PageScrollView getPageView() {
		return pageView;
	}

	@Override
	public void setPosistion(int x, int y) {
		pageView.getViewPanel().setWidgetPosition(asWidget(), x, y);
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		DOM.setCapture(iconWidget.getElement());
		Event e = DOM.eventGetCurrentEvent();
		e.preventDefault();
		
		mouseDownPoint = new Point(	event.getRelativeX(pageView.asWidget().getElement()),
									event.getRelativeY(pageView.asWidget().getElement()));
		
		event.stopPropagation();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if (mouseDownPoint != null) {
			int x = event.getRelativeX(pageView.asWidget().getElement());
			int y = event.getRelativeY(pageView.asWidget().getElement());
			
			//Limit to document dimensions
			int xDoc = pageView.clientToDocumentCoordsX(x);
			if (xDoc < 0)
				x = pageView.documentToClientCoordsX(0);
			else if (xDoc >= pageView.getPageLayout().getWidth())
				x = pageView.documentToClientCoordsX(pageView.getPageLayout().getWidth()-1);
			
			int yDoc = pageView.clientToDocumentCoordsY(y);
			if (yDoc < 0)
				y = pageView.documentToClientCoordsY(0);
			else if (yDoc >= pageView.getPageLayout().getHeight())
				y = pageView.documentToClientCoordsY(pageView.getPageLayout().getHeight()-1);
			
			Point currentPoint = new Point(x, y);
			onDrag(mouseDownPoint, currentPoint);
			event.stopPropagation();
		}
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		Point currentPoint = new Point(	event.getRelativeX(pageView.asWidget().getElement()), 
										event.getRelativeY(pageView.asWidget().getElement()));
		onDragEnd(mouseDownPoint, currentPoint);
		event.stopPropagation();
		DOM.releaseCapture(iconWidget.getElement());
		mouseDownPoint = null;
	}
	
	abstract protected void onDrag(Point start, Point current);
	
	abstract protected void onDragEnd(Point start, Point current);


}
