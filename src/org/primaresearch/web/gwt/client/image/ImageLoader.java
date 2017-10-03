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
package org.primaresearch.web.gwt.client.image;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Helper class for loading images from the server (asynchronously).
 * Singleton.
 * 
 * @author Christian Clausner
 *
 */
public class ImageLoader {

	private static ImageLoader instance;
	private Panel imagePanel;
	
	/**
	 * Constructor (creates an invisible panel to attach images)
	 */
	private ImageLoader() {
		//Create a panel that holds images that are to be loaded
		imagePanel = new AbsolutePanel();
		imagePanel.setWidth("0px");
		imagePanel.setHeight("0px");
		RootPanel.get().add(imagePanel);
	}
	
	/**
	 * Returns (creates if first call) the singleton instance of this class.
	 * @return Image loader instance
	 */
	private static ImageLoader getInstance() {
		if (instance == null)
			instance = new ImageLoader();
		return instance;
	}
	
	/**
	 * Loads an image from the given URL (asynchronously).
	 * @param url Image source
	 * @param listener Load listener (will be notified on success or failure)
	 */
	public static synchronized void loadImage(final String url, final ImageLoadListener listener) {
		final ImageLoader instance = getInstance();
		
		final Image image = new Image();
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				listener.imageLoaded(image);
				instance.imagePanel.remove(image);
			}
		});
		image.addErrorHandler(new ErrorHandler() {
		      public void onError(ErrorEvent event) {
		    	  listener.onImageLoadError(url, event);
		      }
		    });
		image.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		//We have to add the image to the DOM, otherwise IE won't load it properly (image size not available)
		instance.imagePanel.add(image);
		image.setUrl(url);
	}
	
	
	/**
	 * Listener interface for loading images from the server.
	 * 
	 * @author Christian Clausner
	 *
	 */
	public static interface ImageLoadListener {
		
		/** Called when the document page image has been loaded successfully */
		public void imageLoaded(Image image);
		/** Called when the document page image could not be loaded */
		public void onImageLoadError(String url, ErrorEvent event);
	}
}
