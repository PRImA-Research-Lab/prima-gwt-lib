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

import java.util.ArrayList;
import java.util.List;

import org.primaresearch.web.gwt.client.page.PageLayoutC;
import org.primaresearch.web.gwt.client.ui.DocumentImageListener;
import org.primaresearch.web.gwt.client.ui.DocumentImageSource;
import org.primaresearch.web.gwt.client.ui.RenderStyles;
import org.primaresearch.web.gwt.client.ui.page.SelectionManager;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;

/**
 * Basic renderer for a document page that renders the document image.<br>
 * Add renderer plugins for additional layers.
 * 
 * @author Christian Clausner
 *
 */
public class PageRenderer implements DocumentImageListener {

	private Canvas canvas;
	private Canvas imageBuffer;
	private boolean imageBuffered = false;
	
	private PageLayoutC pageLayout;
	private String pageContentToRender = null;
	private String contentObjectToHighlightFaintly = null;
	private SelectionManager selectionManager;
	
	private DocumentImageSource imageSource;
	private int width = 0;
	private int height = 0;
	private Context2d context;
	private double zoomFactor = 1.0;
	private RenderStyles renderStyles = DefaultRenderStyles.getInstance();
	
	private List<RendererPlugin> plugins = new ArrayList<RendererPlugin>();
	
	/**
	 * Constructor
	 * @param canvas Drawing canvas
	 * @param pageLayout Page layout to render
	 * @param selectionManager Page object selection manager (to highlight selected objects for instance)
	 * @param imageSource Document image source
	 */
	public PageRenderer(Canvas canvas, PageLayoutC pageLayout, SelectionManager selectionManager, DocumentImageSource imageSource) {
		this.canvas = canvas;
		imageBuffer = Canvas.createIfSupported();
		this.pageLayout = pageLayout;
		this.selectionManager = selectionManager;
		this.imageSource = imageSource;
		imageSource.addListener(this);
	}
	
	/**
	 * Add renderer plug-in (layer)
	 */
	public void addPlugin(RendererPlugin plugin) {
		this.plugins.add(plugin);
	}
	
	/**
	 * Remove renderer plug-in (layer)
	 */
	public void removePlugin(RendererPlugin plugin) {
		plugins.remove(plugin);
	}

	/**
	 * Redraws everything using the current zoom factor
	 */
	public void refresh() {
		refresh(this.zoomFactor);
	}

