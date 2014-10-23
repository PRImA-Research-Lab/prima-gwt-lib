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
package org.primaresearch.web.gwt.client.ui.page.tool.drawing;

import org.primaresearch.maths.geometry.Point;
import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.web.gwt.client.ui.page.PageScrollView;
import org.primaresearch.web.gwt.client.ui.page.renderer.PageRenderer;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

/**
 * Tool that allows drawing a rectangle on the document.
 * 
 * @author Christian Clausner
 *
 */
public class RectangleTool extends BasePageViewTool implements PageViewTool {

	Point p1 = null;
	Point p2 = null;
	PageScrollView view;

	/**
	 * Constructor
	 * @param view The document page view.
	 */
	public RectangleTool(PageScrollView view) {
		this.view = view;
		//Change cursor
		view.getViewPanel().getElement().getStyle().setCursor(Cursor.CROSSHAIR);
	}
	
	/**
	 * Returns the rectangle that has been drawn by the user.
	 * @return A rectangle or <code>null</code>
	 */
	public Rect getRect() {
		if (p1 == null)
			return null;
		int x1 = Math.min(p1.x, p2.x);
		int x2 = Math.max(p1.x, p2.x);
		int y1 = Math.min(p1.y, p2.y);
		int y2 = Math.max(p1.y, p2.y);
		return new Rect(x1,y1,x2,y2);
	}
	
	@Override
	public void render(PageRenderer renderer) {
		if (p1 == null || !isEnabled())
			return;
		Context2d context = renderer.getContext();

		//Blue rectangle
		context.setStrokeStyle(CssColor.make(0, 128, 255));
		context.setLineWidth(1.0/renderer.getZoomFactor());
		
		int x1 = Math.min(p1.x, p2.x);
		int x2 = Math.max(p1.x, p2.x);
		int y1 = Math.min(p1.y, p2.y);
		int y2 = Math.max(p1.y, p2.y);
		
		context.beginPath();
		context.rect(x1, y1, x2-x1+1, y2-y1+1);
		context.stroke();
	}

	@Override
	public boolean onMouseMove(MouseMoveEvent event) {
		//Dragging?
		if (p1 != null) {
			int x = view.clientToDocumentCoordsX(event.getRelativeX(view.asWidget().getElement()));
			int y = view.clientToDocumentCoordsY(event.getRelativeY(view.asWidget().getElement()));
			p2.x = x;
			p2.y = y;
			view.getRenderer().refresh();
			return true; //Forbid scrolling
		}
		return false; //Allow scrolling
	}

	@Override
	public boolean onMouseOut(MouseOutEvent event) {
		if (p1 != null) {
			enable(false);
			p1 = p2 = null;
			notifyListenersToolFinished(false);
			return true;
		}
		return false;
	}

	@Override
	public boolean onMouseUp(MouseUpEvent event) {
		if (p1 != null) {
			enable(false);
			notifyListenersToolFinished(true);
			p1 = p2 = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean onMouseDown(MouseDownEvent event) {
		if (p1 == null) {
			//Start dragging
			int x = view.clientToDocumentCoordsX(event.getRelativeX(view.asWidget().getElement()));
			int y = view.clientToDocumentCoordsY(event.getRelativeY(view.asWidget().getElement()));
			p1 = new Point(x, y);
			p2 = new Point(x, y);
			return true; //Forbid scrolling
		}
		return false; //Allow scrolling
	}

	@Override
	protected void notifyListenersToolFinished(boolean success) {
		view.getViewPanel().getElement().getStyle().setCursor(Cursor.AUTO);
		super.notifyListenersToolFinished(success);
	}

}
