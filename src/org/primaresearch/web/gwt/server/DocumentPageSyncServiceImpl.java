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
package org.primaresearch.web.gwt.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.io.PageWriter;
import org.primaresearch.dla.page.io.UrlInput;
import org.primaresearch.dla.page.io.xml.MetsMultiPageReader;
import org.primaresearch.dla.page.io.xml.StreamTarget;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.io.xml.XmlPageReader;
import org.primaresearch.dla.page.layout.PageLayout;
import org.primaresearch.dla.page.layout.logical.ContentObjectRelation;
import org.primaresearch.dla.page.layout.physical.ContentObject;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.shared.ContentType;
import org.primaresearch.dla.page.layout.physical.shared.LowLevelTextType;
import org.primaresearch.dla.page.layout.physical.shared.RegionType;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextContainer;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.TextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.Glyph;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.dla.page.layout.physical.text.impl.Word;
import org.primaresearch.dla.page.layout.shared.GeometricObject;
import org.primaresearch.io.UnsupportedFormatVersionException;
import org.primaresearch.io.xml.IOError;
import org.primaresearch.maths.geometry.Polygon;
import org.primaresearch.shared.Pair;
import org.primaresearch.shared.variable.StringValue;
import org.primaresearch.shared.variable.Variable;
import org.primaresearch.shared.variable.VariableMap;
import org.primaresearch.web.gwt.client.page.DocumentPageSyncService;
import org.primaresearch.web.gwt.shared.RemoteException;
import org.primaresearch.web.gwt.shared.page.ContentObjectC;
import org.primaresearch.web.gwt.shared.page.ContentObjectSync;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Synchronisation of page content between client and server.
 * 
 * @author Christian Clausner
 *
 */
public class DocumentPageSyncServiceImpl extends RemoteServiceServlet implements DocumentPageSyncService {

	private static final long serialVersionUID = 1L;
	
	private static final String PAGE_OBJECT_CACHE_ATTR = "PAGEObjectCache"; 
	

