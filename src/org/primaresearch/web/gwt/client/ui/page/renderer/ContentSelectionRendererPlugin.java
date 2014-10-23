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

import org.primaresearch.maths.geometry.Rect;
import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

/**
 * Renderer plug-in for document page view that highlights the selected content objects with a surrounding rectangle. 
 * 
 * @author Christian Clausner
 *
 */
public class ContentSelectionRendererPlugin implements RendererPlugin {
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

	@Override
	public void render(PageRenderer renderer) {
		if (!enabled)
			return;
		
		this.renderer = renderer;
		this.context = renderer.getContext();
		this.pageLayout = renderer.getPageLayout();
		
		if (pageLayout != null) 
			drawSelection();
	}

	private void drawSelection() {
		if (renderer.getSelectionManager() == null || renderer.getSelectionManager().isEmpty())
			return;
		
		Set<ContentObjectC> selection = renderer.getSelectionManager().getSelection();
		ContentObjectC obj;
		context.setStrokeStyle(CssColor.make(0, 162, 232));
		context.setLineWidth(2.0/renderer.getZoomFactor());
		for (Iterator<ContentObjectC> it = selection.iterator(); it.hasNext(); ) {
			obj = it.next();
			//drawPolygon(obj.getCoords());
			drawRectangle(obj.getCoords().getBoundingBox());
		}		
	}
	
	private void drawRectangle(Rect rect) {
		context.beginPath();
		context.rect(rect.left, rect.top, rect.getWidth(), rect.getHeight());
		context.stroke();
	}

}
