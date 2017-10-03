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
package org.primaresearch.web.gwt.client.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.primaresearch.maths.geometry.Point;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Scroll panel handling mouse events to enable panning by dragging the mouse.<br>
 * <br>
 * The first widget that is added to the panel is used for the scrolling.
 * 
 * @author Christian Clausner
 *
 */
public class MouseScrollPanel extends AbsolutePanel implements HasAllMouseHandlers, MouseWheelHandler, 
																MouseOverHandler, MouseOutHandler, 
																MouseMoveHandler, MouseUpHandler, 
																MouseDownHandler
															{
	/** Values close to 1 mean slow deceleration; higher values mean fast deceleration. Reasonable range: (1.1,5) */
	private static double scrollingDecelerationFactor = 1.5;
	
	private Point mouseDownPoint = null;
	
	private Point scrollWidgetBasePosition;
	
	//Mouse move history
	private Point 	mouseMovePoint1 = null;
	private long 	mouseDownTime1;
	private Point 	mouseMovePoint2 = null;
	private long 	mouseDownTime2;
	
	//Auto scroll
	private double 	scrollingSpeedX;
	private double 	scrollingSpeedY;
	private Timer 	autoScrollTimer = null;
	private boolean isAutoScrolling = false;
	private Set<ScrollListener> scrollListeners = new HashSet<ScrollListener>();
	
	private double targetScrollX = Double.MIN_VALUE;
	private double targetScrollY = Double.MIN_VALUE;
	
	private boolean enableMouseWheelScrolling;
	
	private boolean isDragged = false;
	
	private MouseHandlerExtension mouseHandlerExtention = null;
	

	/**
	 * Constructor
	 * @param enableMouseWheelScrolling Set to <code>true</code> to enable scrolling up/down via mouse wheel
	 */
	public MouseScrollPanel(boolean enableMouseWheelScrolling) {
		super();
		this.enableMouseWheelScrolling = enableMouseWheelScrolling;
		this.addMouseWheelHandler(this);
		this.addMouseOverHandler(this);
		this.addMouseOutHandler(this);
		this.addMouseMoveHandler(this);
		this.addMouseUpHandler(this);
		this.addMouseDownHandler(this);
		getElement().getStyle().setProperty("overflow", "visible");
	}
	
	public void setMouseHandlerExtension(MouseHandlerExtension mouseHandlerExtention) {
		this.mouseHandlerExtention = mouseHandlerExtention;
	}
	
	public void addScrollListener(ScrollListener listener) {
		this.scrollListeners.add(listener);
	}
	
	public void removeScrollListener(ScrollListener listener) {
		this.scrollListeners.remove(listener);
	}
	
	@Override
	public final void onMouseWheel(MouseWheelEvent event) {
		boolean handle = true;
		if (mouseHandlerExtention != null) 
			handle = !mouseHandlerExtention.onMouseWheel(event);
		
		if (handle) {
			if (autoScrollTimer != null) 
				autoScrollTimer.cancel();
			
			if (!enableMouseWheelScrolling)
				return;
			
			//Ctrl + Mouse wheel
			if (event.isControlKeyDown()) {
				//Handled somewhere else
			}
			//Scrolling
			else {		
				int dy = (int)confineVertically(-10*event.getDeltaY());
				if (dy == 0)
					dy = workaroundEventGetMouseWheelVelocityY(event.getNativeEvent());
				if (dy != 0) {
					for (int i=0; i<this.getWidgetCount(); i++) {
						Widget widget = this.getWidget(i);
						if (widget != null && mouseDownPoint == null) {
							int x = this.getWidgetLeft(widget);
							int y = this.getWidgetTop(widget);
							this.setWidgetPosition(widget, x, y+dy-1);
						}
					}
				}
			}
		}
		
		if (mouseHandlerExtention != null) 
			mouseHandlerExtention.postMouseWheel(event);
	}
	
	private static native int workaroundEventGetMouseWheelVelocityY(NativeEvent evt) /*-{
	  if (typeof evt.wheelDelta == "undefined") {
	    return 0;
	  }
	  return Math.round(evt.wheelDelta / 40) || 0;
	}-*/;

	@Override
	public void onMouseOver(MouseOverEvent event) {
		boolean handle = true;
		if (mouseHandlerExtention != null) 
			handle = !mouseHandlerExtention.onMouseOver(event);
		
		if (handle) {
		}
		
		if (mouseHandlerExtention != null) 
			mouseHandlerExtention.postMouseOver(event);
	}
	
	@Override
	public final void onMouseOut(MouseOutEvent event) {
		boolean handle = true;
		if (mouseHandlerExtention != null) 
			handle = !mouseHandlerExtention.onMouseOut(event);
		
		if (handle) {
		}
		
		isDragged = false;
		
		if (mouseHandlerExtention != null) 
			mouseHandlerExtention.postMouseOut(event);		
		
		//mouseDownPoint = null;
		//event.stopPropagation();
	}
	
	@Override
	public final void onMouseMove(MouseMoveEvent event) {
		Event e = DOM.eventGetCurrentEvent();
		e.preventDefault();

		boolean handle = true;
		if (mouseHandlerExtention != null) 
			handle = !mouseHandlerExtention.onMouseMove(event);
		
		if (mouseDownPoint != null) {
			mouseMovePoint2 = mouseMovePoint1;
			mouseDownTime2 = mouseDownTime1;
			mouseMovePoint1 = new Point(event.getX(), event.getY());
			mouseDownTime1 = new Date().getTime();
			
			if (handle) {
				Point refPos = getScrollWidgetPos();
				
				int mouseDx = event.getClientX() - mouseDownPoint.x;
				int mouseDy = event.getClientY() - mouseDownPoint.y;
				
				int dx = (scrollWidgetBasePosition.x + mouseDx) - refPos.x;
				int dy = (scrollWidgetBasePosition.y + mouseDy) - refPos.y;
	
				moveScrollWidget(dx, dy);
			}
			
			event.stopPropagation();

			isDragged = isDragged || dist(event.getClientX(), event.getClientY(), mouseDownPoint.x, mouseDownPoint.y) > 5.0;
		}
		
		if (mouseHandlerExtention != null) 
			mouseHandlerExtention.postMouseMove(event);		
	}
	
	private Point getScrollWidgetPos() {
		if (this.getWidgetCount() > 0)
			return new Point(this.getWidget(0).getElement().getOffsetLeft(), this.getWidget(0).getElement().getOffsetTop());
		return new Point();
	}
	
	private void moveScrollWidget(double dx, double dy) {
		dx = confineHorizontally(dx);
		dy = confineVertically(dy);
		//Logger logger = Logger.getLogger("CrowdSourcing");
	    //logger.log(Level.INFO, "autoScroll: "+dx+", "+dy); 
		if (this.getWidgetCount() > 0) {
			Widget widget = this.getWidget(0);
			if (widget != null) {
				int x = widget.getElement().getOffsetLeft() + (int)(dx+0.5);
				int y = widget.getElement().getOffsetTop() + (int)(dy+0.5);
				//this.setWidgetPosition(widget, x, y);
				widget.getElement().getStyle().setPosition(Position.ABSOLUTE);
				widget.getElement().getStyle().setLeft(x, Style.Unit.PX);
				widget.getElement().getStyle().setTop(y, Style.Unit.PX);
			}
		}
		notifyScrollPositionChanged();
	}
	
	private double confineHorizontally(double dx) {
		double confined = dx;
		if (this.getWidgetCount() <= 0)
			return confined;
		int panelWidth = this.getElement().getClientWidth();
		Widget refWidget = this.getWidget(0);
		int refWidgetWidth = refWidget.getElement().getClientWidth();

		//Maximum visible background around the reference widget
		int padding = Math.max(panelWidth - refWidgetWidth/2, panelWidth/2); 
		
		double refLeft = refWidget.getElement().getOffsetLeft();

		//Widget moving to the right
		if (dx > 0) {
			//Check if out of bounds
			if (refLeft + dx > padding) {
				return padding - refLeft;
				//return Math.max(0.0, padding - refLeft);
			}
		}
		//Widget moving to the left
		else if (dx < 0) {
			double refRight = refLeft + refWidgetWidth;
			//Check if out of bounds
			if (refRight + dx < panelWidth-padding) {
				return  panelWidth-padding-refRight;
				//return  Math.min(0.0, panelWidth-padding-refRight);
			}
		}
		return confined;
	}

	private double confineVertically(double dy) {
		double confined = dy;
		if (this.getWidgetCount() <= 0)
			return confined;
		int panelHeight = this.getElement().getClientHeight();
		Widget refWidget = this.getWidget(0);
		int refWidgetHeight = refWidget.getElement().getClientHeight();

		//Maximum visible background around the reference widget
		int padding = Math.max(panelHeight - refWidgetHeight/2, panelHeight/2);
		
		double refTop = refWidget.getElement().getOffsetTop();

		//Widget moving to the bottom
		if (dy > 0) {
			//Check if out of bounds
			if (refTop + dy > padding) {
				return padding - refTop;
				//return Math.max(0.0, padding - refTop);
			}
		}
		//Widget moving to the top
		else if (dy < 0) {
			double refBottom = refTop + refWidgetHeight;
			//Check if out of bounds
			if (refBottom + dy < panelHeight-padding) {
				return panelHeight-padding - refBottom;
				//return Math.min(0.0, panelHeight-padding - refBottom);
			}
		}
		return confined;
	}

	@Override
	public final void onMouseUp(MouseUpEvent event) {
		boolean handle = true;
		if (mouseHandlerExtention != null) 
			handle = !mouseHandlerExtention.onMouseUp(event);
		
		if (handle) {
			//Calculate speed
			if (mouseMovePoint2 != null) {
				long now = new Date().getTime();
				int timePassed = (int)(now - mouseDownTime2);
				//double distance = dist(event.getX(), event.getY(), (int)mouseMovePoint2.getX(), (int)mouseMovePoint2.getY());
				double moveX = -mouseMovePoint2.x + event.getX();
				double moveY = -mouseMovePoint2.y + event.getY();
				scrollingSpeedX = 0.0;
				scrollingSpeedY = 0.0;
				if (timePassed > 0) {
					scrollingSpeedX = moveX / (double)timePassed;
					scrollingSpeedY = moveY / (double)timePassed;
				}
				if (Math.abs(scrollingSpeedX) > 0.0 || Math.abs(scrollingSpeedY) > 0.0) {
				    // Setup timer to refresh 
					if (autoScrollTimer != null)
						autoScrollTimer.cancel();
					startAutoScroll(); //inertia mode
				}
			}
		}

		isDragged = false;
		
		mouseMovePoint1 = null;
		mouseMovePoint2 = null;
		mouseDownPoint = null;
		event.stopPropagation();
		DOM.releaseCapture(this.getElement());
		
		if (mouseHandlerExtention != null) 
			mouseHandlerExtention.postMouseUp(event);		
	}
	
	private void startAutoScroll() {
		final int refreshTimespan = 30;
		autoScrollTimer = new Timer() {
	    	@Override
	    	public void run() {
	    		synchronized (this) {
	    			isAutoScrolling = true;
	    			autoScroll(refreshTimespan, this);
	    		}
	    	}
	    	@Override
	    	public void cancel() {
	    		super.cancel();
	    		synchronized (this) {
		    		if (isAutoScrolling) {
						targetScrollX = Double.MIN_VALUE;
						targetScrollY = Double.MIN_VALUE;
						notifyAutoScrollFinished();
		    		}
		    		isAutoScrolling = false;
	    		}
	    	}
	    };
	    autoScrollTimer.scheduleRepeating(refreshTimespan);
	}
	
	public boolean isDragged() {
		return isDragged;
	}
	
	/**
	 * Returns true if scrolling at the moment.
	 */
	public boolean isAutoScrolling() {
		return isAutoScrolling;
	}
	
	private void autoScroll(int refreshTimespan, Timer refreshTimer) {
		
		//There are two different modes:
		//  1: Inertia scrolling: Slowing down after mouse drag
		//  2: Smooth moving to target position
		
		//Mode 1 (inertia)
		if (targetScrollX == Double.MIN_VALUE) {
			double dx = scrollingSpeedX * refreshTimespan;
			double dy = scrollingSpeedY * refreshTimespan;
			
			moveScrollWidget(dx, dy);
			
			//Slow down
			scrollingSpeedX /= scrollingDecelerationFactor;
			scrollingSpeedY /= scrollingDecelerationFactor;
			
			//Stop timer?
			if (Math.abs(scrollingSpeedX) < 0.0001 && Math.abs(scrollingSpeedY) < 0.0001) {
				refreshTimer.cancel();
			}
		}
		//Mode 2 (smooth moving to target)
		else {
			boolean reachedTargetX = false;
			boolean reachedTargetY = false;
			
			Point refPos = getScrollWidgetPos();
			
			double dx = targetScrollX - refPos.x;
			double dy = targetScrollY - refPos.y;
			
			//X
			if (Math.abs(dx) <= 4.0)
				reachedTargetX = true;
			//Y
			if (Math.abs(dy) <= 4.0)
				reachedTargetY = true;
			
			//Finished?
			if (reachedTargetX && reachedTargetY) {
				refreshTimer.cancel();
			}
			else {
				moveScrollWidget(dx/3.0, dy/3.0);
			}
		}
	}
	
	//Distance between two points
	private double dist(int x1, int y1, int x2, int y2) {
		return Math.sqrt(((double)((x2 - x1)*(x2 - x1))) + ((double)((y2 - y1)*(y2 - y1))));
	}
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		DOM.setCapture(this.getElement());
		Event e = DOM.eventGetCurrentEvent();
		e.preventDefault();
		
		boolean handle = true;
		if (mouseHandlerExtention != null) 
			handle = !mouseHandlerExtention.onMouseDown(event);

		if (autoScrollTimer != null)
			autoScrollTimer.cancel();

		if (handle) {
			mouseDownPoint = new Point(event.getClientX(), event.getClientY());
			//Get widget positions
			scrollWidgetBasePosition = getScrollWidgetPos();
		}

		//for (int i=0; i<this.getWidgetCount(); i++) {
		//	Widget widget = this.getWidget(i);
		//	DOM.setIntStyleAttribute(widget.getElement(), "zIndex", i);
		//}
		
		event.stopPropagation();

		if (mouseHandlerExtention != null) 
			mouseHandlerExtention.postMouseDown(event);
	}
	
	/**
	 * Returns the view rectangle position in relation to the first child widget.
	 */
	public Point getScrollPosition() {
		if (this.getWidgetCount() > 0) {
			Point refPos = getScrollWidgetPos();
			return new Point(-refPos.x, -refPos.y);
		}
		return new Point();
	}
	
	/**
	 * Scrolls the view rectangle to a position relative to the first child widget.  
	 */
	public void scrollToPosition(int x, int y) {
		if (autoScrollTimer != null) 
			autoScrollTimer.cancel();
		
		if (this.getWidgetCount() > 0) {
			Widget widget = this.getWidget(0);
			int dx = -this.getWidgetLeft(widget) - x;
			int dy = -this.getWidgetTop(widget) - y;
			moveScrollWidget(dx, dy);
		}		
	}

	/**
	 * Scrolls relatively to the current position.
	 */
	public void scroll(int dx, int dy) {
		scroll(dx, dy, false);
	}
	
	/**
	 * Scrolls relatively to the current position.
	 */
	public void scroll(int dx, int dy, boolean smooth) {
		dx = (int)confineHorizontally(dx);
		dy = (int)confineVertically(dy);
		
		if (autoScrollTimer != null) 
			autoScrollTimer.cancel();

		if (smooth) { //Smooth auto scrolling
			Point refPos = getScrollWidgetPos();
			targetScrollX = refPos.x + dx;
			targetScrollY = refPos.y + dy;
			startAutoScroll();
		} 
		else { //Scroll immediately
			moveScrollWidget(dx, dy);
		}
	}
	
	public void scrollToCenter() {
		if (autoScrollTimer != null) 
			autoScrollTimer.cancel();
		
		if (this.getWidgetCount() > 0) {
			int clientWidth = this.getElement().getClientWidth();
			int clientHeight = this.getElement().getClientHeight();
		
			int widgetWidth = this.getWidget(0).getElement().getClientWidth();
			int widgetHeight = this.getWidget(0).getElement().getClientHeight();
			
			scrollToPosition(widgetWidth/2 - clientWidth/2, widgetHeight/2 - clientHeight/2);
		}
	}
	
	public void stopAutoScroll() {
		if (autoScrollTimer != null) 
			autoScrollTimer.cancel();
	}
	
	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return addDomHandler(handler, MouseDownEvent.getType());  
	}

	@Override
	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return addDomHandler(handler, MouseUpEvent.getType());  
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());  
	}

	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());  
	}

	@Override
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return addDomHandler(handler, MouseMoveEvent.getType());  
	}

	@Override
	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
		return addDomHandler(handler, MouseWheelEvent.getType());  
	}
	
	private void notifyScrollPositionChanged() {
		for (Iterator<ScrollListener> it = scrollListeners.iterator(); it.hasNext(); )
			it.next().scrollPositionChanged();
	}

	private void notifyAutoScrollFinished() {
		List<ScrollListener> toRemove = new ArrayList<ScrollListener>();
		for (Iterator<ScrollListener> it = scrollListeners.iterator(); it.hasNext(); ) {
			ScrollListener l = it.next();
			if (l.autoScrollingFinished())
				toRemove.add(l);
		}
		for (int i=0; i<toRemove.size(); i++)
			scrollListeners.remove(toRemove.get(i));
	}

	
	public static interface ScrollListener {
		public void scrollPositionChanged();
		
		/**
		 * Called when a smooth auto scrolling has finished.
		 * @return Returning true removes the listener.
		 */
		public boolean autoScrollingFinished();
	}
	
	
	/**
	 * Mouse handling extension for MouseScrollPanel
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static interface MouseHandlerExtension {
		/**
		 * Mouse wheel handling
		 * @return <code>true</code> if the event has been handled and the MouseScrollPanel should not handle it further.
		 */
		boolean onMouseWheel(MouseWheelEvent event);
		
		/**
		 * Mouse over handling
		 * @return <code>true</code> if the event has been handled and the MouseScrollPanel should not handle it further.
		 */
		boolean onMouseOver(MouseOverEvent event);
		
		/**
		 * Mouse out handling
		 * @return <code>true</code> if the event has been handled and the MouseScrollPanel should not handle it further.
		 */
		boolean onMouseOut(MouseOutEvent event);
		
		/**
		 * Mouse down handling
		 * @return <code>true</code> if the event has been handled and the MouseScrollPanel should not handle it further.
		 */
		boolean onMouseDown(MouseDownEvent event);
		
		/**
		 * Mouse up handling
		 * @return <code>true</code> if the event has been handled and the MouseScrollPanel should not handle it further.
		 */
		boolean onMouseUp(MouseUpEvent event);

		/**
		 * Mouse move handling
		 * @return <code>true</code> if the event has been handled and the MouseScrollPanel should not handle it further.
		 */
		boolean onMouseMove(MouseMoveEvent event);

		void postMouseWheel(MouseWheelEvent event);
		void postMouseOver(MouseOverEvent event);
		void postMouseOut(MouseOutEvent event);
		void postMouseDown(MouseDownEvent event);
		void postMouseUp(MouseUpEvent event);
		void postMouseMove(MouseMoveEvent event);
	}
}