	@Override
	public ArrayList<ContentObjectC> loadContentObjects(String url, String contentType) throws RemoteException {
		
		
		/*try {
			InputStream is = new URL(url).openStream();
			StringWriter sw = new StringWriter();
			IOUtils.copy(is, sw, "utf-8");
			String theString = sw.toString();
			System.out.println(theString);

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
		
		/*try {
			InputStream is = new URL(url).openStream();
			OutputStream out=new FileOutputStream("c:\\temp\\output.xml");
			byte buf[]=new byte[1024];
			 int len;
			  while((len=is.read(buf))>0)
			  out.write(buf,0,len);
			  out.close();
			  is.close();

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
		
		//Get the page object
		Page page = getPageFile(url);
		
		//Get content
		if (page != null && page.getLayout() != null) {
			PageLayout layout = page.getLayout();
			
			//Regions
			if ("Region".equals(contentType)) {
				ArrayList<ContentObjectC> contentObjects = new ArrayList<ContentObjectC>(layout.getRegionCount());
				List<Region> sorted = page.getLayout().getRegionsSorted();
				for (int i=0; i<sorted.size(); i++) {
					Region region = sorted.get(i);
					
					Polygon polygon = region.getCoords();
					String id = region.getId().toString();
					ContentObjectC contentObj = new ContentObjectC(polygon, id);
					contentObj.setType(region.getType());
					contentObj.setAttributes(region.getAttributes());
					if (region instanceof TextObject)
						contentObj.setText(((TextObject)region).getText());
					
					contentObjects.add(contentObj);
				}
				return contentObjects;
			}
			//Lines
			else if ("TextLine".equals(contentType)) {
				ArrayList<ContentObjectC> contentObjects = new ArrayList<ContentObjectC>(layout.getRegionCount());
				List<Region> sorted = page.getLayout().getRegionsSorted();
				for (int i=0; i<sorted.size(); i++) {
					Region region = sorted.get(i);
					if (! (region instanceof TextRegion))
						continue;
					TextRegion textReg = (TextRegion)region;
					List<LowLevelTextObject> lines = textReg.getTextObjectsSorted();
					for (int l=0; l<lines.size(); l++) {
						TextLine line = (TextLine)lines.get(l);
						
						Polygon polygon = line.getCoords();
						String id = line.getId().toString();
						ContentObjectC contentObj = new ContentObjectC(polygon, id);
						contentObj.setType(LowLevelTextType.TextLine);
						contentObj.setAttributes(line.getAttributes());
						contentObj.setText(((TextObject)line).getText());

						contentObjects.add(contentObj);
					}
				}
				return contentObjects;
			}
			//Words
			else if ("Word".equals(contentType)) {
				ArrayList<ContentObjectC> contentObjects = new ArrayList<ContentObjectC>(layout.getRegionCount());
				List<Region> sorted = page.getLayout().getRegionsSorted();
				for (int i=0; i<sorted.size(); i++) {
					Region region = sorted.get(i);
					if (! (region instanceof TextRegion))
						continue;
					TextRegion textReg = (TextRegion)region;
					List<LowLevelTextObject> lines = textReg.getTextObjectsSorted();
					for (int l=0; l<lines.size(); l++) {
						TextLine line = (TextLine)lines.get(l);

						List<LowLevelTextObject> words = line.getTextObjectsSorted();
						for (int w=0; w<words.size(); w++) {
							Word word = (Word)words.get(w);

							Polygon polygon = word.getCoords();
							String id = word.getId().toString();
							ContentObjectC contentObj = new ContentObjectC(polygon, id);
							contentObj.setType(LowLevelTextType.Word);
							contentObj.setAttributes(word.getAttributes());
							contentObj.setText(((TextObject)word).getText());

							contentObjects.add(contentObj);
						}
					}
				}
				return contentObjects;
			}
			//Glyphs
			else if ("Glyph".equals(contentType)) {
				ArrayList<ContentObjectC> contentObjects = new ArrayList<ContentObjectC>(layout.getRegionCount());
				List<Region> sorted = page.getLayout().getRegionsSorted();
				for (int i=0; i<sorted.size(); i++) {
					Region region = sorted.get(i);
					if (! (region instanceof TextRegion))
						continue;
					TextRegion textReg = (TextRegion)region;
					List<LowLevelTextObject> lines = textReg.getTextObjectsSorted();
					for (int l=0; l<lines.size(); l++) {
						TextLine line = (TextLine)lines.get(l);

						List<LowLevelTextObject> words = line.getTextObjectsSorted();
						for (int w=0; w<words.size(); w++) {
							Word word = (Word)words.get(w);

							List<LowLevelTextObject> glyphs = word.getTextObjectsSorted();
							for (int g=0; g<glyphs.size(); g++) {
								Glyph glyph = (Glyph)glyphs.get(g);
								
								Polygon polygon = glyph.getCoords();
								String id = glyph.getId().toString();
								ContentObjectC contentObj = new ContentObjectC(polygon, id);
								contentObj.setType(LowLevelTextType.Glyph);
								contentObj.setAttributes(glyph.getAttributes());
								contentObj.setText(((TextObject)glyph).getText());

								contentObjects.add(contentObj);
							}
						}
					}
				}
				return contentObjects;
			}
			//Border
			else if ("Border".equals(contentType)) {
				ArrayList<ContentObjectC> contentObjects = new ArrayList<ContentObjectC>(1);
				GeometricObject border = page.getLayout().getBorder(); 
				if (border != null && border.getCoords() != null && border.getCoords().getSize() >= 3) {
					ContentObjectC contentObj = new ContentObjectC(border.getCoords(), "[border]");
					contentObj.setType(ContentType.Border);
					contentObj.setAttributes(new VariableMap());
					contentObjects.add(contentObj);
				}
				return contentObjects;
			}
			//Print space
			else if ("PrintSpace".equals(contentType)) {
				ArrayList<ContentObjectC> contentObjects = new ArrayList<ContentObjectC>(1);
				GeometricObject printSpace = page.getLayout().getPrintSpace(); 
				if (printSpace != null && printSpace.getCoords() != null && printSpace.getCoords().getSize() >= 3) {
					ContentObjectC contentObj = new ContentObjectC(printSpace.getCoords(), "[print space]");
					contentObj.setType(ContentType.PrintSpace);
					contentObj.setAttributes(new VariableMap());
					contentObjects.add(contentObj);
				}
				return contentObjects;
			}
		}

		return null;
	}
	
	public Boolean putTextContent(String url, ContentType type, String contentObjectId, String text) throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return false;
		
		ContentObject obj = page.getLayout().getObject(type, contentObjectId);
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof TextObject))
			return false;
		
		TextObject textObj = (TextObject)obj;
		
		textObj.setText(text);
		
		return true;
	}
	
	/*public MetaData loadMetaData(String url) {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return null;

		return page.getMetaData();
	}*/
	
	public String getPageId(String url) throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return null;

		return page.getGtsId().toString();
	}
	
