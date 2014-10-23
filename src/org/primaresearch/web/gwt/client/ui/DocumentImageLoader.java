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
package org.primaresearch.web.gwt.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.primaresearch.web.gwt.client.image.ImageLoader;
import org.primaresearch.web.gwt.client.image.ImageLoader.ImageLoadListener;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.user.client.ui.Image;

/**
 * Helper class to load document images from the server. This loader supports multiple images per document with different zoom levels.
 * 
 * @author Christian Clausner
 *
 */
public class DocumentImageLoader implements DocumentImageSource {

	private Map<Double, Image> images = new HashMap<Double, Image>();
	private Set<DocumentImageListener> listeners = new HashSet<DocumentImageListener>();
	
	public DocumentImageLoader() {
	}

	public DocumentImageLoader(String imageUrl) {
		loadImage(imageUrl);
	}
	
	public void clear() {
		images.clear();
	}
		
	public void loadImage(String url) {
		final double zoomFactor = 1.0;
		ImageLoader.loadImage(url, new ImageLoadListener() {
			@Override
			public void imageLoaded(Image image) {
				images.put(zoomFactor, image);
				notifyListeners();
			}

			@Override
			public void onImageLoadError(String url, ErrorEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
//		ImagePreloader.load(url, new ImageLoadHandler() {
//			@Override
//			public void imageLoaded(ImageLoadEvent event) {
//				final Image image = event.takeImage();
//				images.put(zoomFactor, image);
//				notifyListeners();
//				if (image.getWidth() <= 0) {
//					Timer imageLoadTimer = new Timer() {
//				    	@Override
//				    	public void run() {
//				    		if (image.getWidth() > 0) {
//								notifyListeners();
//								this.cancel();
//				    		}
//				    	}
//				    };
//				    imageLoadTimer.scheduleRepeating(100);
//				}
//			}
//		});
		//final Image image = new Image();
		//image.addLoadHandler(new LoadHandler() {
		//	@Override
		//	public void onLoad(LoadEvent event) {
		//		notiftListeners();
		//	}
		//});
		//image.setUrl(url);
		//images.put(zoomFactor, image);
	}
	

	@Override
	public Image getImage(double zoomFactor) {
		
		Double closestKey = findClosestZoomFactor(zoomFactor);
		if (closestKey == null)
			return null;
		return images.get(closestKey);
	}
	
	private Double findClosestZoomFactor(double factor) {
		double minDist = 100.0;
		Double minDistKey = null;
		for (Iterator<Double> it = images.keySet().iterator(); it.hasNext(); ) {
			Double zoomFactOfImage = it.next();
			if (Math.abs(factor-zoomFactOfImage) < minDist) {
				minDist = Math.abs(factor-zoomFactOfImage);
				minDistKey = zoomFactOfImage;
			}
		}
		return minDistKey;
	}
	
	@Override
	public void addListener(DocumentImageListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void removeListener(DocumentImageListener listener) {
		this.listeners.remove(listener);
	}
	
	private void notifyListeners() {
		for (Iterator<DocumentImageListener> it = listeners.iterator(); it.hasNext(); )
			it.next().imageLoaded();
	}

	@Override
	public int getOriginalImageWidth() {
		Double key = findClosestZoomFactor(1.0);
		if (key == null)
			return 0;
		Image image = images.get(key);
		return (int)(image.getWidth() / key.doubleValue());
	}

	@Override
	public int getOriginalImageHeight() {
		Double key = findClosestZoomFactor(1.0);
		if (key == null)
			return 0;
		Image image = images.get(key);
		return (int)(image.getHeight() / key.doubleValue());
	}

}
