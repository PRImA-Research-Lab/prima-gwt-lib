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
package org.primaresearch.web.gwt.client.ui.page.renderer;

import org.primaresearch.maths.geometry.Point;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.web.gwt.client.ui.RenderStyles.RenderStyle;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

/**
 * Simple static helper class to draw a polygon or an arrow on a canvas.
 * 
 * @author Christian Clausner
 *
 */
public abstract class RendererHelper {

	private static void setFillColor(Context2d context, RenderStyle style) {
		if (style != null)
			context.setFillStyle(style.getFillColor());
		else
			context.setFillStyle(CssColor.make("rgba(128, 128, 128, 0.2)"));
	}

	private static void setLineColor(Context2d context, RenderStyle style) {
		if (style != null)
			context.setStrokeStyle(style.getLineColor());
		else
			context.setStrokeStyle(CssColor.make("rgb(128, 128, 128)"));
	}
	
	/**
	 * Draws a polygon
	 * @param context Canvas
	 * @param polygon List of points
	 * @param style Colours and line width
	 * @param zoomFactor Current zoom
	 * @param outline Set to <code>true</code> to draw the polygon outline
	 * @param fill Set to <code>true</code> to fill the polygon
	 */
	public static void drawPolygon(Context2d context, Polygon polygon, RenderStyle style, double zoomFactor, boolean outline, boolean fill) {
		setLineColor(context, style);
		setFillColor(context, style);
		context.setLineWidth(style.getLineWidth() / zoomFactor); 
		drawPolygon(context, polygon, outline, fill);
	}
	
	private static void drawPolygon(Context2d context, Polygon polygon, boolean outline, boolean fill) {
		if (polygon == null || polygon.getSize() < 3 || (!outline && !fill))
			return;
		
		context.beginPath();
		context.moveTo(polygon.getPoint(0).x, polygon.getPoint(0).y);
		for (int i=1; i<polygon.getSize(); i++)
			context.lineTo(polygon.getPoint(i).x, polygon.getPoint(i).y);
		context.lineTo(polygon.getPoint(0).x, polygon.getPoint(0).y);
		if (fill)
			context.fill();
		if (outline)
			context.stroke();
	}

	/**
	 * Draws a line with arrow end
	 */
	public static void drawArrow(Context2d gc, int x1, int y1, int x2, int y2, ArrowShape arrow) {
		drawArrow(gc, new Point(x1,y1), new Point(x2,y2), arrow);
	}
	
	/** 
	 * Draws the given polygon (filled)
	 * Adapted from http://www.codeproject.com/KB/GDI/arrows.aspx
	 */
	public static void drawArrow(Context2d gc, Point from, Point to, ArrowShape arrow) {
		if (from == null || to == null)
			return;

		Point base = new Point();
		int[] aptPoly = new int[6];
		double[] vecLine = new double[2];
		double[] vecLeft = new double[2];
		double length;
		double th;
		double ta;

		// set to point
		aptPoly[0] = to.x;
		aptPoly[1] = to.y;

		// build the line vector
		vecLine[0] = (double) aptPoly[0] - from.x;
		vecLine[1] = (double) aptPoly[1] - from.y;

		// build the arrow base vector - normal to the line
		vecLeft[0] = -vecLine[1];
		vecLeft[1] = vecLine[0];

		// setup length parameters
		length = (double) Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
		th = arrow.width / (2.0 * length);
		ta = arrow.width / (2.0 * (Math.tan(arrow.theta) / 2.0) * length);

		// find the base of the arrow
		base.x = (int) (aptPoly[0] + -ta * vecLine[0]);
		base.y = (int) (aptPoly[1] + -ta * vecLine[1]);

		// build the points on the sides of the arrow
		aptPoly[2] = (int) (base.x + th * vecLeft[0]);
		aptPoly[3] = (int) (base.y + th * vecLeft[1]);
		aptPoly[4] = (int) (base.x + -th * vecLeft[0]);
		aptPoly[5] = (int) (base.y + -th * vecLeft[1]);

		// draw we're fillin'...
		gc.beginPath();
		gc.moveTo(from.x, from.y);
		gc.lineTo(aptPoly[0], aptPoly[1]);
		gc.stroke();
		gc.closePath();
		
		gc.beginPath();
		gc.moveTo(aptPoly[0], aptPoly[1]);
		gc.lineTo(aptPoly[2], aptPoly[3]);
		gc.lineTo(aptPoly[4], aptPoly[5]);
		gc.lineTo(aptPoly[0], aptPoly[1]);
		if(arrow.fill) {
			gc.fill();
			gc.stroke();
		}
		// ... or even jes chillin'...
		else {
			gc.stroke();
		}
		gc.closePath();
	}
	
	/**
	 * Data structure for defining an arrow
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static final class ArrowShape {
		public boolean fill = true;
		public int width = 10;
		public double theta = 0.8;
	}
}