	/**
	 * Redraws everything
	 * @param zoom Zoom factor to use
	 */
	public synchronized void refresh(double zoom) {
		if (canvas == null)
			return;
		
		this.context = canvas.getContext2d();

		onZoom(zoom);

		//Draw image		
		try {
			Image image = imageSource.getImage(zoom);
			if (image == null)
				drawBackground();
			else
				drawImage(/*image*/);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		//Process plugins
		for (int i=0; i<plugins.size(); i++)
			plugins.get(i).render(this);
	}
	
	/**
	 * Apply new zoom
	 * @param zoomFactor Zoom factor to use
	 */
	private void onZoom(double zoomFactor) {
		this.zoomFactor = zoomFactor; 

		//Update size
		int newWidth = (int)(pageLayout.getWidth() * zoomFactor);
		int newHeight = (int)(pageLayout.getHeight() * zoomFactor);
		if ((newWidth != width || newHeight != height) && newHeight > 0 && newWidth > 0) {
			canvas.setSize(newWidth+"px", newHeight+"px");
			canvas.setCoordinateSpaceWidth(newWidth);
			canvas.setCoordinateSpaceHeight(newHeight);
			
			width = newWidth;
			height = newHeight;
		}
		
		context.setTransform(zoomFactor, 0, 0, zoomFactor, 0, 0);
	}
	
	/**
	 * Draws grey background
	 */
	private void drawBackground() {
		CssColor liteGrey = CssColor.make(200, 200, 200);
		CssColor darkGrey = CssColor.make(100, 100, 100);
		
		boolean dark;
		boolean startLineDark = false;
		final int rectSize = 200;
		for (int x=0; x<canvas.getCoordinateSpaceWidth(); x+=rectSize) {
			dark = startLineDark;
			for (int y=0; y<canvas.getCoordinateSpaceWidth(); y+=rectSize) {
				context.setFillStyle(dark ? darkGrey : liteGrey);
				context.fillRect(x, y, x+rectSize, y+rectSize);
				context.fill();
				dark = !dark;
			}
			startLineDark = !startLineDark;
		}
	}
	
	/**
	 * Draws the document image into a buffer
	 */
	private void bufferImage() {
		//
		//CC: Comment on using a separate canvas as buffer for the page image:
		//
		//The straightforward approach of directly drawing the image on the main canvas
		//proved to slow in Chrome (though extremely fast in IE).
		//As a workaround we now draw the image on an internal canvas (buffer). 
		//We then draw the buffer canvas on the main canvas. This improves
		//performance in Chrome considerably, but slows down IE a little bit.
		
		if (imageBuffered)
			return;

		Image image = imageSource.getImage(zoomFactor);
		if (image == null)
			return;

		imageBuffer.setSize(pageLayout.getWidth()+"px", pageLayout.getHeight()+"px");
		imageBuffer.getCanvasElement().setWidth(pageLayout.getWidth());
		imageBuffer.getCanvasElement().setHeight(pageLayout.getHeight());
		imageBuffer.setCoordinateSpaceWidth(pageLayout.getWidth());
		imageBuffer.setCoordinateSpaceHeight(pageLayout.getHeight());

		imageBuffer.getContext2d().drawImage((ImageElement)(image).getElement().cast(), 0, 0);
		
		imageBuffered = true;
	}
	
	/**
	 * Draws the document image onto the canvas
	 */
	private void drawImage(/*Image image*/) {

		//context.putImageData(((ImageElement)image.getElement().cast()), 0, 0);
		//context.drawImage((ImageElement)image.getElement().cast(), 0, 0);
		CanvasElement ce = imageBuffer.getCanvasElement();
		//int w = imageBuffer.getElement().getClientWidth();
		context.drawImage(ce, 0, 0);
	}
	
	/**
	 * Specify which page content objects to render
	 * @param pageContentToRender See PageLayoutC.TYPE_ constants)
	 */
	public void setPageContentToRender(String pageContentToRender) {
		this.pageContentToRender = pageContentToRender;
	}
	
	/**
	 * Returns the current setting for which page content objects to render
	 * @return See PageLayoutC.TYPE_ constants)
	 */
	public String getPageContentToRender() {
		return pageContentToRender;
	}


	/**
	 * Sets a style provider for rendering page content objects (e.g. fill colour of text region). 
	 */
	public void setRenderStyles(RenderStyles renderStyles) {
		this.renderStyles = renderStyles;
	}

	/**
	 * Highlights the content object with the given ID
	 * @param id Content object ID. Use <code>null</code> to highlight nothing
	 */
	public void highlightContentObject(String id) {
		if (id == null && contentObjectToHighlightFaintly == null) //No change
			return;
		if (id == null) { //No highlight
			contentObjectToHighlightFaintly = null;
			refresh(zoomFactor);
			return;
		}
		if (!id.equals(contentObjectToHighlightFaintly)) { //Changed
			contentObjectToHighlightFaintly = id;
			refresh(zoomFactor);
		}
	}

	public PageLayoutC getPageLayout() {
		return pageLayout;
	}

	public String getContentObjectToHighlightFaintly() {
		return contentObjectToHighlightFaintly;
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	public Context2d getContext() {
		return context;
	}

	public RenderStyles getRenderStyles() {
		return renderStyles;
	}

	public double getZoomFactor() {
		return zoomFactor;
	}
	
	public int measureTextWidth(String text, String font) {
		Context2d context = canvas.getContext2d();
		context.setFont(font);
		return (int)context.measureText(text).getWidth();
	}

	@Override
	public void imageLoaded() {
		bufferImage();
		refresh();
	}

}
