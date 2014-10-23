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

import java.util.Iterator;
import java.util.Set;

import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

/**
 * Page view rendering layer that greys out the area around the currently selected page object.
 *  
 * @author Christian Clausner
 *
 */
public class SelectionFocusRendererPlugin implements RendererPlugin {
	
	private int padding;
	
	private PageRenderer renderer;
	private Context2d context;
	private PageLayoutC pageLayout;
	private boolean enabled = true;
	
	@Override
	public void enable(boolean enable) {
		enabled = enable;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public SelectionFocusRendererPlugin() {
		this(5);
	}
	
	public SelectionFocusRendererPlugin(int padding) {
		this.padding = padding;
	}

	@Override
	public void render(PageRenderer renderer) {
		if (!enabled)
			return;
		
		this.renderer = renderer;
		this.context = renderer.getContext();
		this.pageLayout = renderer.getPageLayout();

		if (pageLayout != null)
			greyOutBackground();
	}

	private void greyOutBackground() {
		if (renderer.getSelectionManager() == null || renderer.getSelectionManager().isEmpty())
			return;
		
		Set<ContentObjectC> selection = renderer.getSelectionManager().getSelection();
		
		if (selection.isEmpty())
			return;
		
		context.save();

		//Outer greyed out area
		int padding = (int)(this.padding / renderer.getZoomFactor());
		ContentObjectC obj;
		context.beginPath();
		for (Iterator<ContentObjectC> it = selection.iterator(); it.hasNext(); ) {
			obj = it.next();

			Rect boundingBox = obj.getCoords().getBoundingBox();
			Rect rect = new Rect(Math.max(0,boundingBox.left - padding), Math.max(0,boundingBox.top-padding), 
								 boundingBox.right+padding, boundingBox.bottom+padding);
			//Top
			context.rect(0, 0, renderer.getPageLayout().getWidth(), rect.top);
			//Bottom
			context.rect(0, rect.bottom, renderer.getPageLayout().getWidth(), renderer.getPageLayout().getHeight());
			//Left
			context.rect(0, rect.top, rect.left, rect.bottom);
			//Right
			context.rect(rect.right, rect.top, renderer.getPageLayout().getWidth(), rect.bottom);
		}
		context.clip();
		
		context.setFillStyle(CssColor.make("rgba(100,100,100,0.5)"));
		context.beginPath();
		context.rect(0.0, 0.0, renderer.getPageLayout().getWidth(), renderer.getPageLayout().getHeight());
		context.fill();
		
		context.restore();
		
		//Inner area
		for (Iterator<ContentObjectC> it = selection.iterator(); it.hasNext(); ) {
			obj = it.next();
			//context.save();
			//Rect boundingBox = obj.getCoords().getBoundingBox();
			//Rect rect = new Rect(boundingBox.left - padding, boundingBox.top-padding, boundingBox.right+padding, boundingBox.bottom+padding);
			//ImageData innerArea = context.getImageData(rect.left, rect.right, rect.getWidth(), rect.getHeight());

			//Clip the rectangle
			//context.beginPath();
			//context.rect(rect.left, rect.top, rect.right, rect.top);
			//context.clip();

			//Draw the polygon
			Polygon polygon = obj.getCoords();
			if (polygon.getSize() < 3)
				continue;
			//context.setStrokeStyle(CssColor.make("rgba(100,100,100,0.2)"));
			//context.setLineWidth(5.0 / renderer.getZoomFactor());
			context.setFillStyle("rgba(0,128,192,0.15)");
			//context.setShadowColor("rgba(0,255,100,0.8");
			//context.setShadowOffsetX(0);
			//context.setShadowOffsetY(0);
			//context.setShadowBlur(5);
			context.beginPath();
			context.moveTo(polygon.getPoint(0).x, polygon.getPoint(0).y);
			for (int i=1; i<polygon.getSize(); i++) {
				context.lineTo(polygon.getPoint(i).x, polygon.getPoint(i).y);
			}
			context.lineTo(polygon.getPoint(0).x, polygon.getPoint(0).y);
			context.fill();
			//context.stroke();
			
			//context.putImageData(innerArea, rect.left, rect.right); //Not allowed (JavaScript security exception)
			
			//context.restore();
		}
	}
	
}