	public ArrayList<String> getMultiplePageIds(String metsFileUrl) throws RemoteException {
		MetsMultiPageReader metsReader = new MetsMultiPageReader();
		
		try {
			List<String> pageFileNames = metsReader.read(new UrlInput(new URL(metsFileUrl)));
			
			ArrayList<String> multiplePageIds = new ArrayList<String>(pageFileNames.size());
			
			String urlBase = metsFileUrl.substring(0, metsFileUrl.lastIndexOf("/")+1);
			
			for (int i=0; i<pageFileNames.size(); i++) {
				String pageFileName = pageFileNames.get(i);
				
				//Create page URL
				String pageUrl = urlBase + pageFileName;
				
				//Load page content
				String pageId = getPageId(pageUrl);
				multiplePageIds.add(pageId);
			}
			
			return multiplePageIds;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Loads page content objects of the given type of multiple page files (specified in a METS file).
	 */
	@Override
	public ArrayList<ArrayList<ContentObjectC>> loadMultiPageContentObjects(String metsFileUrl, String contentType) throws RemoteException {
		
		MetsMultiPageReader metsReader = new MetsMultiPageReader();
		
		try {
			List<String> pageFileNames = metsReader.read(new UrlInput(new URL(metsFileUrl)));
			
			ArrayList<ArrayList<ContentObjectC>> multiPageContentObjects = new ArrayList<ArrayList<ContentObjectC>>(pageFileNames.size());
			
			String urlBase = metsFileUrl.substring(0, metsFileUrl.lastIndexOf("/")+1);
			
			for (int i=0; i<pageFileNames.size(); i++) {
				String pageFileName = pageFileNames.get(i);
				
				//Create page URL
				String pageUrl = urlBase + pageFileName;
				
				//Load page content
				ArrayList<ContentObjectC> pageContent = loadContentObjects(pageUrl, contentType);
				multiPageContentObjects.add(pageContent);
			}
			
			return multiPageContentObjects;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	/**
	 * Gets a page file from the session cache or loads it using the URL.
	 * @throws RemoteException 
	 */
	private synchronized Page getPageFile(String url) throws RemoteException {
		
		//Get session
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();
		
		//If no URL is given, use the session attribute
		if (url == null)
			url = (String)session.getAttribute(SessionAttributes.PAGE_CONTENT_WEB_SERVICE);

		Page page = null;
		
		//Try to get it from the session first
		@SuppressWarnings("unchecked")
		Map<String, Page> pageCache = (Map<String, Page>)session.getAttribute(PAGE_OBJECT_CACHE_ATTR);
		if (pageCache == null) {
			pageCache = new HashMap<String, Page>();
			session.setAttribute(PAGE_OBJECT_CACHE_ATTR, pageCache);
		}
		
		page = pageCache.get(url);
		
		//Not in cache? -> Load it now
		if (page == null) {
		
			try {
				XmlPageReader reader = PageXmlInputOutput.getReader();
				String uidParam = "Uid="+(String)session.getAttribute(SessionAttributes.USER_ID);
				URL getAttachmentUrl = new URL(url + (url.contains("?") ? ("&"+uidParam) : ("?"+uidParam)));
				System.out.println("Get PAGE file: "+getAttachmentUrl);
				page = reader.read(new UrlInput(getAttachmentUrl));
				//page = XmlInputOutput.readPage("C:\\junit\\page.xml");
				
				//Put it in the cache
				if (page != null) {
					page.setFormatVersion(PageXmlInputOutput.getLatestSchemaModel());
					pageCache.put(url, page);
				}
				else { //Error
					List<IOError> errors = reader.getErrors();
					String errmsg = "";
					for (int i=0; i<errors.size(); i++) 
						errmsg += errors.get(i).getMessage() + "\n";
					throw new RemoteException(errmsg);
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				String errmsg = "Error loading XML file: \n";
				errmsg += e.getMessage() + "\n";
				throw new RemoteException(errmsg);
			} catch (UnsupportedFormatVersionException e) {
				e.printStackTrace();
				String errmsg = "Error loading XML file: \n";
				errmsg += e.getMessage() + "\n";
				throw new RemoteException(errmsg);
			} catch (Exception e) {
				e.printStackTrace();
				String errmsg = "Error loading XML file: \n";
				errmsg += e.getMessage() + "\n";
				throw new RemoteException(errmsg);
			}
		}
		
		return page;
	}

	/**
	 * Adds a new content object on server side. The object type is specified by the given
	 * template object. The template object is enriched with ID and attributes of the new object
	 * and is returned to the client.
	 * @return A sync object with old ID and new content object. The content object may be <code>null</code> if no object could be created.
	 */
	@Override
	public ContentObjectSync addContentObject(String url, ContentObjectC object) throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return null;
		
		PageLayout layout = page.getLayout();
		
		//Create a new page object
		ContentObject newObject = null;
		Polygon polygon = object.getCoords();
		int xCenter = (polygon.getBoundingBox().left + polygon.getBoundingBox().right)/2;
		int yCenter = (polygon.getBoundingBox().top + polygon.getBoundingBox().bottom)/2;
		// Region
		if (object.getType() instanceof RegionType) {
			newObject = layout.createRegion((RegionType)object.getType());
			
			//Sub-type
			VariableMap attrs = object.getAttributes();
			if (attrs != null) {
				Variable attr = attrs.get("type");
				if (attr != null && attr.getValue() != null) {
					String val = attr.getValue().toString();
					if (!"".equals(val)) {
						VariableMap newAttrs = newObject.getAttributes();
						if (newAttrs != null) {
							Variable newAttr = newAttrs.get("type");
							if (newAttr != null) {
								try {
									newAttr.setValue(attr.getValue());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		//Text Line
		else if (LowLevelTextType.TextLine.equals(object.getType())) {
			Region parent = layout.getRegionAt(xCenter, yCenter);
			if (parent != null && parent instanceof TextRegion)
				newObject = ((TextRegion)parent).createTextLine();
		}
		//Word
		else if (LowLevelTextType.Word.equals(object.getType())) {
			ContentObject parent = layout.getObjectAt(xCenter, yCenter, LowLevelTextType.TextLine);
			if (parent != null && parent instanceof TextLine)
				newObject = ((TextLine)parent).createWord();
		}
		//Glyph
		else if (LowLevelTextType.Glyph.equals(object.getType())) {
			ContentObject parent = layout.getObjectAt(xCenter, yCenter, LowLevelTextType.Word);
			if (parent != null && parent instanceof Word)
				newObject = ((Word)parent).createGlyph();
		}
		
		//Copy content
		String oldId = object.getId();
		if (newObject != null) {
			newObject.setCoords(object.getCoords());
			
			object.setId(newObject.getId().toString());
			object.setAttributes(newObject.getAttributes());
			
			return new ContentObjectSync(oldId, object);
		} else {
			return new ContentObjectSync(oldId, null);
		}
	}
	
	@Override
	public Boolean updateOutline(String url, ContentType type, String contentObjectId, Polygon outline) throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return false;
		
		ContentObject obj = page.getLayout().getObject(type, contentObjectId);
		
		if (obj == null)
			return false;

		obj.setCoords(outline);
		
		return true;
	}
	
	@Override
	public Boolean deleteContentObject(String url, ContentType type, String contentObjectId) throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return false;
		
		ContentObject obj = page.getLayout().getObject(type, contentObjectId);
		
		if (obj == null)
			return false;

		if (obj.getType() instanceof RegionType) 
			page.getLayout().removeRegion(obj.getId());
		else if (obj instanceof LowLevelTextObject) {
			ContentObjectRelation relation = page.getLayout().getParentChildRelation(type, contentObjectId);
			if (relation != null && relation.getObject1() != null)
				((LowLevelTextContainer)relation.getObject1()).removeTextObject(obj.getId());
			//((LowLevelTextObject)obj).removeTextObject(obj.getId());
		}
		
		return true;		
	}
	
	@Override
	public Boolean save(String url) throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return false;

		//Get session
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();

		String soapServiceUrl = (String)session.getAttribute(SessionAttributes.SOAP_SERVICE);
		String uid = (String)session.getAttribute(SessionAttributes.USER_ID);
		String aid = (String)session.getAttribute(SessionAttributes.ATTACHMENT_ID);
		
		//Create PAGE XML
		//byte[] xmlData = null;
		String xmlData = null;
		try {
			PageWriter pageWriter = null; 
			pageWriter = PageXmlInputOutput.getWriterForLastestXmlFormat();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			StreamTarget outputTarget = new StreamTarget(outputStream);
			
			//No validation!!!:
			//pageWriter = new XmlPageWriter_2010_03_19(null);
			
			if (!pageWriter.write(page, outputTarget))
				throw new RemoteException("XML file not valid.");
			
			//xmlData = outputStream.toByteArray();
			xmlData = outputStream.toString();
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RemoteException("Could not create PAGE XML file.");
		}
		
		//Soap request
		try {
			SimpleSoapRequest soapRequest = new SimpleSoapRequest(soapServiceUrl, "saveDocumentAttachment");
			soapRequest.addMethodParameter("uid", uid);
			soapRequest.addMethodParameter("aid", aid);
			soapRequest.addMethodParameter("mode", "new");
			soapRequest.addMethodParameter("attachment", xmlData); //Base64.encode(xmlData));
			String response = soapRequest.send();
			
			//Handle response
			InputStream is = new ByteArrayInputStream(response.getBytes());
		    
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			
			Node root = doc.getFirstChild();
			if (root != null) {
				String newAttachmentId = null;
				String oldAttachmentId = (String)session.getAttribute(SessionAttributes.ATTACHMENT_ID);
				Node node = root.getFirstChild();
				while (node != null) {
					if ("SaveDocumentAttachmentResult".equals(node.getNodeName())) {
						
						NamedNodeMap attrs = node.getAttributes();
						if (attrs != null) {
							if(attrs.getNamedItem("returnCode") != null) {
								String returnCode = attrs.getNamedItem("returnCode").getNodeValue();
								//Error?
								if (!"0".equals(returnCode)) {
									
									Node messageNode = node.getFirstChild();
									while (messageNode != null) {
										if ("ReturnMessage".equals(messageNode.getNodeName())) {
											String msg = messageNode.getTextContent();
											throw new Exception(msg);
										}
										messageNode = messageNode.getNextSibling();
									}
								}
							}
							//New attachment ID
							if(attrs.getNamedItem("newAttachmentId") != null) {
								newAttachmentId = attrs.getNamedItem("newAttachmentId").getNodeValue();
								session.setAttribute(SessionAttributes.ATTACHMENT_ID, newAttachmentId);
							}	
						}
						
						break;
					}
					else if ("DocumentAttachmentSources".equals(node.getNodeName())) {
						Node sourceNode = node.getFirstChild();
						while (sourceNode != null) {
							if ("AttachmentSource".equals(sourceNode.getNodeName())) {
								String attachmentSource = sourceNode.getTextContent();
								session.setAttribute(SessionAttributes.PAGE_CONTENT_WEB_SERVICE, attachmentSource);
								
								//Save as new entry in map and remove the old entry
								@SuppressWarnings("unchecked")
								Map<String, Page> pageCache = (Map<String, Page>)session.getAttribute(PAGE_OBJECT_CACHE_ATTR);
								if (pageCache == null) {
									pageCache = new HashMap<String, Page>();
								}
								pageCache.put(attachmentSource, page);
								pageCache.remove(oldAttachmentId);
								break;
							}
							sourceNode = sourceNode.getNextSibling();
						}

					}
					node = node.getNextSibling();
				}
			}
	
			is.close();
			
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RemoteException("PAGE XML file could not be saved.");
		}
		
		return true;		
	}
	
	@Override
	public Boolean savetMultiplePagesLocally(String metsFileUrl, String folderName) throws RemoteException {
		//Get temp dir
		//String tempDir = System.getProperty("java.io.tmpdir");
		//if (tempDir == null)
		//	throw new RemoteException("Could not retrieve temp directory.");
		
		//String targetBaseDir = "/var/www/crowdPrototype";
		String targetBaseDir = "/media/netstor/wwwroot/tomcat/crowdPrototype";
		
		//Create target folder
		File targetFolder = new File(targetBaseDir + File.separator + folderName);
		if (!targetFolder.exists()) {
			if (!targetFolder.mkdir())
				throw new RemoteException("Could not create target directory: "+folderName);
		}
		
		//Save PAGE XML files
		MetsMultiPageReader metsReader = new MetsMultiPageReader();
		try {
			List<String> pageFileNames = metsReader.read(new UrlInput(new URL(metsFileUrl)));
			
			String urlBase = metsFileUrl.substring(0, metsFileUrl.lastIndexOf("/")+1);
			
			for (int i=0; i<pageFileNames.size(); i++) {
				String pageFileName = pageFileNames.get(i);
				
				//Create page URL
				String pageUrl = urlBase + pageFileName;

				//Get file
				Page page = getPageFile(pageUrl);
				
				//Save file
				String filename = pageUrl.substring(pageUrl.lastIndexOf("/"));
				String location = targetFolder.getPath() + File.separator + filename;
				PageXmlInputOutput.writePage(page, location);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("Could not save PAGE XML files: "+e.getMessage());
		}
		
		return true;
	}

	@Override
	public Pair<ContentObjectC,ArrayList<String>> setRegionType(String url, RegionType oldType,
			RegionType newType, String newSubType, String contentObjectId)
			throws RemoteException {
		Page page = getPageFile(url);
		
		if (page == null) //No page object in cache
			return null;
		
		ContentObject obj = page.getLayout().getObject(oldType, contentObjectId);
		
		if (obj == null)
			return null;

		//Children to delete?
		ArrayList<String> toDelete = new ArrayList<String>();
		if (RegionType.TextRegion.equals(oldType) && !RegionType.TextRegion.equals(newType)) {
			collectChildObjectIds((LowLevelTextContainer)obj, toDelete);
		}
		
		//Change type
		ContentObject changed = page.getLayout().changeTypeOfRegion(obj, newType);
		
		//Update sub-type
		if (newSubType != null && !("".equals(newSubType))) {
			VariableMap attrs = changed.getAttributes();
			if (attrs != null) {
				Variable attr = attrs.get("type");
				if (attr != null) {
					try {
						attr.setValue(new StringValue(newSubType));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		//Create ContentObjectC
		Polygon polygon = changed.getCoords();
		String id = changed.getId().toString();
		ContentObjectC syncObj = new ContentObjectC(polygon, id);
		syncObj.setType(changed.getType());
		syncObj.setAttributes(changed.getAttributes());
		if (changed instanceof TextObject)
			syncObj.setText(((TextObject)changed).getText());
		
		return new Pair<ContentObjectC, ArrayList<String>>(syncObj, toDelete);
	}
	
	private void collectChildObjectIds(LowLevelTextContainer parent, ArrayList<String> list) {
		if (parent == null)
			return;
		for (int i=0; i<parent.getTextObjectCount(); i++) {
			LowLevelTextObject child = parent.getTextObject(i);
			if (child != null) {
				list.add(child.getId().toString());
				if (child instanceof LowLevelTextContainer) {
					//Recursion
					collectChildObjectIds((LowLevelTextContainer)child, list);
				}
			}
		}
	}
	
	@Override
	public Boolean revertChanges(String url) throws RemoteException {
		
		//Get session
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();
		
		//If no URL is given, use the session attribute
		if (url == null)
			url = (String)session.getAttribute(SessionAttributes.PAGE_CONTENT_WEB_SERVICE);

		//Try to get it from the session first
		@SuppressWarnings("unchecked")
		Map<String, Page> pageCache = (Map<String, Page>)session.getAttribute(PAGE_OBJECT_CACHE_ATTR);
		if (pageCache == null) {
			pageCache = new HashMap<String, Page>();
			session.setAttribute(PAGE_OBJECT_CACHE_ATTR, pageCache);
		}
		
		//Remove from cache
		pageCache.remove(url);

		return true;
	}

}
