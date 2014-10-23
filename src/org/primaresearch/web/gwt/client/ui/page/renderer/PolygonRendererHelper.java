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
package org.primaresearch.web.gwt.client.ui.page.renderer;

import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.web.gwt.client.ui.RenderStyles.RenderStyle;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

/**
 * Simple static helper class to draw a polygon on a canvas.
 * 
 * @author Christian Clausner
 *
 */
public abstract class PolygonRendererHelper {

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

}
